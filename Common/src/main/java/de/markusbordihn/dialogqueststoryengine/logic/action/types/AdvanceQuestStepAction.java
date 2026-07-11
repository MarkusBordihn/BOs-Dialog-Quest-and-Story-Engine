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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record AdvanceQuestStepAction(ResourceLocation questId, String stepId, int amount)
    implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "advance_quest_step");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has("quest") || !jsonObject.get("quest").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "quest"));
      return Action.NOOP;
    }

    ResourceLocation questId = ResourceLocation.tryParse(jsonObject.get("quest").getAsString());
    if (questId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              contentId,
              filePath,
              "quest",
              Map.of("value", jsonObject.get("quest").getAsString())));
      return Action.NOOP;
    }

    if (!jsonObject.has("step") || !jsonObject.get("step").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "step"));
      return Action.NOOP;
    }

    String stepId = jsonObject.get("step").getAsString();
    if (stepId.isEmpty()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "step"));
      return Action.NOOP;
    }

    int amount = 1;
    if (jsonObject.has("amount") && jsonObject.get("amount").isJsonPrimitive()) {
      amount = Math.max(1, jsonObject.get("amount").getAsInt());
    }

    return new AdvanceQuestStepAction(questId, stepId, amount);
  }

  @Override
  public void execute(ActionContext actionContext) {
    if (QuestService.progressStep(actionContext, this.questId, this.stepId, this.amount)
        .isEmpty()) {
      log.warn(
          "{} advance_quest_step: step '{}' not found in quest '{}' — skipping",
          Constants.LOG_PREFIX,
          this.stepId,
          this.questId);
    }
  }
}
