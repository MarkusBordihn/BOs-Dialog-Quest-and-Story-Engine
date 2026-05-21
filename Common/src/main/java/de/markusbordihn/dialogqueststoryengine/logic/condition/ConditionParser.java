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

package de.markusbordihn.dialogqueststoryengine.logic.condition;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ConditionHandler;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class ConditionParser {

  private static final String KEY_ALL = "all";
  private static final String KEY_ANY = "any";
  private static final String KEY_TYPE = "type";

  private ConditionParser() {}

  public static Condition parse(
      JsonElement element,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!element.isJsonObject()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              "condition",
              Map.of("expected", "object")));
      return Condition.NEVER;
    }

    JsonObject json = element.getAsJsonObject();

    if (json.has(KEY_ALL) && json.get(KEY_ALL).isJsonArray()) {
      return parseGroup(GroupOperator.ALL, json.getAsJsonArray(KEY_ALL), contentType, id, filePath, issues);
    }
    if (json.has(KEY_ANY) && json.get(KEY_ANY).isJsonArray()) {
      return parseGroup(GroupOperator.ANY, json.getAsJsonArray(KEY_ANY), contentType, id, filePath, issues);
    }

    return parseLeaf(json, contentType, id, filePath, issues);
  }

  public static ConditionGroup parseGroup(
      JsonArray array,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    return parseGroup(GroupOperator.ALL, array, contentType, id, filePath, issues);
  }

  private static ConditionGroup parseGroup(
      GroupOperator operator,
      JsonArray array,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    List<Condition> members = new ArrayList<>(array.size());
    for (int i = 0; i < array.size(); i++) {
      members.add(parse(array.get(i), contentType, id, filePath, issues));
    }
    return new ConditionGroup(operator, members);
  }

  private static Condition parseLeaf(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has(KEY_TYPE) || !json.get(KEY_TYPE).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, KEY_TYPE));
      return Condition.NEVER;
    }

    String typeString = json.get(KEY_TYPE).getAsString();
    ResourceLocation typeId = ResourceLocation.tryParse(typeString);
    if (typeId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_CONDITION_TYPE,
              contentType,
              id,
              filePath,
              KEY_TYPE,
              Map.of("type", typeString)));
      return Condition.NEVER;
    }

    Optional<ConditionHandler> handler = Registries.CONDITIONS.get(typeId);
    if (handler.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_CONDITION_TYPE,
              contentType,
              id,
              filePath,
              KEY_TYPE,
              Map.of("type", typeString)));
      return Condition.NEVER;
    }

    return handler.get().parse(json, contentType, id, filePath, issues);
  }
}
