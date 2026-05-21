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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record FactEqualsCondition(FactScope scope, String fact, FactValue expectedValue)
    implements Condition {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "fact_equals");

  public static Condition parse(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    FactScope scope = parseScope(json, contentType, id, filePath, issues);
    if (scope == null) {
      return Condition.NEVER;
    }

    String fact = parseStringField(json, "fact", contentType, id, filePath, issues);
    if (fact == null) {
      return Condition.NEVER;
    }

    FactValue expectedValue = parseValue(json, contentType, id, filePath, issues);
    if (expectedValue == null) {
      return Condition.NEVER;
    }

    return new FactEqualsCondition(scope, fact, expectedValue);
  }

  private static FactScope parseScope(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has("scope") || !json.get("scope").isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, "scope"));
      return null;
    }
    String scopeString = json.get("scope").getAsString().toUpperCase(Locale.ROOT);
    FactScope scope = FactScope.fromName(scopeString);
    if (scope == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              "scope",
              Map.of("value", json.get("scope").getAsString())));
      return null;
    }
    return scope;
  }

  private static String parseStringField(
      JsonObject json,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has(field) || !json.get(field).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, field));
      return null;
    }
    String value = json.get(field).getAsString();
    if (value.isEmpty()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, field));
      return null;
    }
    return value;
  }

  private static FactValue parseValue(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has("value")) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, "value"));
      return null;
    }
    JsonElement element = json.get("value");
    if (!element.isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              "value",
              Map.of("expected", "boolean, number, or string")));
      return null;
    }
    return factValueFromPrimitive(element.getAsJsonPrimitive());
  }

  private static FactValue factValueFromPrimitive(JsonPrimitive primitive) {
    if (primitive.isBoolean()) {
      return FactValue.of(primitive.getAsBoolean());
    }
    if (primitive.isNumber()) {
      double d = primitive.getAsDouble();
      long l = (long) d;
      return d == l ? FactValue.of(l) : FactValue.of(d);
    }
    return FactValue.of(primitive.getAsString());
  }

  @Override
  public boolean evaluate(ConditionContext conditionContext) {
    FactValue actual = conditionContext.playerState().getFact(this.scope, this.fact);
    if (actual == null) {
      return false;
    }

    return actual.equals(this.expectedValue);
  }
}
