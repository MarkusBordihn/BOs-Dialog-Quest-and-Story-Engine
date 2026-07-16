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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record QuestStepStateCondition(
    ResourceLocation questId, String stepId, StepState expectedState) implements Condition {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "quest_step_state");

  private static final String FIELD_QUEST = "quest";
  private static final String FIELD_STEP = "step";
  private static final String FIELD_STATE = "state";

  public static Condition parse(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has(FIELD_QUEST) || !json.get(FIELD_QUEST).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_QUEST));
      return Condition.NEVER;
    }

    ResourceLocation questId = ResourceLocation.tryParse(json.get(FIELD_QUEST).getAsString());
    if (questId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              id,
              filePath,
              FIELD_QUEST,
              Map.of("value", json.get(FIELD_QUEST).getAsString())));
      return Condition.NEVER;
    }

    if (!json.has(FIELD_STEP) || !json.get(FIELD_STEP).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_STEP));
      return Condition.NEVER;
    }
    String stepId = json.get(FIELD_STEP).getAsString();

    if (!json.has(FIELD_STATE) || !json.get(FIELD_STATE).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_STATE));
      return Condition.NEVER;
    }

    StepState expectedState =
        StepState.fromName(json.get(FIELD_STATE).getAsString().toUpperCase(Locale.ROOT));
    if (expectedState == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              FIELD_STATE,
              Map.of("value", json.get(FIELD_STATE).getAsString())));
      return Condition.NEVER;
    }

    return new QuestStepStateCondition(questId, stepId, expectedState);
  }

  @Override
  public boolean evaluate(ConditionContext conditionContext) {
    QuestProgress questProgress = conditionContext.playerState().getQuest(this.questId);
    if (questProgress == null) {
      return false;
    }

    StepProgress stepProgress = questProgress.steps().get(this.stepId);
    return stepProgress != null && stepProgress.state() == this.expectedState;
  }
}
