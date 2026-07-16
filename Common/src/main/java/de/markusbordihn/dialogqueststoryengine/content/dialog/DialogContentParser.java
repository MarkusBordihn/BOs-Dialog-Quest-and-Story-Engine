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

package de.markusbordihn.dialogqueststoryengine.content.dialog;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.OptionalFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionParser;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionParser;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class DialogContentParser {

  static final String FIELD_START_NODE = "start_node";
  static final String FIELD_NODES = "nodes";
  static final String FIELD_SPEAKER_KEY = "speaker_key";
  static final String FIELD_TEXT_KEY = "text_key";
  static final String FIELD_CHOICES = "choices";
  static final String FIELD_LABEL_KEY = "label_key";
  static final String FIELD_CONDITIONS = "conditions";
  static final String FIELD_ACTIONS = "actions";
  static final String FIELD_NEXT = "next";
  static final String FIELD_CLOSE = "close";
  static final String FIELD_ONCE = "once";
  static final String FIELD_PRESENTATION = "presentation";
  static final String FIELD_PORTRAIT = "portrait";
  static final String FIELD_SCENE = "scene";
  static final String FIELD_MOOD = "mood";
  static final String FIELD_THEME = "theme";

  private DialogContentParser() {}

  public static ParseResult<DialogDefinition> parse(
      ResourceLocation id, String filePath, JsonObject jsonObject) {
    List<ContentIssue> issues = new ArrayList<>();

    Optional<Integer> schema =
        JsonFieldReader.readSchema(
            jsonObject,
            ContentType.DIALOG,
            ContentType.DIALOG.currentSchema(),
            id,
            filePath,
            issues);
    if (schema.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<String> startNode =
        JsonFieldReader.readString(
            jsonObject, FIELD_START_NODE, ContentType.DIALOG, id, filePath, issues);
    if (startNode.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<JsonObject> nodesObject =
        JsonFieldReader.readObject(
            jsonObject, FIELD_NODES, ContentType.DIALOG, id, filePath, issues);
    if (nodesObject.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Map<String, DialogNodeDefinition> nodes = parseNodes(nodesObject.get(), id, filePath, issues);

    if (!nodes.containsKey(startNode.get())) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_START_NODE,
              ContentType.DIALOG,
              id,
              filePath,
              FIELD_START_NODE,
              Map.of("start_node", startNode.get())));
      return ParseResult.failure(issues);
    }

    return ParseResult.success(
        new DialogDefinition(id, schema.get(), startNode.get(), nodes), issues);
  }

  private static Map<String, DialogNodeDefinition> parseNodes(
      JsonObject nodesObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, DialogNodeDefinition> nodes = new LinkedHashMap<>();

    for (Map.Entry<String, JsonElement> nodeEntry : nodesObject.entrySet()) {
      String nodeId = nodeEntry.getKey();

      if (nodes.containsKey(nodeId)) {
        issues.add(
            ContentIssue.of(
                IssueCode.DUPLICATE_NODE_ID,
                ContentType.DIALOG,
                id,
                filePath,
                FIELD_NODES + "." + nodeId));
        continue;
      }

      if (!nodeEntry.getValue().isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.DIALOG,
                id,
                filePath,
                FIELD_NODES + "." + nodeId,
                Map.of("expected", "object")));
        continue;
      }

      parseNode(nodeId, nodeEntry.getValue().getAsJsonObject(), id, filePath, issues)
          .ifPresent(node -> nodes.put(nodeId, node));
    }

    return nodes;
  }

  private static Optional<DialogNodeDefinition> parseNode(
      String nodeId,
      JsonObject nodeJson,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<String> speakerKey =
        JsonFieldReader.readString(
            nodeJson, FIELD_SPEAKER_KEY, ContentType.DIALOG, id, filePath, issues);
    Optional<String> textKey =
        JsonFieldReader.readString(
            nodeJson, FIELD_TEXT_KEY, ContentType.DIALOG, id, filePath, issues);

    if (speakerKey.isEmpty() || textKey.isEmpty()) {
      return Optional.empty();
    }

    List<DialogChoiceDefinition> choices = List.of();
    if (nodeJson.has(FIELD_CHOICES)) {
      if (!nodeJson.get(FIELD_CHOICES).isJsonArray()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.DIALOG,
                id,
                filePath,
                FIELD_NODES + "." + nodeId + "." + FIELD_CHOICES,
                Map.of("expected", "array")));
      } else {
        choices =
            parseChoices(nodeJson.getAsJsonArray(FIELD_CHOICES), nodeId, id, filePath, issues);
      }
    } else {
      issues.add(
          ContentIssue.of(
              IssueCode.EMPTY_CHOICES,
              ContentType.DIALOG,
              id,
              filePath,
              FIELD_NODES + "." + nodeId + "." + FIELD_CHOICES));
    }

    DialogPresentation presentation = parsePresentation(nodeJson, nodeId, id, filePath, issues);
    return Optional.of(
        new DialogNodeDefinition(nodeId, speakerKey.get(), textKey.get(), choices, presentation));
  }

  private static List<DialogChoiceDefinition> parseChoices(
      JsonArray choicesArray,
      String nodeId,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    List<DialogChoiceDefinition> choices = new ArrayList<>();

    for (int i = 0; i < choicesArray.size(); i++) {
      JsonElement element = choicesArray.get(i);
      if (!element.isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.DIALOG,
                id,
                filePath,
                FIELD_NODES + "." + nodeId + ".choices[" + i + "]",
                Map.of("expected", "object")));
        continue;
      }

      parseChoice(element.getAsJsonObject(), nodeId, i, id, filePath, issues)
          .ifPresent(choices::add);
    }

    return choices;
  }

  private static Optional<DialogChoiceDefinition> parseChoice(
      JsonObject choiceJson,
      String nodeId,
      int index,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<String> choiceId =
        JsonFieldReader.readString(choiceJson, "id", ContentType.DIALOG, id, filePath, issues);
    Optional<String> labelKey =
        JsonFieldReader.readString(
            choiceJson, FIELD_LABEL_KEY, ContentType.DIALOG, id, filePath, issues);

    if (choiceId.isEmpty() || labelKey.isEmpty()) {
      return Optional.empty();
    }

    ConditionGroup conditions =
        choiceJson.has(FIELD_CONDITIONS) && choiceJson.get(FIELD_CONDITIONS).isJsonArray()
            ? ConditionParser.parseGroup(
                choiceJson.getAsJsonArray(FIELD_CONDITIONS),
                ContentType.DIALOG,
                id,
                filePath,
                issues)
            : ConditionGroup.ALWAYS_TRUE;

    ActionList actions =
        choiceJson.has(FIELD_ACTIONS) && choiceJson.get(FIELD_ACTIONS).isJsonArray()
            ? ActionParser.parseList(
                choiceJson.getAsJsonArray(FIELD_ACTIONS), ContentType.DIALOG, id, filePath, issues)
            : ActionList.EMPTY;

    Optional<String> next =
        choiceJson.has(FIELD_NEXT) && choiceJson.get(FIELD_NEXT).isJsonPrimitive()
            ? Optional.of(choiceJson.get(FIELD_NEXT).getAsString())
            : Optional.empty();

    String choicePath = FIELD_NODES + "." + nodeId + ".choices[" + index + "]";
    boolean close =
        OptionalFieldReader.bool(
            choiceJson,
            FIELD_CLOSE,
            choicePath + "." + FIELD_CLOSE,
            false,
            ContentType.DIALOG,
            id,
            filePath,
            issues);
    boolean once =
        OptionalFieldReader.bool(
            choiceJson,
            FIELD_ONCE,
            choicePath + "." + FIELD_ONCE,
            false,
            ContentType.DIALOG,
            id,
            filePath,
            issues);

    if (next.isPresent() && close) {
      issues.add(
          ContentIssue.of(
              IssueCode.CHOICE_NEXT_CLOSE_CONFLICT, ContentType.DIALOG, id, filePath, choicePath));
      return Optional.empty();
    }

    return Optional.of(
        new DialogChoiceDefinition(
            choiceId.get(), labelKey.get(), conditions, actions, next, close, once));
  }

  private static DialogPresentation parsePresentation(
      JsonObject nodeJson,
      String nodeId,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!nodeJson.has(FIELD_PRESENTATION) || !nodeJson.get(FIELD_PRESENTATION).isJsonObject()) {
      return DialogPresentation.EMPTY;
    }

    JsonObject presentationJson = nodeJson.getAsJsonObject(FIELD_PRESENTATION);
    String path = FIELD_NODES + "." + nodeId + "." + FIELD_PRESENTATION + ".";
    return new DialogPresentation(
        OptionalFieldReader.resourceLocation(
            presentationJson,
            FIELD_PORTRAIT,
            path + FIELD_PORTRAIT,
            ContentType.DIALOG,
            id,
            filePath,
            issues),
        OptionalFieldReader.resourceLocation(
            presentationJson,
            FIELD_SCENE,
            path + FIELD_SCENE,
            ContentType.DIALOG,
            id,
            filePath,
            issues),
        OptionalFieldReader.string(
            presentationJson,
            FIELD_MOOD,
            path + FIELD_MOOD,
            ContentType.DIALOG,
            id,
            filePath,
            issues),
        OptionalFieldReader.resourceLocation(
            presentationJson,
            FIELD_THEME,
            path + FIELD_THEME,
            ContentType.DIALOG,
            id,
            filePath,
            issues));
  }
}
