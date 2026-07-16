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
import com.google.gson.JsonPrimitive;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

final class FactConditionFields {

  static final String FIELD_SCOPE = "scope";
  static final String FIELD_FACT = "fact";
  static final String FIELD_VALUE = "value";

  private FactConditionFields() {}

  static FactScope parseScope(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has(FIELD_SCOPE) || !json.get(FIELD_SCOPE).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_SCOPE));
      return null;
    }

    FactScope scope =
        FactScope.fromName(json.get(FIELD_SCOPE).getAsString().toUpperCase(Locale.ROOT));
    if (scope == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              FIELD_SCOPE,
              Map.of("value", json.get(FIELD_SCOPE).getAsString())));
    }
    return scope;
  }

  static String parseFact(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has(FIELD_FACT) || !json.get(FIELD_FACT).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_FACT));
      return null;
    }

    String fact = json.get(FIELD_FACT).getAsString();
    if (fact.isBlank()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_FACT));
      return null;
    }
    return fact;
  }

  static FactValue parseValue(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has(FIELD_VALUE) || !json.get(FIELD_VALUE).isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_VALUE));
      return null;
    }

    JsonPrimitive primitive = json.get(FIELD_VALUE).getAsJsonPrimitive();
    if (primitive.isBoolean()) {
      return FactValue.of(primitive.getAsBoolean());
    }
    if (primitive.isNumber()) {
      double numeric = primitive.getAsDouble();
      long asLong = (long) numeric;
      return numeric == asLong ? FactValue.of(asLong) : FactValue.of(numeric);
    }
    return FactValue.of(primitive.getAsString());
  }
}
