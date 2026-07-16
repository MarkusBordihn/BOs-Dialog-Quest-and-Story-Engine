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

import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepEvents;
import de.markusbordihn.dialogqueststoryengine.quest.step.types.CollectItemStepType;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class QuestStepTrackerGameTestHelper {

  static final ResourceLocation FIRST_QUEST = new ResourceLocation("dqse_example", "first_quest");
  static final ResourceLocation HUNT_QUEST = new ResourceLocation("dqse_example", "hunt_quest");

  private QuestStepTrackerGameTestHelper() {}

  public static void testCollectItemTracksPickupsWithoutRetroactiveCredit(GameTestHelper helper) {
    Player mockPlayer = helper.makeMockPlayer();
    UUID playerUuid = mockPlayer.getUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      mockPlayer.getInventory().add(new ItemStack(Items.OAK_LOG, 3));

      QuestService.startQuest(playerState, FIRST_QUEST);
      QuestStepEvents.handleQuestStarted(playerState, mockPlayer, FIRST_QUEST);

      String baselineKey = CollectItemStepType.baselineKey(FIRST_QUEST, "collect_item");
      FactValue baseline = playerState.getFact(FactScope.PLAYER, baselineKey);
      GameTestHelpers.assertNotNull(helper, "Baseline fact should be snapshotted", baseline);
      GameTestHelpers.assertEquals(
          helper,
          "Baseline should count pre-existing items",
          3L,
          ((FactValue.LongValue) baseline).value());

      mockPlayer.getInventory().add(new ItemStack(Items.OAK_LOG, 5));
      QuestStepEvents.handleItemPickup(mockPlayer);

      StepProgress stepProgress = playerState.getQuest(FIRST_QUEST).steps().get("collect_item");
      GameTestHelpers.assertEquals(
          helper, "Pickup should credit only new items", 5, stepProgress.progress());
      GameTestHelpers.assertTrue(
          helper, "Step 'collect_item' should be complete", stepProgress.complete());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testInteractEntityCompletesOnRightClick(GameTestHelper helper) {
    Player mockPlayer = helper.makeMockPlayer();
    UUID playerUuid = mockPlayer.getUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      QuestService.startQuest(playerState, FIRST_QUEST);

      Entity pig = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 1));
      QuestStepEvents.handleEntityInteract(mockPlayer, pig);
      GameTestHelpers.assertEquals(
          helper,
          "Non-matching entity should not progress the step",
          0,
          playerState.getQuest(FIRST_QUEST).steps().get("talk_to_villager").progress());

      Entity villager = helper.spawn(EntityType.VILLAGER, new BlockPos(1, 2, 1));
      QuestStepEvents.handleEntityInteract(mockPlayer, villager);
      StepProgress stepProgress = playerState.getQuest(FIRST_QUEST).steps().get("talk_to_villager");
      GameTestHelpers.assertTrue(
          helper,
          "Step 'talk_to_villager' should complete on first valid click",
          stepProgress.complete());

      QuestStepEvents.handleEntityInteract(mockPlayer, villager);
      GameTestHelpers.assertEquals(
          helper,
          "Completed step should not progress further",
          1,
          playerState.getQuest(FIRST_QUEST).steps().get("talk_to_villager").progress());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testKillEntityCreditsKillingBlow(GameTestHelper helper) {
    Player mockPlayer = helper.makeMockPlayer();
    UUID playerUuid = mockPlayer.getUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      QuestService.startQuest(playerState, HUNT_QUEST);

      LivingEntity firstZombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
      QuestStepEvents.handleEntityKilled(
          firstZombie, mockPlayer.damageSources().playerAttack(mockPlayer));
      GameTestHelpers.assertEquals(
          helper,
          "Player kill should credit the step",
          1,
          playerState.getQuest(HUNT_QUEST).steps().get("hunt").progress());

      LivingEntity secondZombie = helper.spawn(EntityType.ZOMBIE, new BlockPos(1, 2, 1));
      QuestStepEvents.handleEntityKilled(secondZombie, secondZombie.damageSources().generic());
      GameTestHelpers.assertEquals(
          helper,
          "Non-player kill should not credit the step",
          1,
          playerState.getQuest(HUNT_QUEST).steps().get("hunt").progress());

      QuestStepEvents.handleEntityKilled(
          secondZombie, mockPlayer.damageSources().playerAttack(mockPlayer));
      StepProgress stepProgress = playerState.getQuest(HUNT_QUEST).steps().get("hunt");
      GameTestHelpers.assertTrue(
          helper, "Step 'hunt' should complete after the second kill", stepProgress.complete());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testManualStepIgnoresTrackerEvents(GameTestHelper helper) {
    Player mockPlayer = helper.makeMockPlayer();
    UUID playerUuid = mockPlayer.getUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).orElseThrow();
      QuestService.startQuest(playerState, HUNT_QUEST);

      QuestStepEvents.handleItemPickup(mockPlayer);
      Entity villager = helper.spawn(EntityType.VILLAGER, new BlockPos(1, 2, 1));
      QuestStepEvents.handleEntityInteract(mockPlayer, villager);
      LivingEntity pig = helper.spawn(EntityType.PIG, new BlockPos(1, 2, 1));
      QuestStepEvents.handleEntityKilled(pig, mockPlayer.damageSources().playerAttack(mockPlayer));

      GameTestHelpers.assertEquals(
          helper,
          "Manual step should ignore tracker events",
          0,
          playerState.getQuest(HUNT_QUEST).steps().get("report").progress());
      GameTestHelpers.assertEquals(
          helper,
          "Kill step should ignore non-matching entity kills",
          0,
          playerState.getQuest(HUNT_QUEST).steps().get("hunt").progress());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }
}
