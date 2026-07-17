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

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.content.NarrativeMetadataParser;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.OptionalFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.json.RegistryReferenceValidator;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.PrerequisiteMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardClaimMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionParser;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionParser;
import de.markusbordihn.dialogqueststoryengine.utils.DependencyGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;

public final class QuestContentParser {

  static final String FIELD_DISPLAY = "display";
  static final String FIELD_LOGIC = "logic";
  static final String FIELD_ON_COMPLETE = "on_complete";
  static final String FIELD_REWARDS = "rewards";
  static final String FIELD_TITLE_KEY = "title_key";
  static final String FIELD_DESCRIPTION_KEY = "description_key";
  static final String FIELD_CATEGORY = "category";
  static final String FIELD_CATEGORY_KEY = "category_key";
  static final String FIELD_ICON = "icon";
  static final String FIELD_SORT_ORDER = "sort_order";
  static final String FIELD_VISIBILITY = "visibility";
  static final String FIELD_WHEN = "when";
  static final String FIELD_PREREQUISITES = "prerequisites";
  static final String FIELD_MODE = "mode";
  static final String FIELD_QUESTS = "quests";
  static final String FIELD_RESTART_AFTER_FAILURE = "restart_after_failure";
  static final String FIELD_STEPS = "steps";
  static final String FIELD_REQUIRES = "requires";
  static final String FIELD_COMPLETION_POLICY = "completion_policy";
  static final String FIELD_TYPE = "type";
  static final String FIELD_CLAIM_MODE = "claim_mode";
  static final String FIELD_ENTRIES = "entries";
  static final String FIELD_ITEM = "item";
  static final String FIELD_COUNT = "count";
  static final String FIELD_AMOUNT = "amount";

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

    NarrativeMetadata narrative =
        NarrativeMetadataParser.parse(jsonObject, ContentType.QUEST, id, filePath, issues);

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

    ActionList onComplete = ActionList.EMPTY;
    if (jsonObject.has(FIELD_ON_COMPLETE) && jsonObject.get(FIELD_ON_COMPLETE).isJsonArray()) {
      onComplete =
          ActionParser.parseList(
              jsonObject.getAsJsonArray(FIELD_ON_COMPLETE),
              ContentType.QUEST,
              id,
              filePath,
              issues);
    }

    RewardSection rewards = parseRewards(jsonObject, id, filePath, issues);

    return ParseResult.success(
        new QuestDefinition(
            id, schema.get(), narrative, display.get(), logic.get(), onComplete, rewards),
        issues);
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

    Optional<ResourceLocation> category =
        OptionalFieldReader.resourceLocation(
            displayJson,
            FIELD_CATEGORY,
            FIELD_DISPLAY + "." + FIELD_CATEGORY,
            ContentType.QUEST,
            id,
            filePath,
            issues);
    Optional<String> categoryKey =
        OptionalFieldReader.string(
            displayJson,
            FIELD_CATEGORY_KEY,
            FIELD_DISPLAY + "." + FIELD_CATEGORY_KEY,
            ContentType.QUEST,
            id,
            filePath,
            issues);
    Optional<ResourceLocation> icon =
        OptionalFieldReader.resourceLocation(
            displayJson,
            FIELD_ICON,
            FIELD_DISPLAY + "." + FIELD_ICON,
            ContentType.QUEST,
            id,
            filePath,
            issues);
    int sortOrder =
        OptionalFieldReader.integer(
            displayJson,
            FIELD_SORT_ORDER,
            FIELD_DISPLAY + "." + FIELD_SORT_ORDER,
            ContentType.QUEST,
            id,
            filePath,
            issues);

