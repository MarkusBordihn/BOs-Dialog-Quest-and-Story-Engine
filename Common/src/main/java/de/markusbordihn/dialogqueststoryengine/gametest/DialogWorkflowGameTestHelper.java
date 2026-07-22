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

import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogChoiceDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogNodeDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.data.session.SessionRejectionReason;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.StartQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import de.markusbordihn.dialogqueststoryengine.logic.condition.GroupOperator;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.network.message.session.CloseSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacketType;
import de.markusbordihn.dialogqueststoryengine.network.message.session.SessionRejectedPacket;
import de.markusbordihn.dialogqueststoryengine.session.DialogSession;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class DialogWorkflowGameTestHelper {

  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "workflow_dialog");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "workflow_quest");
  private static final String GATED_FACT = "gate_open";

  private static CapturingNetworkTestHandler network;
  private static Map<ResourceLocation, QuestDefinition> previousQuests;

  private DialogWorkflowGameTestHelper() {}

  public static void testThreeNodeNavigation(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(threeNodeDialog());
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
      teardownEnvironment(playerUuid);
    }
  }

  public static void testGatedChoiceFilteredFromOpenPacket(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(gatedDialog());
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
      teardownEnvironment(playerUuid);
    }
  }

  public static void testBuiltinCloseEndsSession(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(gatedDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      SessionManager.submitChoice(player, session.sessionId(), "open_choice", session.revision());

      GameTestHelpers.assertTrue(
          helper, "Session should be closed after builtin close", !session.isOpen());
      GameTestHelpers.assertTrue(
          helper, "A CloseSessionPacket should be sent", hasPacket(CloseSessionPacket.class));
    } finally {
      teardownEnvironment(playerUuid);
    }
  }

  public static void testQuestStartChoiceLandsOnNextNode(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(questDialog());
      installWorkflowQuest();
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
      teardownEnvironment(playerUuid);
    }
  }

  public static void testStaleRevisionRejectedAndStateUnchanged(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(threeNodeDialog());
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
      teardownEnvironment(playerUuid);
    }
  }

  public static void testUnknownChoiceRejected(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(threeNodeDialog());
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
      teardownEnvironment(playerUuid);
    }
  }

  public static void testChoiceButtonPacketNavigates(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(threeNodeDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      UiActionTestDriver.pressChoice(player, session.sessionId(), "to_middle", session.revision());

      GameTestHelpers.assertEquals(
          helper, "Choice packet should navigate to 'middle'", "middle", session.currentNodeId());
      GameTestHelpers.assertEquals(
          helper,
          "Response should be a NAVIGATE_NODE packet",
          DialogSessionPacketType.NAVIGATE_NODE,
          lastDialogPacket().type());
    } finally {
      teardownEnvironment(playerUuid);
    }
  }

  public static void testChoiceButtonPacketStartsQuest(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(questDialog());
      installWorkflowQuest();
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      UiActionTestDriver.pressChoice(player, session.sessionId(), "accept", session.revision());

      GameTestHelpers.assertEquals(
          helper,
          "Choice packet should start the quest",
          QuestState.ACTIVE,
          PlayerStateService.get(playerUuid).get().getQuest(QUEST_ID).state());
      GameTestHelpers.assertEquals(
          helper, "Should land on 'thanks' node", "thanks", session.currentNodeId());
    } finally {
      teardownEnvironment(playerUuid);
    }
  }

  public static void testCloseButtonPacketEndsSession(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(threeNodeDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      UiActionTestDriver.closeSession(player, session.sessionId());

      GameTestHelpers.assertTrue(helper, "Close packet should end the session", !session.isOpen());
    } finally {
      teardownEnvironment(playerUuid);
    }
  }

  public static void testStaleChoiceButtonPacketRejected(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    try {
      installEnvironment(threeNodeDialog());
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      DialogSession session = SessionManager.openDialogSession(player, DIALOG_ID, Optional.empty());
      UiActionTestDriver.pressChoice(player, session.sessionId(), "to_middle", 99);

      GameTestHelpers.assertEquals(
          helper,
          "Stale choice packet should be rejected",
          SessionRejectionReason.STALE_REVISION,
          lastRejection().reason());
      GameTestHelpers.assertEquals(
          helper, "Node should be unchanged after rejection", "start", session.currentNodeId());
    } finally {
      teardownEnvironment(playerUuid);
    }
  }

  private static Map<ResourceLocation, DialogDefinition> threeNodeDialog() {
    DialogChoiceDefinition toMiddle = choice("to_middle", Optional.of("middle"), false);
    DialogChoiceDefinition leave = choice("leave", Optional.empty(), true);
    DialogChoiceDefinition toEnd = choice("to_end", Optional.of("end"), false);

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
            true,
            false);
    DialogChoiceDefinition gated =
        new DialogChoiceDefinition(
            "gated_choice",
            "label.gated",
            new ConditionGroup(GroupOperator.ALL, List.of(gate)),
            ActionList.EMPTY,
            Optional.empty(),
            true,
            false);

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
            Optional.of("thanks"));

    Map<String, DialogNodeDefinition> nodes =
        Map.of(
            "intro", node("intro", List.of(accept)),
            "thanks", node("thanks", List.of()));
    return Map.of(DIALOG_ID, new DialogDefinition(DIALOG_ID, 1, "intro", nodes));
  }

  private static void installWorkflowQuest() {
    RawQuestStep step =
        new RawQuestStep(
            "step",
            new ResourceLocation(Constants.MOD_NAMESPACE, "manual"),
            JsonParser.parseString("{\"type\":\"dqse:manual\"}").getAsJsonObject());
    QuestDefinition quest =
        new QuestDefinition(
            QUEST_ID,
            1,
            NarrativeMetadata.EMPTY,
            new DisplaySection(
                "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
            new LogicSection(
                Optional.empty(),
                QuestPrerequisites.NONE,
                Map.of("step", step),
                CompletionPolicy.ALL_STEPS,
                true),
            ActionList.EMPTY,
            RewardSection.EMPTY);
    Map<ResourceLocation, QuestDefinition> snapshot = new LinkedHashMap<>();
    for (QuestDefinition existing : QuestContentRegistry.all()) {
      snapshot.put(existing.id(), existing);
    }
    previousQuests = snapshot;

    Map<ResourceLocation, QuestDefinition> withWorkflow = new LinkedHashMap<>(snapshot);
    withWorkflow.put(QUEST_ID, quest);
    replaceQuests(withWorkflow);
  }

  private static void replaceQuests(Map<ResourceLocation, QuestDefinition> quests) {
    try {
      Method replaceAll = QuestContentRegistry.class.getDeclaredMethod("replaceAll", Map.class);
      replaceAll.setAccessible(true);
      replaceAll.invoke(null, quests);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to replace quest registry", e);
    }
  }

  private static DialogChoiceDefinition choice(String id, Optional<String> next, boolean close) {
    return new DialogChoiceDefinition(
        id, "label." + id, ConditionGroup.ALWAYS_TRUE, ActionList.EMPTY, next, close, false);
  }

  private static DialogNodeDefinition node(String id, List<DialogChoiceDefinition> choices) {
    return new DialogNodeDefinition(id, "speaker." + id, "text." + id, choices);
  }

  private static DialogSessionPacket lastDialogPacket() {
    return network.last(DialogSessionPacket.class);
  }

  private static SessionRejectedPacket lastRejection() {
    return network.last(SessionRejectedPacket.class);
  }

  private static boolean hasPacket(Class<? extends NetworkMessageRecord> type) {
    return network.has(type);
  }

  private static void installEnvironment(Map<ResourceLocation, DialogDefinition> dialogs) {
    network = CapturingNetworkTestHandler.install();
    try {
      Method replaceAll = DialogContentRegistry.class.getDeclaredMethod("replaceAll", Map.class);
      replaceAll.setAccessible(true);
      replaceAll.invoke(null, dialogs);
    } catch (ReflectiveOperationException e) {
      throw new IllegalStateException("Failed to install dialog test environment", e);
    }
  }

  private static void teardownEnvironment(UUID playerUuid) {
    if (network != null) {
      network.restore();
      network = null;
    }
    DialogContentRegistry.clear();
    if (previousQuests != null) {
      replaceQuests(previousQuests);
      previousQuests = null;
    }
    SessionManager.invalidateAll();
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    PlayerStateEvents.clearAll();
  }
}
