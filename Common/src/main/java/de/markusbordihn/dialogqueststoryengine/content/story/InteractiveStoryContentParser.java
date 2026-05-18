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

package de.markusbordihn.dialogqueststoryengine.content.story;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.json.RawAction;
import de.markusbordihn.dialogqueststoryengine.data.json.RawCondition;
import de.markusbordihn.dialogqueststoryengine.data.json.RawJsonListReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public final class InteractiveStoryContentParser {

  static final String FIELD_DISPLAY = "display";
  static final String FIELD_MODE = "mode";
  static final String FIELD_ON_OPEN = "on_open";
  static final String FIELD_CHOICES = "choices";
  static final String FIELD_LABEL_KEY = "label_key";
  static final String FIELD_CONDITIONS = "conditions";
  static final String FIELD_ACTIONS = "actions";

  private InteractiveStoryContentParser() {}

  public static ParseResult<InteractiveStoryDefinition> parse(
      ResourceLocation id, String filePath, JsonObject jsonObject) {
    List<ContentIssue> issues = new ArrayList<>();

    Optional<Integer> schema =
        JsonFieldReader.readSchema(
            jsonObject,
            ContentType.INTERACTIVE_STORY,
            ContentType.INTERACTIVE_STORY.currentSchema(),
            id,
            filePath,
            issues);
    if (schema.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<String> displayStr =
        JsonFieldReader.readString(
            jsonObject, FIELD_DISPLAY, ContentType.INTERACTIVE_STORY, id, filePath, issues);
    if (displayStr.isEmpty()) {
      return ParseResult.failure(issues);
    }

    ResourceLocation displayId;
    try {
      displayId = new ResourceLocation(displayStr.get());
    } catch (ResourceLocationException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              ContentType.INTERACTIVE_STORY,
              id,
              filePath,
              FIELD_DISPLAY,
              Map.of("value", displayStr.get())));
      return ParseResult.failure(issues);
    }

    Optional<String> modeStr =
        JsonFieldReader.readString(
            jsonObject, FIELD_MODE, ContentType.INTERACTIVE_STORY, id, filePath, issues);
    if (modeStr.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<InteractiveStoryMode> mode = InteractiveStoryMode.fromKey(modeStr.get());
    if (mode.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_STORY_MODE,
              ContentType.INTERACTIVE_STORY,
              id,
              filePath,
              FIELD_MODE,
              Map.of("value", modeStr.get())));
      return ParseResult.failure(issues);
    }

    List<RawAction> onOpen = List.of();
    if (jsonObject.has(FIELD_ON_OPEN) && jsonObject.get(FIELD_ON_OPEN).isJsonArray()) {
      onOpen =
          RawJsonListReader.readActions(
              jsonObject.getAsJsonArray(FIELD_ON_OPEN),
              ContentType.INTERACTIVE_STORY,
              id,
              filePath,
              issues);
    }

    List<InteractiveStoryChoice> choices = List.of();
    if (jsonObject.has(FIELD_CHOICES) && jsonObject.get(FIELD_CHOICES).isJsonArray()) {
      choices = parseChoices(jsonObject.getAsJsonArray(FIELD_CHOICES), id, filePath, issues);
    }

    return ParseResult.success(
        new InteractiveStoryDefinition(id, schema.get(), displayId, mode.get(), onOpen, choices),
        issues);
  }

  private static List<InteractiveStoryChoice> parseChoices(
      JsonArray choicesArray, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    List<InteractiveStoryChoice> choices = new ArrayList<>();

    for (int i = 0; i < choicesArray.size(); i++) {
      if (!choicesArray.get(i).isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.INTERACTIVE_STORY,
                id,
                filePath,
                FIELD_CHOICES + "[" + i + "]",
                Map.of("expected", "object")));
        continue;
      }

      JsonObject choiceJson = choicesArray.get(i).getAsJsonObject();

      Optional<String> choiceId =
          JsonFieldReader.readString(
              choiceJson, "id", ContentType.INTERACTIVE_STORY, id, filePath, issues);
      Optional<String> labelKey =
          JsonFieldReader.readString(
              choiceJson, FIELD_LABEL_KEY, ContentType.INTERACTIVE_STORY, id, filePath, issues);

      if (choiceId.isEmpty() || labelKey.isEmpty()) {
        continue;
      }

      List<RawCondition> conditions =
          choiceJson.has(FIELD_CONDITIONS) && choiceJson.get(FIELD_CONDITIONS).isJsonArray()
              ? RawJsonListReader.readConditions(
                  choiceJson.getAsJsonArray(FIELD_CONDITIONS),
                  ContentType.INTERACTIVE_STORY,
                  id,
                  filePath,
                  issues)
              : List.of();

      List<RawAction> actions =
          choiceJson.has(FIELD_ACTIONS) && choiceJson.get(FIELD_ACTIONS).isJsonArray()
              ? RawJsonListReader.readActions(
                  choiceJson.getAsJsonArray(FIELD_ACTIONS),
                  ContentType.INTERACTIVE_STORY,
                  id,
                  filePath,
                  issues)
              : List.of();

      choices.add(new InteractiveStoryChoice(choiceId.get(), labelKey.get(), conditions, actions));
    }

    return choices;
  }
}
