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

package de.markusbordihn.dialogqueststoryengine.content.interaction;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.content.InteractionBinding;
import de.markusbordihn.dialogqueststoryengine.data.interaction.content.InteractionBindingKind;
import de.markusbordihn.dialogqueststoryengine.data.interaction.content.InteractionDefinition;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionParser;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionParser;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ResourceLocationException;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public final class InteractionContentParser {

  static final String FIELD_EVENT = "event";
  static final String FIELD_BINDING = "binding";
  static final String FIELD_BINDING_KIND = "kind";
  static final String FIELD_TARGET_ID = "target_id";
  static final String FIELD_DIMENSION = "dimension";
  static final String FIELD_POS_X = "x";
  static final String FIELD_POS_Y = "y";
  static final String FIELD_POS_Z = "z";
  static final String FIELD_CONDITIONS = "conditions";
  static final String FIELD_ACTIONS = "actions";

  private InteractionContentParser() {}

  public static ParseResult<InteractionDefinition> parse(
      ResourceLocation id, String filePath, JsonObject jsonObject) {
    List<ContentIssue> issues = new ArrayList<>();

    Optional<Integer> schema =
        JsonFieldReader.readSchema(
            jsonObject,
            ContentType.INTERACTION,
            ContentType.INTERACTION.currentSchema(),
            id,
            filePath,
            issues);
    if (schema.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<String> eventKey =
        JsonFieldReader.readString(
            jsonObject, FIELD_EVENT, ContentType.INTERACTION, id, filePath, issues);
    if (eventKey.isEmpty()) {
      return ParseResult.failure(issues);
    }

    InteractionEventType event = InteractionEventType.fromJsonKey(eventKey.get());
    if (event == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_INTERACTION_EVENT,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_EVENT,
              Map.of("value", eventKey.get())));
      return ParseResult.failure(issues);
    }

    InteractionBinding binding = parseBinding(jsonObject, id, filePath, issues);
    if (binding == null) {
      return ParseResult.failure(issues);
    }

    ConditionGroup conditions = ConditionGroup.ALWAYS_TRUE;
    if (jsonObject.has(FIELD_CONDITIONS) && jsonObject.get(FIELD_CONDITIONS).isJsonArray()) {
      conditions =
          ConditionParser.parseGroup(
              jsonObject.getAsJsonArray(FIELD_CONDITIONS),
              ContentType.INTERACTION,
              id,
              filePath,
              issues);
    }

    ActionList actions = ActionList.EMPTY;
    if (jsonObject.has(FIELD_ACTIONS) && jsonObject.get(FIELD_ACTIONS).isJsonArray()) {
      actions =
          ActionParser.parseList(
              jsonObject.getAsJsonArray(FIELD_ACTIONS),
              ContentType.INTERACTION,
              id,
              filePath,
              issues);
    }

    return ParseResult.success(
        new InteractionDefinition(id, schema.get(), event, binding, conditions, actions), issues);
  }

  private static InteractionBinding parseBinding(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    if (!jsonObject.has(FIELD_BINDING) || !jsonObject.get(FIELD_BINDING).isJsonObject()) {
      return InteractionBinding.UnboundBinding.INSTANCE;
    }

    JsonObject bindingJson = jsonObject.getAsJsonObject(FIELD_BINDING);

    if (!bindingJson.has(FIELD_BINDING_KIND)
        || !bindingJson.get(FIELD_BINDING_KIND).isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING + "." + FIELD_BINDING_KIND));
      return null;
    }

    String kindStr = bindingJson.get(FIELD_BINDING_KIND).getAsString();
    Optional<InteractionBindingKind> kind = InteractionBindingKind.fromKey(kindStr);
    if (kind.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_BINDING_KIND,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING + "." + FIELD_BINDING_KIND,
              Map.of("value", kindStr)));
      return null;
    }

    if (kind.get() == InteractionBindingKind.ENTITY) {
      return parseEntityBinding(bindingJson, id, filePath, issues);
    }

    if (kind.get() == InteractionBindingKind.BLOCK) {
      return parseBlockBinding(bindingJson, id, filePath, issues);
    }

    if (kind.get() == InteractionBindingKind.UNBOUND) {
      return InteractionBinding.UnboundBinding.INSTANCE;
    }

    issues.add(
        ContentIssue.of(
            IssueCode.UNKNOWN_BINDING_KIND,
            ContentType.INTERACTION,
            id,
            filePath,
            FIELD_BINDING + "." + FIELD_BINDING_KIND,
            Map.of("value", kindStr, "note", "not yet supported in V1")));

    return null;
  }

  private static InteractionBinding.EntityBinding parseEntityBinding(
      JsonObject bindingJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Optional<String> targetIdStr =
        JsonFieldReader.readString(
            bindingJson, FIELD_TARGET_ID, ContentType.INTERACTION, id, filePath, issues);
    Optional<String> dimensionStr =
        JsonFieldReader.readString(
            bindingJson, FIELD_DIMENSION, ContentType.INTERACTION, id, filePath, issues);

    if (targetIdStr.isEmpty() || dimensionStr.isEmpty()) {
      return null;
    }

    UUID targetId;
    try {
      targetId = UUID.fromString(targetIdStr.get());
    } catch (IllegalArgumentException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_UUID,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING + "." + FIELD_TARGET_ID,
              Map.of("value", targetIdStr.get())));
      return null;
    }

    ResourceLocation dimension;
    try {
      dimension = new ResourceLocation(dimensionStr.get());
    } catch (ResourceLocationException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING + "." + FIELD_DIMENSION,
              Map.of("value", dimensionStr.get())));
      return null;
    }

    return new InteractionBinding.EntityBinding(targetId, dimension);
  }

  private static InteractionBinding.BlockBinding parseBlockBinding(
      JsonObject bindingJson, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Optional<String> dimensionStr =
        JsonFieldReader.readString(
            bindingJson, FIELD_DIMENSION, ContentType.INTERACTION, id, filePath, issues);

    if (dimensionStr.isEmpty()) {
      return null;
    }

    ResourceLocation dimension;
    try {
      dimension = new ResourceLocation(dimensionStr.get());
    } catch (ResourceLocationException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING + "." + FIELD_DIMENSION,
              Map.of("value", dimensionStr.get())));
      return null;
    }

    if (!bindingJson.has(FIELD_POS_X)
        || !bindingJson.has(FIELD_POS_Y)
        || !bindingJson.has(FIELD_POS_Z)) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_BLOCK_POS,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING,
              Map.of("missing", "x/y/z")));
      return null;
    }

    try {
      int x = bindingJson.get(FIELD_POS_X).getAsInt();
      int y = bindingJson.get(FIELD_POS_Y).getAsInt();
      int z = bindingJson.get(FIELD_POS_Z).getAsInt();
      return new InteractionBinding.BlockBinding(new BlockPos(x, y, z), dimension);
    } catch (Exception e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_BLOCK_POS,
              ContentType.INTERACTION,
              id,
              filePath,
              FIELD_BINDING,
              Map.of("reason", "non-integer values")));
      return null;
    }
  }
}
