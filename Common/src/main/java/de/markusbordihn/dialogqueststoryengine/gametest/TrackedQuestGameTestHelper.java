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

import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.network.message.session.TrackedQuestPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgressSync;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class TrackedQuestGameTestHelper {

  private static final ResourceLocation QUEST_1 = new ResourceLocation("test", "tracked_one");
  private static final ResourceLocation QUEST_2 = new ResourceLocation("test", "tracked_two");
  private static final ResourceLocation MISSING = new ResourceLocation("test", "tracked_missing");

  private TrackedQuestGameTestHelper() {}

  public static void firstStartAutoTracks(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    Map<ResourceLocation, QuestDefinition> previous = QuestRegistryTestSupport.install(quests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      network.clear();

      QuestService.startQuest(player, QUEST_1);

      GameTestHelpers.assertEquals(
          helper,
          "First started quest is auto-tracked",
          Optional.of(QUEST_1),
          network.last(TrackedQuestPacket.class).trackedQuestId());
    } finally {
      teardownEnvironment(playerUuid, network, previous);
    }
  }

  public static void trackAndUntrackViaPacket(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    Map<ResourceLocation, QuestDefinition> previous = QuestRegistryTestSupport.install(quests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      QuestService.startQuest(player, QUEST_1);
      QuestService.startQuest(player, QUEST_2);
      network.clear();

      UiActionTestDriver.trackQuest(player, Optional.of(QUEST_2));
      GameTestHelpers.assertEquals(
          helper,
          "Tracking QUEST_2 updates tracked",
          Optional.of(QUEST_2),
          network.last(TrackedQuestPacket.class).trackedQuestId());

      UiActionTestDriver.trackQuest(player, Optional.empty());
      GameTestHelpers.assertEquals(
          helper,
          "Untracking clears tracked",
          Optional.empty(),
          network.last(TrackedQuestPacket.class).trackedQuestId());
    } finally {
      teardownEnvironment(playerUuid, network, previous);
    }
  }

  public static void trackingInactiveQuestKeepsCurrent(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    Map<ResourceLocation, QuestDefinition> previous = QuestRegistryTestSupport.install(quests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      QuestService.startQuest(player, QUEST_1);
      network.clear();

      UiActionTestDriver.trackQuest(player, Optional.of(MISSING));

      GameTestHelpers.assertEquals(
          helper,
          "Tracking a non-active quest keeps the current tracked value",
          Optional.of(QUEST_1),
          network.last(TrackedQuestPacket.class).trackedQuestId());
    } finally {
      teardownEnvironment(playerUuid, network, previous);
    }
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
    quests.put(QUEST_1, quest(QUEST_1));
    quests.put(QUEST_2, quest(QUEST_2));
    return quests;
  }

  private static QuestDefinition quest(ResourceLocation id) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(
            Optional.empty(), QuestPrerequisites.NONE, Map.of(), CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }
}
