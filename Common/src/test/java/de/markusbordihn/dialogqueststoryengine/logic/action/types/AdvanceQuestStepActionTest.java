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

package de.markusbordihn.dialogqueststoryengine.logic.action.types;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class AdvanceQuestStepActionTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "my_quest");
  private static final String STEP_ID = "find_artifact";

  @Test
  void execute_activeStep_incrementsProgress() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    QuestProgress questProgress = playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    questProgress.putStep(STEP_ID, StepProgress.active(5));
    ActionContext ctx = ActionContext.ofTest(playerState);

    new AdvanceQuestStepAction(QUEST_ID, STEP_ID, 2).execute(ctx);

    assertEquals(2, questProgress.steps().get(STEP_ID).progress());
    assertEquals(StepState.ACTIVE, questProgress.steps().get(STEP_ID).state());
  }

  @Test
  void execute_progressReachesRequired_completesStep() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    QuestProgress questProgress = playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    questProgress.putStep(STEP_ID, StepProgress.active(3));
    ActionContext ctx = ActionContext.ofTest(playerState);

    new AdvanceQuestStepAction(QUEST_ID, STEP_ID, 3).execute(ctx);

    assertEquals(StepState.COMPLETED, questProgress.steps().get(STEP_ID).state());
  }

  @Test
  void execute_lockedStep_isIgnored() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    QuestProgress questProgress = playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    questProgress.putStep(STEP_ID, new StepProgress(StepState.LOCKED, 0, 3));
    ActionContext ctx = ActionContext.ofTest(playerState);

    new AdvanceQuestStepAction(QUEST_ID, STEP_ID, 1).execute(ctx);

    assertEquals(StepState.LOCKED, questProgress.steps().get(STEP_ID).state());
    assertEquals(0, questProgress.steps().get(STEP_ID).progress());
  }

  @Test
  void execute_requiredIsZero_completesImmediately() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    QuestProgress questProgress = playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    questProgress.putStep(STEP_ID, StepProgress.active(0));
    ActionContext ctx = ActionContext.ofTest(playerState);

    new AdvanceQuestStepAction(QUEST_ID, STEP_ID, 1).execute(ctx);

    assertEquals(StepState.COMPLETED, questProgress.steps().get(STEP_ID).state());
  }

  @Test
  void execute_questNotInPlayerState_doesNotCrash() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext ctx = ActionContext.ofTest(playerState);

    new AdvanceQuestStepAction(QUEST_ID, STEP_ID, 1).execute(ctx);
  }

  @Test
  void execute_stepNotInQuest_doesNotCrash() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    ActionContext ctx = ActionContext.ofTest(playerState);

    new AdvanceQuestStepAction(QUEST_ID, "nonexistent_step", 1).execute(ctx);
  }
}
