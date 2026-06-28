/*
 * Copyright 2025 Markus Bordihn
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and
 * associated documentation files (the "Software"), to deal in the Software without restriction,
 * including without limitation the rights to use, copy, modify, merge, publish, distribute,
 * sublicense, and/or sell copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or
 * substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT
 * NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM,
 * DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package de.markusbordihn.dialogqueststoryengine.gametest;

import de.markusbordihn.dialogqueststoryengine.content.dialog.BuiltinChoiceAction;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogChoiceDefinition;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogNodeDefinition;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.StartQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import de.markusbordihn.dialogqueststoryengine.logic.condition.GroupOperator;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerInterface;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.network.message.session.CloseSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacketType;
import de.markusbordihn.dialogqueststoryengine.network.message.session.SessionRejectedPacket;
import de.markusbordihn.dialogqueststoryengine.session.DialogSession;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import de.markusbordihn.dialogqueststoryengine.session.SessionRejectionReason;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class DialogWorkflowGameTestHelper {

  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "workflow_dialog");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "workflow_quest");
  private static final String GATED_FACT = "gate_open";

  private static final List<NetworkMessageRecord> captured = new ArrayList<>();
  private static NetworkHandlerInterface previousHandler;

  private DialogWorkflowGameTestHelper() {}

  public static void testThreeNodeNavigation(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    UUID playerUuid = player.getUUID();
    try {
      installEnv(threeNodeDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      GameTestHelpers.assertNotNull(helper, "Session should open", session);
      GameTestHelpers.assertEquals(
          helper, "Start node should be 'start'", "start", session.currentNodeId());
      GameTestHelpers.assertEquals(
          helper, "Open packet node should be 'start'", "start", lastDialogPacket().nodeId());

      SessionManager.submitChoice(player, session.sessionId(), "to_middle", session.revision());
      GameTestHelpers.assertEquals(
          helper, "Should navigate to 'middle'", "middle", session.currentNodeId());
      GameTestHelpers.assertEquals(helper, "Revision should bump to 1", 1, session.revision());
      GameTestHelpers.assertEquals(
          helper,
          "Navigate packet should be NAVIGATE_NODE",
          DialogSessionPacketType.NAVIGATE_NODE,
          lastDialogPacket().type());

      SessionManager.submitChoice(player, session.sessionId(), "to_end", session.revision());
      GameTestHelpers.assertEquals(
          helper, "Should navigate to 'end'", "end", session.currentNodeId());
      GameTestHelpers.assertEquals(helper, "Revision should bump to 2", 2, session.revision());
    } finally {
      teardownEnv(playerUuid);
    }
  }

  public static void testGatedChoiceFilteredFromOpenPacket(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    UUID playerUuid = player.getUUID();
    try {
      installEnv(gatedDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      List<String> beforeUnlock = lastDialogPacket().allowedChoiceIds();
      GameTestHelpers.assertTrue(
          helper, "Open choice should be visible", beforeUnlock.contains("open_choice"));
      GameTestHelpers.assertTrue(
          helper,
          "Gated choice should be hidden before fact set",
          !beforeUnlock.contains("gated_choice"));

      SessionManager.closeSession(player, session.sessionId());
      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, GATED_FACT, FactValue.of(true));

      SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      List<String> afterUnlock = lastDialogPacket().allowedChoiceIds();
      GameTestHelpers.assertTrue(
          helper,
          "Gated choice should be visible after fact set",
          afterUnlock.contains("gated_choice"));
    } finally {
      teardownEnv(playerUuid);
    }
  }

  public static void testBuiltinCloseEndsSession(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    UUID playerUuid = player.getUUID();
    try {
      installEnv(gatedDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      SessionManager.submitChoice(player, session.sessionId(), "open_choice", session.revision());

      GameTestHelpers.assertTrue(
          helper, "Session should be closed after builtin close", !session.isOpen());
      GameTestHelpers.assertTrue(
          helper, "A CloseSessionPacket should be sent", hasPacket(CloseSessionPacket.class));
    } finally {
      teardownEnv(playerUuid);
    }
  }

  public static void testQuestStartChoiceLandsOnNextNode(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    UUID playerUuid = player.getUUID();
    try {
      installEnv(questDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      SessionManager.submitChoice(player, session.sessionId(), "accept", session.revision());

      GameTestHelpers.assertEquals(
          helper, "Should land on 'thanks' node", "thanks", session.currentNodeId());
      GameTestHelpers.assertTrue(
          helper, "Session should still be open after navigation", session.isOpen());
      GameTestHelpers.assertEquals(
          helper,
          "Quest should be ACTIVE in player state",
          QuestState.ACTIVE,
          PlayerStateService.get(playerUuid).get().getQuest(QUEST_ID).state());
    } finally {
      teardownEnv(playerUuid);
    }
  }

  public static void testStaleRevisionRejectedAndStateUnchanged(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    UUID playerUuid = player.getUUID();
    try {
      installEnv(threeNodeDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      SessionManager.submitChoice(player, session.sessionId(), "to_middle", 99);

      GameTestHelpers.assertEquals(
          helper,
          "Rejection reason should be STALE_REVISION",
          SessionRejectionReason.STALE_REVISION,
          lastRejection().reason());
      GameTestHelpers.assertEquals(
          helper, "Node should be unchanged after rejection", "start", session.currentNodeId());
      GameTestHelpers.assertTrue(
          helper, "Session should still be open after rejection", session.isOpen());
    } finally {
      teardownEnv(playerUuid);
    }
  }

  public static void testUnknownChoiceRejected(GameTestHelper helper) {
    ServerPlayer player = helper.makeMockServerPlayerInLevel();
    UUID playerUuid = player.getUUID();
    try {
      installEnv(threeNodeDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      SessionManager.submitChoice(
          player, session.sessionId(), "does_not_exist", session.revision());

      GameTestHelpers.assertEquals(
          helper,
          "Rejection reason should be UNKNOWN_CHOICE",
          SessionRejectionReason.UNKNOWN_CHOICE,
          lastRejection().reason());
      GameTestHelpers.assertTrue(
          helper, "Session should still be open after rejection", session.isOpen());
    } finally {
      teardownEnv(playerUuid);
    }
  }

  private static Map<ResourceLocation, DialogDefinition> threeNodeDialog() {
    DialogChoiceDefinition toMiddle = choice("to_middle", Optional.of("middle"), Optional.empty());
    DialogChoiceDefinition leave =
        choice("leave", Optional.empty(), Optional.of(BuiltinChoiceAction.CLOSE));
    DialogChoiceDefinition toEnd = choice("to_end", Optional.of("end"), Optional.empty());

    Map<String, DialogNodeDefinition> nodes =
        Map.of(
            "start", node("start", List.of(toMiddle, leave)),
            "middle", node("middle", List.of(toEnd)),
            "end", node("end", List.of()));
    return Map.of(DIALOG_ID, new DialogDefinition(DIALOG_ID, 1, "start", nodes));
  }

  private static Map<ResourceLocation, DialogDefinition> gatedDialog() {
    Condition gate = new FactEqualsCondition(FactScope.PLAYER, GATED_FACT, FactValue.of(true));
    DialogChoiceDefinition open =
        new DialogChoiceDefinition(
            "open_choice",
            "label.open",
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY,
            Optional.empty(),
            Optional.of(BuiltinChoiceAction.CLOSE));
    DialogChoiceDefinition gated =
        new DialogChoiceDefinition(
            "gated_choice",
            "label.gated",
            new ConditionGroup(GroupOperator.ALL, List.of(gate)),
            ActionList.EMPTY,
            Optional.empty(),
            Optional.of(BuiltinChoiceAction.CLOSE));

    Map<String, DialogNodeDefinition> nodes = Map.of("root", node("root", List.of(open, gated)));
    return Map.of(DIALOG_ID, new DialogDefinition(DIALOG_ID, 1, "root", nodes));
  }

  private static Map<ResourceLocation, DialogDefinition> questDialog() {
    DialogChoiceDefinition accept =
        new DialogChoiceDefinition(
            "accept",
            "label.accept",
            ConditionGroup.ALWAYS_TRUE,
            new ActionList(List.<Action>of(new StartQuestAction(QUEST_ID))),
            Optional.of("thanks"),
            Optional.empty());

    Map<String, DialogNodeDefinition> nodes =
        Map.of(
            "intro", node("intro", List.of(accept)),
            "thanks", node("thanks", List.of()));
    return Map.of(DIALOG_ID, new DialogDefinition(DIALOG_ID, 1, "intro", nodes));
  }

  private static DialogChoiceDefinition choice(
      String id, Optional<String> next, Optional<BuiltinChoiceAction> builtin) {
    return new DialogChoiceDefinition(
        id, "label." + id, ConditionGroup.ALWAYS_TRUE, ActionList.EMPTY, next, builtin);
  }

  private static DialogNodeDefinition node(String id, List<DialogChoiceDefinition> choices) {
    return new DialogNodeDefinition(id, "speaker." + id, "text." + id, choices);
  }

  private static DialogSessionPacket lastDialogPacket() {
    for (int i = captured.size() - 1; i >= 0; i--) {
      if (captured.get(i) instanceof DialogSessionPacket packet) {
        return packet;
      }
    }
    throw new IllegalStateException("No DialogSessionPacket captured");
  }

  private static SessionRejectedPacket lastRejection() {
    for (int i = captured.size() - 1; i >= 0; i--) {
      if (captured.get(i) instanceof SessionRejectedPacket packet) {
        return packet;
      }
    }
    throw new IllegalStateException("No SessionRejectedPacket captured");
  }

  private static boolean hasPacket(Class<? extends NetworkMessageRecord> type) {
    return captured.stream().anyMatch(type::isInstance);
  }

  private static void installEnv(Map<ResourceLocation, DialogDefinition> dialogs) {
    captured.clear();
    try {
      Field handlerField = NetworkHandlerManager.class.getDeclaredField("networkHandler");
      handlerField.setAccessible(true);
      previousHandler = (NetworkHandlerInterface) handlerField.get(null);
      handlerField.set(null, new CapturingHandler());

      Method replaceAll = DialogContentRegistry.class.getDeclaredMethod("replaceAll", Map.class);
      replaceAll.setAccessible(true);
      replaceAll.invoke(null, dialogs);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to install dialog test environment", e);
    }
  }

  private static void teardownEnv(UUID playerUuid) {
    try {
      Field handlerField = NetworkHandlerManager.class.getDeclaredField("networkHandler");
      handlerField.setAccessible(true);
      handlerField.set(null, previousHandler);
    } catch (ReflectiveOperationException e) {
      // Best-effort restore; ignore in teardown.
    }
    DialogContentRegistry.clear();
    SessionManager.invalidateAll();
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    PlayerStateEvents.clearAll();
    captured.clear();
  }

  private static final class CapturingHandler implements NetworkHandlerInterface {

    @Override
    public <M extends NetworkMessageRecord> void registerClientNetworkMessageHandler(
        ResourceLocation messageId,
        Class<M> networkMessage,
        Function<FriendlyByteBuf, M> creator) {}

    @Override
    public <M extends NetworkMessageRecord> void registerServerNetworkMessageHandler(
        ResourceLocation messageId,
        Class<M> networkMessage,
        Function<FriendlyByteBuf, M> creator) {}

    @Override
    public void sendToPlayer(ServerPlayer serverPlayer, NetworkMessageRecord networkMessageRecord) {
      captured.add(networkMessageRecord);
    }

    @Override
    public void sendToServer(NetworkMessageRecord networkMessageRecord) {}
  }
}
