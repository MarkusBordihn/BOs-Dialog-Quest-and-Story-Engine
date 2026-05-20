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

import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class QuestWorkflowGameTestHelper {

  static final ResourceLocation QUEST_1 = new ResourceLocation("test", "quest_one");
  static final ResourceLocation QUEST_2 = new ResourceLocation("test", "quest_two");

  private QuestWorkflowGameTestHelper() {}

  public static void testPlayerAcceptsQuestAndItBecomesActive(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      QuestProgress result = PlayerStateService.startQuest(playerUuid, QUEST_1).orElse(null);

      GameTestHelpers.assertNotNull(helper, "QuestProgress should not be null", result);
      GameTestHelpers.assertEquals(
          helper, "Quest should be ACTIVE", QuestState.ACTIVE, result.state());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testAllStepsFullyProgressedBeforeQuestComplete(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerStateService.startQuest(playerUuid, QUEST_1);

      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      playerState.getQuest(QUEST_1).putStep("gather_wood", StepProgress.active(5));
      playerState.getQuest(QUEST_1).putStep("build_shelter", StepProgress.active(1));

      PlayerStateService.progressStep(playerUuid, QUEST_1, "gather_wood", 5);
      PlayerStateService.progressStep(playerUuid, QUEST_1, "build_shelter", 1);

      QuestProgress quest = playerState.getQuest(QUEST_1);
      GameTestHelpers.assertTrue(
          helper,
          "Step 'gather_wood' should be complete",
          quest.steps().get("gather_wood").complete());
      GameTestHelpers.assertTrue(
          helper,
          "Step 'build_shelter' should be complete",
          quest.steps().get("build_shelter").complete());

      QuestProgress completed = PlayerStateService.completeQuest(playerUuid, QUEST_1).orElse(null);
      GameTestHelpers.assertNotNull(
          helper, "Completed QuestProgress should not be null", completed);
      GameTestHelpers.assertEquals(
          helper, "Quest should be COMPLETED", QuestState.COMPLETED, completed.state());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testQuestChainFirstCompletedThenSecondStarted(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      PlayerStateService.startQuest(playerUuid, QUEST_1);
      PlayerStateService.completeQuest(playerUuid, QUEST_1);
      PlayerStateService.startQuest(playerUuid, QUEST_2);

      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      GameTestHelpers.assertEquals(
          helper,
          "Quest 1 should be COMPLETED",
          QuestState.COMPLETED,
          playerState.getQuest(QUEST_1).state());
      GameTestHelpers.assertEquals(
          helper,
          "Quest 2 should be ACTIVE",
          QuestState.ACTIVE,
          playerState.getQuest(QUEST_2).state());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testFullWorkflowPersistsThroughSaveAndReload(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      PlayerStateService.startQuest(playerUuid, QUEST_1);
      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      playerState.getQuest(QUEST_1).putStep("deliver_letter", StepProgress.active(1));
      PlayerStateService.progressStep(playerUuid, QUEST_1, "deliver_letter", 1);
      PlayerStateService.completeQuest(playerUuid, QUEST_1);
      PlayerStateService.startQuest(playerUuid, QUEST_2);

      CompoundTag savedNbt = PlayerStateService.getPlayerDataForSave(playerUuid);
      GameTestHelpers.assertNotNull(helper, "Dirty state should produce non-null NBT", savedNbt);

      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateService.onPlayerDataLoaded(playerUuid, savedNbt);

      PlayerState restoredState = PlayerStateService.get(playerUuid).get();
      GameTestHelpers.assertEquals(
          helper,
          "Quest 1 should be COMPLETED after reload",
          QuestState.COMPLETED,
          restoredState.getQuest(QUEST_1).state());
      GameTestHelpers.assertEquals(
          helper,
          "Quest 2 should be ACTIVE after reload",
          QuestState.ACTIVE,
          restoredState.getQuest(QUEST_2).state());
      GameTestHelpers.assertTrue(
          helper,
          "Step 'deliver_letter' should be complete after reload",
          restoredState.getQuest(QUEST_1).steps().get("deliver_letter").complete());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testQuestCompletedEventFires(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    ResourceLocation[] firedQuestId = {null};
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerStateService.startQuest(playerUuid, QUEST_1);

      PlayerStateEvents.addQuestCompletedListener(
          (uuid, questId, progress) -> firedQuestId[0] = questId);

      PlayerStateService.completeQuest(playerUuid, QUEST_1);

      GameTestHelpers.assertEquals(
          helper, "QuestCompleted event should fire with QUEST_1", QUEST_1, firedQuestId[0]);
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }
}
