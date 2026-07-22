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
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record QuestStateCondition(ResourceLocation questId, QuestState expectedState)
    implements Condition {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "quest_state");

  public static Condition parse(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has("quest") || !json.get("quest").isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, "quest"));
      return Condition.NEVER;
    }

    ResourceLocation questId = ResourceLocation.tryParse(json.get("quest").getAsString());
    if (questId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              id,
              filePath,
              "quest",
              Map.of("value", json.get("quest").getAsString())));
      return Condition.NEVER;
    }

    if (!json.has("state") || !json.get("state").isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, "state"));
      return Condition.NEVER;
    }

    QuestState expectedState = QuestState.fromName(json.get("state").getAsString());
    if (expectedState == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              "state",
              Map.of("value", json.get("state").getAsString())));
      return Condition.NEVER;
    }

    return new QuestStateCondition(questId, expectedState);
  }

  @Override
  public boolean evaluate(ConditionContext conditionContext) {
    QuestProgress questProgress = conditionContext.playerState().getQuest(this.questId);
    QuestState actualState = questProgress != null ? questProgress.state() : QuestState.NOT_STARTED;
    return actualState == this.expectedState;
  }
}
