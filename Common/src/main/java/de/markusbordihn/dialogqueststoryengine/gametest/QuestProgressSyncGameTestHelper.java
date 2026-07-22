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

import de.markusbordihn.dialogqueststoryengine.api.story.StoryManager;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.StoryDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgressSync;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestProgressSyncGameTestHelper {

  private static final ResourceLocation QUEST = new ResourceLocation("dqse_example", "first_quest");
  private static final ResourceLocation STORY = new ResourceLocation("test", "sync_story");

  private QuestProgressSyncGameTestHelper() {}

  public static void questMutationEmitsSingleDelta(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      network.clear();

      QuestService.startQuest(player, QUEST);

      GameTestHelpers.assertEquals(
          helper,
          "Starting a quest should emit exactly one quest delta",
          1L,
          network.count(QuestDeltaPacket.class));
      QuestDeltaPacket delta = network.last(QuestDeltaPacket.class);
      GameTestHelpers.assertEquals(helper, "Delta should target the quest", QUEST, delta.questId());
      GameTestHelpers.assertEquals(
          helper, "Delta state should be ACTIVE", QuestState.ACTIVE, delta.questState());
    } finally {
      teardownEnvironment(playerUuid, network);
    }
  }

  public static void storyUnlockEmitsDelta(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnvironment();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      network.clear();

      StoryManager.unlock(player, STORY);

      GameTestHelpers.assertTrue(
          helper,
          "Unlocking a story should emit a story delta",
          network.has(StoryDeltaPacket.class));
      StoryDeltaPacket delta = network.last(StoryDeltaPacket.class);
      GameTestHelpers.assertTrue(
          helper,
          "Story delta should list the unlocked story",
          delta.unlockedStoryIds().contains(STORY));
    } finally {
      teardownEnvironment(playerUuid, network);
    }
  }

  private static CapturingNetworkTestHandler installEnvironment() {
    CapturingNetworkTestHandler network = CapturingNetworkTestHandler.install();
    PlayerStateEvents.clearAll();
    QuestProgressSync.register();
    return network;
  }

  private static void teardownEnvironment(UUID playerUuid, CapturingNetworkTestHandler network) {
    network.restore();
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    PlayerStateEvents.clearAll();
  }
}
