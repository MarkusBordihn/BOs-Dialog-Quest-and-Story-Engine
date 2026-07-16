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

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestRewardClaimReason;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardClaimState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardClaimMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestRewardClaimResultPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RewardGameTestHelper {

  private static final ResourceLocation AUTO = new ResourceLocation("test", "auto_reward_quest");
  private static final ResourceLocation MANUAL =
      new ResourceLocation("test", "manual_reward_quest");

  private RewardGameTestHelper() {}

  public static void automaticRewardGrantedOnCompletion(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(rewardQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      QuestService.startQuest(player, AUTO);
      network.clear();

      QuestService.completeQuest(player, AUTO);

      GameTestHelpers.assertTrue(
          helper,
          "Automatic reward grants the item",
          player.getInventory().countItem(Items.EMERALD) >= 1);
      GameTestHelpers.assertEquals(
          helper,
          "Delta reports CLAIMED",
          RewardClaimState.CLAIMED,
          network.last(QuestDeltaPacket.class).rewardClaimState());
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  public static void manualRewardClaimSucceeds(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(rewardQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      QuestService.startQuest(player, MANUAL);
      QuestService.completeQuest(player, MANUAL);
      GameTestHelpers.assertEquals(
          helper,
          "Manual reward is AVAILABLE after completion",
          RewardClaimState.AVAILABLE,
          playerState.getQuest(MANUAL).rewardClaimState());

      network.clear();
      UiActionTestDriver.claimRewards(player, MANUAL, playerState.getQuest(MANUAL).revision());

      GameTestHelpers.assertTrue(
          helper,
          "Claim result is success",
          network.last(QuestRewardClaimResultPacket.class).isSuccess());
      GameTestHelpers.assertTrue(
          helper, "Claim grants the item", player.getInventory().countItem(Items.EMERALD) >= 1);
      GameTestHelpers.assertEquals(
          helper,
          "Quest is now CLAIMED",
          RewardClaimState.CLAIMED,
          playerState.getQuest(MANUAL).rewardClaimState());
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  public static void inventoryFullRejectsClaim(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(rewardQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      for (int slot = 0; slot < player.getInventory().items.size(); slot++) {
        player.getInventory().items.set(slot, new ItemStack(Items.STONE, 64));
      }
      QuestService.startQuest(player, MANUAL);
      QuestService.completeQuest(player, MANUAL);
      network.clear();

      UiActionTestDriver.claimRewards(player, MANUAL, playerState.getQuest(MANUAL).revision());

      GameTestHelpers.assertEquals(
          helper,
          "Full inventory yields INVENTORY_FULL",
          Optional.of(QuestRewardClaimReason.INVENTORY_FULL),
          network.last(QuestRewardClaimResultPacket.class).reason());
      GameTestHelpers.assertEquals(
          helper,
          "Reward stays AVAILABLE after a failed claim",
          RewardClaimState.AVAILABLE,
          playerState.getQuest(MANUAL).rewardClaimState());
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  public static void secondClaimRejectedAsNotAvailable(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(rewardQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      QuestService.startQuest(player, MANUAL);
      QuestService.completeQuest(player, MANUAL);
      UiActionTestDriver.claimRewards(player, MANUAL, playerState.getQuest(MANUAL).revision());
      network.clear();

      UiActionTestDriver.claimRewards(player, MANUAL, playerState.getQuest(MANUAL).revision());

      GameTestHelpers.assertEquals(
          helper,
          "A second claim is REWARDS_NOT_AVAILABLE",
          Optional.of(QuestRewardClaimReason.REWARDS_NOT_AVAILABLE),
          network.last(QuestRewardClaimResultPacket.class).reason());
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  public static void staleRevisionRejected(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(rewardQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      QuestService.startQuest(player, MANUAL);
      QuestService.completeQuest(player, MANUAL);
      network.clear();

      UiActionTestDriver.claimRewards(player, MANUAL, playerState.getQuest(MANUAL).revision() + 99);

      GameTestHelpers.assertEquals(
          helper,
          "A stale revision is rejected",
          Optional.of(QuestRewardClaimReason.STALE_REVISION),
          network.last(QuestRewardClaimResultPacket.class).reason());
      GameTestHelpers.assertTrue(
          helper,
          "Stale claim also resends the current delta",
          network.has(QuestDeltaPacket.class));
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  private static CapturingNetworkTestHandler installEnv() {
    CapturingNetworkTestHandler network = CapturingNetworkTestHandler.install();
    PlayerStateEvents.clearAll();
    QuestProgressSync.register();
    return network;
  }

  private static void teardownEnv(
      UUID playerUuid,
      CapturingNetworkTestHandler network,
      Map<ResourceLocation, QuestDefinition> previous) {
    QuestRegistryTestSupport.restore(previous);
    network.restore();
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    PlayerStateEvents.clearAll();
  }

  private static Map<ResourceLocation, QuestDefinition> rewardQuests() {
    RewardEntry.Item emerald =
        new RewardEntry.Item(new ResourceLocation("minecraft", "emerald"), 1);
    Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    quests.put(
        AUTO,
        quest(
            AUTO,
            new RewardSection(
                RewardClaimMode.AUTOMATIC, Optional.empty(), Optional.empty(), List.of(emerald))));
    quests.put(
        MANUAL,
        quest(
            MANUAL,
            new RewardSection(
                RewardClaimMode.MANUAL, Optional.empty(), Optional.empty(), List.of(emerald))));
    return quests;
  }

  private static QuestDefinition quest(ResourceLocation id, RewardSection rewards) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(
            Optional.empty(), QuestPrerequisites.NONE, Map.of(), CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        rewards);
  }
}
