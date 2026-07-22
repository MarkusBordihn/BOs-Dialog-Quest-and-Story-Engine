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

package de.markusbordihn.dialogqueststoryengine.logic.action;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ActionHandler;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ActionParser {

  private static final String KEY_TYPE = "type";

  private static final Map<String, ResourceLocation> SHORTHAND_KEYS;
  private static final Set<String> AMBIGUOUS_KEYS;

  static {
    Map<String, List<ActionType>> byShorthand = new LinkedHashMap<>();
    for (ActionType type : ActionType.values()) {
      for (String shorthand : type.shorthandKeys()) {
        byShorthand.computeIfAbsent(shorthand, key -> new ArrayList<>()).add(type);
      }
    }

    Map<String, ResourceLocation> shorthandKeys = new LinkedHashMap<>();
    Set<String> ambiguousKeys = new LinkedHashSet<>();
    byShorthand.forEach(
        (shorthand, owners) -> {
          if (owners.size() > 1) {
            ambiguousKeys.add(shorthand);
          } else {
            owners.get(0).typeId().ifPresent(typeId -> shorthandKeys.put(shorthand, typeId));
          }
        });

    SHORTHAND_KEYS = Map.copyOf(shorthandKeys);
    AMBIGUOUS_KEYS = Set.copyOf(ambiguousKeys);
  }

  private ActionParser() {}

  public static ActionList parseList(
      JsonArray array,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (array == null || array.isEmpty()) {
      return ActionList.EMPTY;
    }

    List<Action> actions = new ArrayList<>(array.size());
    for (int i = 0; i < array.size(); i++) {
      JsonElement element = array.get(i);
      if (!element.isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                contentType,
                contentId,
                filePath,
                "actions[" + i + "]",
                Map.of("expected", "object")));
        continue;
      }
      actions.add(parseSingle(element.getAsJsonObject(), contentType, contentId, filePath, issues));
    }

    return new ActionList(actions);
  }

  static Action parseSingle(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (jsonObject.has(KEY_TYPE) && jsonObject.get(KEY_TYPE).isJsonPrimitive()) {
      return parseByExplicitType(
          jsonObject.get(KEY_TYPE).getAsString(),
          jsonObject,
          contentType,
          contentId,
          filePath,
          issues);
    }

    return inferFromKeys(jsonObject, contentType, contentId, filePath, issues);
  }

  private static Action parseByExplicitType(
      String typeString,
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    ResourceLocation typeId = ResourceLocation.tryParse(typeString);
    Optional<ActionHandler> handler =
        typeId == null ? Optional.empty() : Registries.ACTIONS.get(typeId);
    if (handler.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_ACTION_TYPE,
              contentType,
              contentId,
              filePath,
              KEY_TYPE,
              Map.of("type", typeString)));
      return Action.NOOP;
    }

    return handler.get().parse(jsonObject, contentType, contentId, filePath, issues);
  }

  private static Action inferFromKeys(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (jsonObject.keySet().stream().anyMatch(AMBIGUOUS_KEYS::contains)) {
      issues.add(
          ContentIssue.of(
              IssueCode.AMBIGUOUS_ACTION_TYPE, contentType, contentId, filePath, "actions"));
      return Action.NOOP;
    }

    Map<String, ResourceLocation> matches = new LinkedHashMap<>();
    for (Map.Entry<String, ResourceLocation> shorthand : SHORTHAND_KEYS.entrySet()) {
      if (jsonObject.has(shorthand.getKey())) {
        matches.put(shorthand.getKey(), shorthand.getValue());
      }
    }

    if (matches.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_ACTION_TYPE, contentType, contentId, filePath, "actions"));
      return Action.NOOP;
    }

    if (matches.size() > 1) {
      issues.add(
          ContentIssue.of(
              IssueCode.AMBIGUOUS_ACTION_TYPE,
              contentType,
              contentId,
              filePath,
              "actions",
              Map.of("keys", String.join(", ", matches.keySet()))));
      return Action.NOOP;
    }

    ResourceLocation typeId = matches.values().iterator().next();
    return Registries.ACTIONS
        .get(typeId)
        .map(handler -> handler.parse(jsonObject, contentType, contentId, filePath, issues))
        .orElse(Action.NOOP);
  }
}
