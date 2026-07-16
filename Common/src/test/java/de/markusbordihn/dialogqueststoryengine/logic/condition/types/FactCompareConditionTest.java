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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class FactCompareConditionTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");

  private static Condition parse(String raw, List<ContentIssue> issues) {
    JsonObject json = GSON.fromJson(raw, JsonObject.class);
    return FactCompareCondition.parse(json, ContentType.DIALOG, TEST_ID, "test.json", issues);
  }

  private static ConditionContext contextWith(String fact, FactValue value) {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    if (value != null) {
      playerState.setFact(FactScope.PLAYER, fact, value);
    }
    return ConditionContext.ofTest(playerState);
  }

  @Test
  void greaterOrEqualComparesNumerically() {
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition =
        parse(
            "{\"scope\":\"player\",\"fact\":\"trust\",\"operator\":\"greater_or_equal\",\"value\":3}",
            issues);

    assertTrue(issues.isEmpty());
    assertTrue(condition.evaluate(contextWith("trust", FactValue.of(3L))));
    assertTrue(condition.evaluate(contextWith("trust", FactValue.of(5L))));
    assertFalse(condition.evaluate(contextWith("trust", FactValue.of(2L))));
  }

  @Test
  void lessThanIsFalseForEqualValue() {
    Condition condition =
        parse(
            "{\"scope\":\"player\",\"fact\":\"trust\",\"operator\":\"less_than\",\"value\":3}",
            new ArrayList<>());

    assertFalse(condition.evaluate(contextWith("trust", FactValue.of(3L))));
    assertTrue(condition.evaluate(contextWith("trust", FactValue.of(1L))));
  }

  @Test
  void notEqualsWorksOnStrings() {
    Condition condition =
        parse(
            "{\"scope\":\"player\",\"fact\":\"tier\",\"operator\":\"not_equals\",\"value\":\"gold\"}",
            new ArrayList<>());

    assertTrue(condition.evaluate(contextWith("tier", FactValue.of("silver"))));
    assertFalse(condition.evaluate(contextWith("tier", FactValue.of("gold"))));
  }

  @Test
  void orderingOnNonNumericValueEvaluatesFalseAndWarns() {
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition =
        parse(
            "{\"scope\":\"player\",\"fact\":\"tier\",\"operator\":\"greater_than\",\"value\":\"gold\"}",
            issues);

    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.CONDITION_FACT_TYPE_MISMATCH));
    assertFalse(condition.evaluate(contextWith("tier", FactValue.of("silver"))));
  }

  @Test
  void missingFactEvaluatesFalse() {
    Condition condition =
        parse(
            "{\"scope\":\"player\",\"fact\":\"trust\",\"operator\":\"equals\",\"value\":3}",
            new ArrayList<>());

    assertFalse(condition.evaluate(contextWith("trust", null)));
  }

  @Test
  void unknownOperatorIsRejected() {
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition =
        parse(
            "{\"scope\":\"player\",\"fact\":\"trust\",\"operator\":\"approximately\",\"value\":3}",
            issues);

    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_FIELD_TYPE));
    assertFalse(condition.evaluate(contextWith("trust", FactValue.of(3L))));
  }
}
