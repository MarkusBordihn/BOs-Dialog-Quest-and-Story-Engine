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

package de.markusbordihn.dialogqueststoryengine.content.quest;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.json.RawAction;
import de.markusbordihn.dialogqueststoryengine.data.json.RawJsonListReader;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public final class QuestContentParser {

  static final String FIELD_DISPLAY = "display";
  static final String FIELD_LOGIC = "logic";
  static final String FIELD_REWARDS = "rewards";
  static final String FIELD_TITLE_KEY = "title_key";
  static final String FIELD_DESCRIPTION_KEY = "description_key";
  static final String FIELD_CATEGORY = "category";
  static final String FIELD_VISIBILITY = "visibility";
  static final String FIELD_WHEN = "when";
  static final String FIELD_STEPS = "steps";
  static final String FIELD_COMPLETION_POLICY = "completion_policy";
  static final String FIELD_REPEATABLE = "repeatable";
  static final String FIELD_SYNC_SCOPE = "sync_scope";
  static final String FIELD_TYPE = "type";

  private QuestContentParser() {}

  public static ParseResult<QuestDefinition> parse(
      ResourceLocation id, String filePath, JsonObject jsonObject) {
    List<ContentIssue> issues = new ArrayList<>();

    Optional<Integer> schema =
        JsonFieldReader.readSchema(
            jsonObject, ContentType.QUEST, ContentType.QUEST.currentSchema(), id, filePath, issues);
    if (schema.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<JsonObject> displayJson =
        JsonFieldReader.readObject(
            jsonObject, FIELD_DISPLAY, ContentType.QUEST, id, filePath, issues);
    if (displayJson.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<DisplaySection> display = parseDisplay(displayJson.get(), id, filePath, issues);
    if (display.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<JsonObject> logicJson =
        JsonFieldReader.readObject(
            jsonObject, FIELD_LOGIC, ContentType.QUEST, id, filePath, issues);
    if (logicJson.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<LogicSection> logic = parseLogic(logicJson.get(), id, filePath, issues);
    if (logic.isEmpty()) {
      return ParseResult.failure(issues);
    }

    List<RawAction> rewards = List.of();
    if (jsonObject.has(FIELD_REWARDS) && jsonObject.get(FIELD_REWARDS).isJsonArray()) {
      rewards =
          RawJsonListReader.readActions(
              jsonObject.getAsJsonArray(FIELD_REWARDS), ContentType.QUEST, id, filePath, issues);
    }

    return ParseResult.success(
        new QuestDefinition(id, schema.get(), display.get(), logic.get(), rewards), issues);
  }

  private static Optional<DisplaySection> parseDisplay(
      JsonObject displayJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Optional<String> titleKey =
        JsonFieldReader.readString(
            displayJson, FIELD_TITLE_KEY, ContentType.QUEST, id, filePath, issues);
    Optional<String> descriptionKey =
        JsonFieldReader.readString(
            displayJson, FIELD_DESCRIPTION_KEY, ContentType.QUEST, id, filePath, issues);

    if (titleKey.isEmpty() || descriptionKey.isEmpty()) {
      return Optional.empty();
    }

    Optional<ResourceLocation> category = Optional.empty();
    if (displayJson.has(FIELD_CATEGORY)) {
      JsonElement categoryElement = displayJson.get(FIELD_CATEGORY);
      if (categoryElement.isJsonPrimitive() && categoryElement.getAsJsonPrimitive().isString()) {
        try {
          category = Optional.of(new ResourceLocation(categoryElement.getAsString()));
        } catch (ResourceLocationException e) {
          issues.add(
              ContentIssue.of(
                  IssueCode.INVALID_RESOURCE_LOCATION,
                  ContentType.QUEST,
                  id,
                  filePath,
                  FIELD_DISPLAY + "." + FIELD_CATEGORY,
                  Map.of("value", categoryElement.getAsString())));
        }
      }
    }

    return Optional.of(new DisplaySection(titleKey.get(), descriptionKey.get(), category));
  }

  private static Optional<LogicSection> parseLogic(
      JsonObject logicJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, RawQuestStep> steps = parseSteps(logicJson, id, filePath, issues);

    if (steps.isEmpty()) {
      issues.add(
          ContentIssue.of(IssueCode.EMPTY_STEPS, ContentType.QUEST, id, filePath, FIELD_STEPS));
    }

    CompletionPolicy completionPolicy = CompletionPolicy.ALL_STEPS;
    if (logicJson.has(FIELD_COMPLETION_POLICY)) {
      JsonElement policyElement = logicJson.get(FIELD_COMPLETION_POLICY);
      if (policyElement.isJsonPrimitive()) {
        completionPolicy =
            CompletionPolicy.fromKey(policyElement.getAsString())
                .orElse(CompletionPolicy.ALL_STEPS);
      }
    }

    boolean repeatable = false;
    if (logicJson.has(FIELD_REPEATABLE) && logicJson.get(FIELD_REPEATABLE).isJsonPrimitive()) {
      repeatable = logicJson.get(FIELD_REPEATABLE).getAsBoolean();
    }

    SyncScope syncScope = SyncScope.PLAYER;
    if (logicJson.has(FIELD_SYNC_SCOPE) && logicJson.get(FIELD_SYNC_SCOPE).isJsonPrimitive()) {
      syncScope =
          SyncScope.fromKey(logicJson.get(FIELD_SYNC_SCOPE).getAsString()).orElse(SyncScope.PLAYER);
    }

    Optional<Condition> visibilityCondition = parseVisibility(logicJson, id, filePath, issues);

    return Optional.of(
        new LogicSection(visibilityCondition, steps, completionPolicy, repeatable, syncScope));
  }

  private static Optional<Condition> parseVisibility(
      JsonObject logicJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    if (!logicJson.has(FIELD_VISIBILITY) || !logicJson.get(FIELD_VISIBILITY).isJsonObject()) {
      return Optional.empty();
    }

    JsonObject visibilityJson = logicJson.getAsJsonObject(FIELD_VISIBILITY);
    if (!visibilityJson.has(FIELD_WHEN)) {
      return Optional.empty();
    }

    return Optional.of(
        ConditionParser.parse(
            visibilityJson.get(FIELD_WHEN), ContentType.QUEST, id, filePath, issues));
  }

  private static Map<String, RawQuestStep> parseSteps(
      JsonObject logicJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, RawQuestStep> steps = new LinkedHashMap<>();

    if (!logicJson.has(FIELD_STEPS) || !logicJson.get(FIELD_STEPS).isJsonObject()) {
      return steps;
    }

    for (Map.Entry<String, JsonElement> stepEntry :
        logicJson.getAsJsonObject(FIELD_STEPS).entrySet()) {
      String stepId = stepEntry.getKey();

      if (!stepEntry.getValue().isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.QUEST,
                id,
                filePath,
                FIELD_STEPS + "." + stepId,
                Map.of("expected", "object")));
        continue;
      }

      JsonObject stepJson = stepEntry.getValue().getAsJsonObject();

      if (!stepJson.has(FIELD_TYPE) || !stepJson.get(FIELD_TYPE).isJsonPrimitive()) {
        issues.add(
            ContentIssue.of(
                IssueCode.MISSING_FIELD,
                ContentType.QUEST,
                id,
                filePath,
                FIELD_STEPS + "." + stepId + "." + FIELD_TYPE));
        continue;
      }

      String typeStr = stepJson.get(FIELD_TYPE).getAsString();
      try {
        steps.put(stepId, new RawQuestStep(stepId, new ResourceLocation(typeStr), stepJson));
      } catch (ResourceLocationException e) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_RESOURCE_LOCATION,
                ContentType.QUEST,
                id,
                filePath,
                FIELD_STEPS + "." + stepId + "." + FIELD_TYPE,
                Map.of("value", typeStr)));
      }
    }

    return steps;
  }
}
