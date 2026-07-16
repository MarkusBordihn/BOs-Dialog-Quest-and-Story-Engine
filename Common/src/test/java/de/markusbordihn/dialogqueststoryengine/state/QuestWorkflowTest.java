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

package de.markusbordihn.dialogqueststoryengine.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestWorkflowTest {

  private static final UUID PLAYER_UUID = UUID.fromString("33333333-3333-3333-3333-333333333333");
  private static final ResourceLocation QUEST_1 = new ResourceLocation("test", "quest_one");
  private static final ResourceLocation QUEST_2 = new ResourceLocation("test", "quest_two");

  @BeforeEach
  void setUp() {
    QuestTestFixtures.install(QUEST_1, QUEST_2);
  }

  @AfterEach
  void tearDown() {
    PlayerStateService.markPlayerDataSaved(PLAYER_UUID);
    PlayerStateService.onPlayerLoggedOut(PLAYER_UUID);
    PlayerStateEvents.clearAll();
    QuestTestFixtures.clear();
  }

  @Test
  void playerAcceptsQuestAndItBecomesActive() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    Optional<QuestProgress> result = PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    assertTrue(result.isPresent());
    assertEquals(QuestState.ACTIVE, result.get().state());
  }

  @Test
  void acceptedQuestIsStoredInPlayerState() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();

    assertTrue(playerState.hasQuest(QUEST_1));
    assertEquals(QuestState.ACTIVE, playerState.getQuest(QUEST_1).state());
  }

  @Test
  void questCompletedTransitionsToCompletedState() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    Optional<QuestProgress> result = PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);

    assertTrue(result.isPresent());
    assertEquals(QuestState.COMPLETED, result.get().state());
  }

  @Test
  void questCompletedEventFires() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    ResourceLocation[] firedQuestId = {null};
    PlayerStateEvents.addQuestCompletedListener(
        (uuid, questId, progress) -> firedQuestId[0] = questId);

    PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);

    assertEquals(QUEST_1, firedQuestId[0]);
  }

  @Test
  void allStepsFullyProgressedBeforeQuestComplete() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();
    playerState.getQuest(QUEST_1).putStep("gather_wood", StepProgress.active(5));
    playerState.getQuest(QUEST_1).putStep("build_shelter", StepProgress.active(1));

    PlayerStateService.progressStep(PLAYER_UUID, QUEST_1, "gather_wood", 5);
    PlayerStateService.progressStep(PLAYER_UUID, QUEST_1, "build_shelter", 1);

    QuestProgress quest = playerState.getQuest(QUEST_1);
    assertTrue(quest.steps().get("gather_wood").complete());
    assertTrue(quest.steps().get("build_shelter").complete());

    Optional<QuestProgress> completed = PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);
    assertEquals(QuestState.COMPLETED, completed.get().state());
  }

  @Test
  void partialProgressDoesNotAutoComplete() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();
    playerState.getQuest(QUEST_1).putStep("kill_ten_wolves", StepProgress.active(10));

    var step = PlayerStateService.progressStep(PLAYER_UUID, QUEST_1, "kill_ten_wolves", 3);

    assertTrue(step.isPresent());
    assertEquals(3, step.get().progress());
    assertTrue(!step.get().complete(), "Step should not be complete at 3/10");
    assertEquals(QuestState.ACTIVE, playerState.getQuest(QUEST_1).state());
  }

  @Test
  void questChainFirstCompletedThenSecondStarted() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);
    PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_2);

    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();
    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_1).state());
    assertEquals(QuestState.ACTIVE, playerState.getQuest(QUEST_2).state());
  }

  @Test
  void questChainBothQuestsPersistIndependently() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);
    PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_2);
    PlayerStateService.completeQuest(PLAYER_UUID, QUEST_2);

    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();
    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_1).state());
    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_2).state());
  }

  @Test
  void fullWorkflowPersistsThroughSaveAndReload() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);
    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();
    playerState.getQuest(QUEST_1).putStep("deliver_letter", StepProgress.active(1));
    PlayerStateService.progressStep(PLAYER_UUID, QUEST_1, "deliver_letter", 1);
    PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_2);

    CompoundTag savedNbt = PlayerStateService.getPlayerDataForSave(PLAYER_UUID);
    assertNotNull(savedNbt, "Dirty state should produce non-null NBT");
    PlayerStateService.markPlayerDataSaved(PLAYER_UUID);

    PlayerStateService.onPlayerLoggedOut(PLAYER_UUID);
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, savedNbt);

    PlayerState restoredState = PlayerStateService.get(PLAYER_UUID).get();
    assertEquals(QuestState.COMPLETED, restoredState.getQuest(QUEST_1).state());
    assertEquals(QuestState.ACTIVE, restoredState.getQuest(QUEST_2).state());
    assertTrue(restoredState.getQuest(QUEST_1).steps().get("deliver_letter").complete());
    assertNull(
        PlayerStateService.getPlayerDataForSave(PLAYER_UUID),
        "State should not be dirty after reload without mutation");
  }
}
