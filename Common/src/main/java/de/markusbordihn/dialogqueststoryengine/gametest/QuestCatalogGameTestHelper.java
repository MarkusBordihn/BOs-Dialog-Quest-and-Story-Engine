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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestAvailability;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.PrerequisiteMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDisplayCatalogUpsertPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgressSync;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestCatalogGameTestHelper {

  private static final ResourceLocation GATE_A = new ResourceLocation("test", "catalog_gate_a");
  private static final ResourceLocation GATE_B = new ResourceLocation("test", "catalog_gate_b");
  private static final ResourceLocation STEPPED = new ResourceLocation("test", "catalog_stepped");
  private static final ResourceLocation MANUAL_STEP = new ResourceLocation("dqse", "manual");

  private QuestCatalogGameTestHelper() {}

  public static void catalogUpsertPrecedesQuestDelta(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    Map<ResourceLocation, QuestDefinition> previous = QuestRegistryTestSupport.install(quests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      network.clear();

      QuestService.startQuest(player, GATE_A);

      int upsertIndex = firstIndexOf(network.captured(), QuestDisplayCatalogUpsertPacket.class);
      int deltaIndex = firstIndexOf(network.captured(), QuestDeltaPacket.class);
      GameTestHelpers.assertTrue(helper, "Catalog upsert was sent", upsertIndex >= 0);
      GameTestHelpers.assertTrue(
          helper,
          "Catalog upsert precedes the first quest delta",
          upsertIndex >= 0 && upsertIndex < deltaIndex);
    } finally {
      teardownEnvironment(playerUuid, network, previous);
    }
  }

  public static void revealedDependentIsLockedThenAvailable(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    Map<ResourceLocation, QuestDefinition> previous = QuestRegistryTestSupport.install(quests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      QuestService.startQuest(player, GATE_A);
      GameTestHelpers.assertEquals(
          helper,
          "Revealed dependent starts LOCKED",
          QuestAvailability.LOCKED,
          lastCatalogAvailability(network, GATE_B));

      network.clear();
      QuestService.completeQuest(player, GATE_A);

      GameTestHelpers.assertEquals(
          helper,
          "Dependent becomes AVAILABLE once its prerequisite completes",
          QuestAvailability.AVAILABLE,
          lastCatalogAvailability(network, GATE_B));
    } finally {
      teardownEnvironment(playerUuid, network, previous);
    }
  }

  public static void progressWithoutDisplayChangeSkipsCatalogUpsert(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    Map<ResourceLocation, QuestDefinition> previous = QuestRegistryTestSupport.install(quests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      QuestService.startQuest(player, STEPPED);
      network.clear();

      QuestService.progressStep(playerUuid, STEPPED, "collect", 1);

      GameTestHelpers.assertEquals(
          helper,
          "An unchanged catalog entry is not resent on a progress delta",
          0L,
          network.count(QuestDisplayCatalogUpsertPacket.class));
    } finally {
      teardownEnvironment(playerUuid, network, previous);
    }
  }

  private static QuestAvailability lastCatalogAvailability(
      CapturingNetworkTestHandler network, ResourceLocation questId) {
    List<NetworkMessageRecord> captured = network.captured();
    for (int i = captured.size() - 1; i >= 0; i--) {
      if (captured.get(i) instanceof QuestDisplayCatalogUpsertPacket upsert
          && upsert.entry().questId().equals(questId)) {
        return upsert.entry().derivedAvailability();
      }
    }
    throw new IllegalStateException("No catalog upsert captured for " + questId);
  }

  private static int firstIndexOf(
      List<NetworkMessageRecord> captured, Class<? extends NetworkMessageRecord> type) {
    for (int i = 0; i < captured.size(); i++) {
      if (type.isInstance(captured.get(i))) {
        return i;
      }
    }
    return -1;
  }

  private static CapturingNetworkTestHandler installEnvironment() {
    CapturingNetworkTestHandler network = CapturingNetworkTestHandler.install();
    PlayerStateEvents.clearAll();
    QuestProgressSync.register();
    return network;
  }

  private static void teardownEnvironment(
      UUID playerUuid,
      CapturingNetworkTestHandler network,
      Map<ResourceLocation, QuestDefinition> previous) {
    QuestRegistryTestSupport.restore(previous);
    network.restore();
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    PlayerStateEvents.clearAll();
  }

  private static Map<ResourceLocation, QuestDefinition> quests() {
    Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    quests.put(GATE_A, quest(GATE_A, QuestPrerequisites.NONE, Optional.empty()));
    Condition alwaysVisible = context -> true;
    quests.put(
        GATE_B,
        quest(
            GATE_B,
            new QuestPrerequisites(PrerequisiteMode.ALL, List.of(GATE_A)),
            Optional.of(alwaysVisible)));
    quests.put(STEPPED, steppedQuest());
    return quests;
  }

  private static QuestDefinition quest(
      ResourceLocation id, QuestPrerequisites prerequisites, Optional<Condition> visibility) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(visibility, prerequisites, Map.of(), CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }

  private static QuestDefinition steppedQuest() {
    JsonObject json =
        JsonParser.parseString("{\"type\":\"dqse:manual\",\"count\":3}").getAsJsonObject();
    RawQuestStep step = new RawQuestStep("collect", MANUAL_STEP, Optional.empty(), List.of(), json);
    return new QuestDefinition(
        STEPPED,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(
            Optional.empty(),
            QuestPrerequisites.NONE,
            Map.of("collect", step),
            CompletionPolicy.ALL_STEPS,
            true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }
}
