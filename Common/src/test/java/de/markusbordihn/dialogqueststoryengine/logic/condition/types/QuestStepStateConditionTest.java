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

package de.markusbordihn.dialogqueststoryengine.logic.condition.types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import de.markusbordihn.dialogqueststoryengine.state.StepState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class QuestStepStateConditionTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  private static Condition parse(String raw, List<ContentIssue> issues) {
    JsonObject json = GSON.fromJson(raw, JsonObject.class);
    return QuestStepStateCondition.parse(json, ContentType.DIALOG, TEST_ID, "test.json", issues);
  }

  private static ConditionContext contextWithStep(String stepId, StepState state) {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep(stepId, new StepProgress(state, 0, 1));
    playerState.putQuestDirect(QUEST_ID, questProgress);
    return ConditionContext.ofTest(playerState);
  }

  @Test
  void matchesCompletedStep() {
    Condition condition =
        parse(
            "{\"quest\":\"test:quest\",\"step\":\"talk\",\"state\":\"completed\"}",
            new ArrayList<>());

    assertTrue(condition.evaluate(contextWithStep("talk", StepState.COMPLETED)));
    assertFalse(condition.evaluate(contextWithStep("talk", StepState.ACTIVE)));
  }

  @Test
  void falseWhenQuestNotPresent() {
    Condition condition =
        parse(
            "{\"quest\":\"test:quest\",\"step\":\"talk\",\"state\":\"active\"}", new ArrayList<>());

    assertFalse(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }

  @Test
  void falseWhenStepMissing() {
    Condition condition =
        parse(
            "{\"quest\":\"test:quest\",\"step\":\"missing\",\"state\":\"active\"}",
            new ArrayList<>());

    assertFalse(condition.evaluate(contextWithStep("talk", StepState.ACTIVE)));
  }
}