    return Optional.of(
        new DisplaySection(
            titleKey.get(), descriptionKey.get(), category, categoryKey, icon, sortOrder));
  }

  private static Optional<LogicSection> parseLogic(
      JsonObject logicJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, RawQuestStep> steps = parseSteps(logicJson, id, filePath, issues);

    if (steps.isEmpty()) {
      issues.add(
          ContentIssue.of(IssueCode.EMPTY_STEPS, ContentType.QUEST, id, filePath, FIELD_STEPS));
    }

    if (!validateStepPrerequisites(steps, id, filePath, issues)) {
      return Optional.empty();
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

    boolean restartAfterFailure =
        OptionalFieldReader.bool(
            logicJson,
            FIELD_RESTART_AFTER_FAILURE,
            FIELD_LOGIC + "." + FIELD_RESTART_AFTER_FAILURE,
            true,
            ContentType.QUEST,
            id,
            filePath,
            issues);

    QuestPrerequisites prerequisites = parsePrerequisites(logicJson, id, filePath, issues);
    Optional<Condition> visibilityCondition = parseVisibility(logicJson, id, filePath, issues);

    return Optional.of(
        new LogicSection(
            visibilityCondition, prerequisites, steps, completionPolicy, restartAfterFailure));
  }

  private static QuestPrerequisites parsePrerequisites(
      JsonObject logicJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    if (!logicJson.has(FIELD_PREREQUISITES) || !logicJson.get(FIELD_PREREQUISITES).isJsonObject()) {
      return QuestPrerequisites.NONE;
    }

    JsonObject prerequisitesJson = logicJson.getAsJsonObject(FIELD_PREREQUISITES);
    PrerequisiteMode mode = PrerequisiteMode.ALL;
    if (prerequisitesJson.has(FIELD_MODE) && prerequisitesJson.get(FIELD_MODE).isJsonPrimitive()) {
      mode =
          PrerequisiteMode.fromKey(prerequisitesJson.get(FIELD_MODE).getAsString())
              .orElse(PrerequisiteMode.ALL);
    }

    List<ResourceLocation> quests = new ArrayList<>();
    String questsPath = FIELD_LOGIC + "." + FIELD_PREREQUISITES + "." + FIELD_QUESTS;
    if (prerequisitesJson.has(FIELD_QUESTS) && prerequisitesJson.get(FIELD_QUESTS).isJsonArray()) {
      for (JsonElement entry : prerequisitesJson.getAsJsonArray(FIELD_QUESTS)) {
        if (!entry.isJsonPrimitive() || !entry.getAsJsonPrimitive().isString()) {
          issues.add(
              ContentIssue.of(
                  IssueCode.INVALID_FIELD_TYPE,
                  ContentType.QUEST,
                  id,
                  filePath,
                  questsPath,
                  Map.of("expected", "string")));
          continue;
        }
        try {
          quests.add(new ResourceLocation(entry.getAsString()));
        } catch (ResourceLocationException e) {
          issues.add(
              ContentIssue.of(
                  IssueCode.INVALID_RESOURCE_LOCATION,
                  ContentType.QUEST,
                  id,
                  filePath,
                  questsPath,
                  Map.of("value", entry.getAsString())));
        }
      }
    }

    return new QuestPrerequisites(mode, quests);
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

  private static RewardSection parseRewards(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    if (!jsonObject.has(FIELD_REWARDS)) {
      return RewardSection.EMPTY;
    }

    if (!jsonObject.get(FIELD_REWARDS).isJsonObject()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              ContentType.QUEST,
              id,
              filePath,
              FIELD_REWARDS,
              Map.of("expected", "object")));
      return RewardSection.EMPTY;
    }

    JsonObject rewardsJson = jsonObject.getAsJsonObject(FIELD_REWARDS);
    RewardClaimMode claimMode = RewardClaimMode.AUTOMATIC;
    if (rewardsJson.has(FIELD_CLAIM_MODE) && rewardsJson.get(FIELD_CLAIM_MODE).isJsonPrimitive()) {
      claimMode =
          RewardClaimMode.fromKey(rewardsJson.get(FIELD_CLAIM_MODE).getAsString())
              .orElse(RewardClaimMode.AUTOMATIC);
    }

    Optional<String> titleKey =
        OptionalFieldReader.string(
            rewardsJson,
            FIELD_TITLE_KEY,
            FIELD_REWARDS + "." + FIELD_TITLE_KEY,
            ContentType.QUEST,
            id,
            filePath,
            issues);
    Optional<String> descriptionKey =
        OptionalFieldReader.string(
            rewardsJson,
            FIELD_DESCRIPTION_KEY,
            FIELD_REWARDS + "." + FIELD_DESCRIPTION_KEY,
            ContentType.QUEST,
            id,
            filePath,
            issues);

    List<RewardEntry> entries = parseRewardEntries(rewardsJson, id, filePath, issues);
    return new RewardSection(claimMode, titleKey, descriptionKey, entries);
  }

  private static List<RewardEntry> parseRewardEntries(
      JsonObject rewardsJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    List<RewardEntry> entries = new ArrayList<>();
    if (!rewardsJson.has(FIELD_ENTRIES) || !rewardsJson.get(FIELD_ENTRIES).isJsonArray()) {
      return entries;
    }

    JsonArray array = rewardsJson.getAsJsonArray(FIELD_ENTRIES);
    for (int i = 0; i < array.size(); i++) {
      String entryPath = FIELD_REWARDS + "." + FIELD_ENTRIES + "[" + i + "]";
      JsonElement element = array.get(i);
      if (!element.isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.QUEST,
                id,
                filePath,
                entryPath,
                Map.of("expected", "object")));
        continue;
      }
      parseRewardEntry(element.getAsJsonObject(), entryPath, id, filePath, issues)
          .ifPresent(entries::add);
    }
    return entries;
  }

  private static Optional<RewardEntry> parseRewardEntry(
      JsonObject entryJson,
      String entryPath,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!entryJson.has(FIELD_TYPE) || !entryJson.get(FIELD_TYPE).isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD,
              ContentType.QUEST,
              id,
              filePath,
              entryPath + "." + FIELD_TYPE));
      return Optional.empty();
    }

    String type = entryJson.get(FIELD_TYPE).getAsString();
    if (RewardEntry.Item.TYPE_ID.toString().equals(type)) {
      return parseItemReward(entryJson, entryPath, id, filePath, issues);
    }
    if (RewardEntry.Experience.TYPE_ID.toString().equals(type)) {
      return parseExperienceReward(entryJson, entryPath, id, filePath, issues);
    }

    issues.add(
        ContentIssue.of(
            IssueCode.UNKNOWN_REWARD_TYPE,
            ContentType.QUEST,
            id,
            filePath,
            entryPath + "." + FIELD_TYPE,
            Map.of("type", type)));
    return Optional.empty();
  }

  private static Optional<RewardEntry> parseItemReward(
      JsonObject entryJson,
      String entryPath,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<ResourceLocation> item =
        JsonFieldReader.readResourceLocation(
            entryJson, FIELD_ITEM, ContentType.QUEST, id, filePath, issues);
    if (item.isEmpty()) {
      return Optional.empty();
    }

    if (!RegistryReferenceValidator.requireRegistered(
        BuiltInRegistries.ITEM,
        item.get(),
        ContentType.QUEST,
        id,
        filePath,
        entryPath + "." + FIELD_ITEM,
        issues)) {
      return Optional.empty();
    }

    int count =
        entryJson.has(FIELD_COUNT)
            ? OptionalFieldReader.integer(
                entryJson,
                FIELD_COUNT,
                entryPath + "." + FIELD_COUNT,
                ContentType.QUEST,
                id,
                filePath,
                issues)
            : 1;
    if (count <= 0) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_REWARD_AMOUNT,
              ContentType.QUEST,
              id,
              filePath,
              entryPath + "." + FIELD_COUNT,
              Map.of("value", String.valueOf(count))));
      return Optional.empty();
    }

    return Optional.of(new RewardEntry.Item(item.get(), count));
  }

  private static Optional<RewardEntry> parseExperienceReward(
      JsonObject entryJson,
      String entryPath,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<Integer> amount =
        JsonFieldReader.readInt(entryJson, FIELD_AMOUNT, ContentType.QUEST, id, filePath, issues);
    if (amount.isEmpty()) {
      return Optional.empty();
    }

    if (amount.get() <= 0) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_REWARD_AMOUNT,
              ContentType.QUEST,
              id,
              filePath,
              entryPath + "." + FIELD_AMOUNT,
              Map.of("value", String.valueOf(amount.get()))));
      return Optional.empty();
    }

    return Optional.of(new RewardEntry.Experience(amount.get()));
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
        steps.put(
            stepId,
            new RawQuestStep(
                stepId,
                new ResourceLocation(typeStr),
                parseStepDescriptionKey(stepJson, stepId, id, filePath, issues),
                parseStepRequires(stepJson, stepId, id, filePath, issues),
                stepJson));
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

  private static Optional<String> parseStepDescriptionKey(
      JsonObject stepJson,
      String stepId,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!stepJson.has(FIELD_DESCRIPTION_KEY)) {
      return Optional.empty();
    }

    JsonElement element = stepJson.get(FIELD_DESCRIPTION_KEY);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              ContentType.QUEST,
              id,
              filePath,
              FIELD_STEPS + "." + stepId + "." + FIELD_DESCRIPTION_KEY,
              Map.of("expected", "string")));
      return Optional.empty();
    }

    return Optional.of(element.getAsString());
  }

  private static List<String> parseStepRequires(
      JsonObject stepJson,
      String stepId,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    List<String> requires = new ArrayList<>();
    if (!stepJson.has(FIELD_REQUIRES)) {
      return requires;
    }

    JsonElement element = stepJson.get(FIELD_REQUIRES);
    if (!element.isJsonArray()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              ContentType.QUEST,
              id,
              filePath,
              FIELD_STEPS + "." + stepId + "." + FIELD_REQUIRES,
              Map.of("expected", "array")));
      return requires;
    }

    for (JsonElement entry : element.getAsJsonArray()) {
      if (entry.isJsonPrimitive() && entry.getAsJsonPrimitive().isString()) {
        requires.add(entry.getAsString());
      } else {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.QUEST,
                id,
                filePath,
                FIELD_STEPS + "." + stepId + "." + FIELD_REQUIRES,
                Map.of("expected", "string")));
      }
    }
    return requires;
  }

  private static boolean validateStepPrerequisites(
      Map<String, RawQuestStep> steps,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    boolean valid = true;
    for (RawQuestStep step : steps.values()) {
      String path = FIELD_STEPS + "." + step.id() + "." + FIELD_REQUIRES;
      Set<String> seen = new HashSet<>();
      for (String required : step.requires()) {
        if (!seen.add(required)) {
          issues.add(
              ContentIssue.of(
                  IssueCode.DUPLICATE_STEP_PREREQUISITE,
                  ContentType.QUEST,
                  id,
                  filePath,
                  path,
                  Map.of("step", required)));
          valid = false;
        } else if (required.equals(step.id())) {
          issues.add(
              ContentIssue.of(
                  IssueCode.SELF_STEP_PREREQUISITE,
                  ContentType.QUEST,
                  id,
                  filePath,
                  path,
                  Map.of("step", required)));
          valid = false;
        } else if (!steps.containsKey(required)) {
          issues.add(
              ContentIssue.of(
                  IssueCode.UNKNOWN_STEP_PREREQUISITE,
                  ContentType.QUEST,
                  id,
                  filePath,
                  path,
                  Map.of("step", required)));
          valid = false;
        }
      }
    }

    return validateNoPrerequisiteCycle(steps, id, filePath, issues) && valid;
  }

  private static boolean validateNoPrerequisiteCycle(
      Map<String, RawQuestStep> steps,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Map<String, List<String>> edges = new LinkedHashMap<>();
    for (RawQuestStep step : steps.values()) {
      edges.put(
          step.id(),
          step.requires().stream()
              .distinct()
              .filter(required -> !required.equals(step.id()) && steps.containsKey(required))
              .toList());
    }

    Set<String> cycleSteps = DependencyGraph.findCycleNodes(edges);
    if (cycleSteps.isEmpty()) {
      return true;
    }

    issues.add(
        ContentIssue.of(
            IssueCode.STEP_PREREQUISITE_CYCLE,
            ContentType.QUEST,
            id,
            filePath,
            FIELD_STEPS,
            Map.of("steps", String.join(", ", cycleSteps))));
    return false;
  }
}
