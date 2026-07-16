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
import de.markusbordihn.dialogqueststoryengine.logic.condition.ComparisonOperator;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import java.util.List;
import java.util.Map;
import java.util.OptionalDouble;
import net.minecraft.resources.ResourceLocation;

public record FactCompareCondition(
    FactScope scope, String fact, ComparisonOperator operator, FactValue value)
    implements Condition {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "fact_compare");

  private static final String FIELD_OPERATOR = "operator";

  public static Condition parse(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    FactScope scope = FactConditionFields.parseScope(json, contentType, id, filePath, issues);
    String fact = FactConditionFields.parseFact(json, contentType, id, filePath, issues);
    FactValue value = FactConditionFields.parseValue(json, contentType, id, filePath, issues);

    if (!json.has(FIELD_OPERATOR) || !json.get(FIELD_OPERATOR).isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, FIELD_OPERATOR));
      return Condition.NEVER;
    }

    ComparisonOperator operator =
        ComparisonOperator.fromKey(json.get(FIELD_OPERATOR).getAsString()).orElse(null);
    if (operator == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              FIELD_OPERATOR,
              Map.of("value", json.get(FIELD_OPERATOR).getAsString())));
      return Condition.NEVER;
    }

    if (scope == null || fact == null || value == null) {
      return Condition.NEVER;
    }

    if (operator.requiresNumericOrder() && value.numericValue().isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.CONDITION_FACT_TYPE_MISMATCH,
              contentType,
              id,
              filePath,
              FIELD_OPERATOR,
              Map.of("operator", operator.name(), "value_type", value.type().name())));
    }

    return new FactCompareCondition(scope, fact, operator, value);
  }

  @Override
  public boolean evaluate(ConditionContext conditionContext) {
    FactValue actual = conditionContext.playerState().getFact(this.scope, this.fact);
    if (actual == null) {
      return false;
    }

    OptionalDouble actualNumber = actual.numericValue();
    OptionalDouble expectedNumber = this.value.numericValue();
    if (actualNumber.isPresent() && expectedNumber.isPresent()) {
      return this.operator.compareNumeric(actualNumber.getAsDouble(), expectedNumber.getAsDouble());
    }

    return switch (this.operator) {
      case EQUALS -> actual.equals(this.value);
      case NOT_EQUALS -> !actual.equals(this.value);
      default -> false;
    };
  }
}
