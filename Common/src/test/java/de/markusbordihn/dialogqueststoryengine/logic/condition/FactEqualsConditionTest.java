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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class FactEqualsConditionTest {

  private static final ResourceLocation CONTENT_ID =
      new ResourceLocation("test", "fact_equals_test");

  private static ConditionContext contextWithFact(FactScope scope, String key, FactValue value) {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.setFact(scope, key, value);
    return ConditionContext.ofTest(playerState);
  }

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  @Test
  void evaluate_matchingBooleanFact_returnsTrue() {
    FactEqualsCondition condition =
        new FactEqualsCondition(FactScope.PLAYER, "has_key", FactValue.of(true));
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "has_key", FactValue.of(true));
    assertTrue(condition.evaluate(ctx));
  }

  @Test
  void evaluate_mismatchedBooleanFact_returnsFalse() {
    FactEqualsCondition condition =
        new FactEqualsCondition(FactScope.PLAYER, "has_key", FactValue.of(true));
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "has_key", FactValue.of(false));
    assertFalse(condition.evaluate(ctx));
  }

  @Test
  void evaluate_missingFact_returnsFalse() {
    FactEqualsCondition condition =
        new FactEqualsCondition(FactScope.PLAYER, "missing_key", FactValue.of(42L));
    ConditionContext ctx = ConditionContext.ofTest(new PlayerState(UUID.randomUUID()));
    assertFalse(condition.evaluate(ctx));
  }

  @Test
  void evaluate_strictTypeMismatch_returnsFalse() {
    FactEqualsCondition condition =
        new FactEqualsCondition(FactScope.PLAYER, "coins", FactValue.of(42L));
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "coins", FactValue.of("42"));
    assertFalse(condition.evaluate(ctx));
  }

  @Test
  void evaluate_matchingLongFact_returnsTrue() {
    FactEqualsCondition condition =
        new FactEqualsCondition(FactScope.PLAYER, "coins", FactValue.of(100L));
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "coins", FactValue.of(100L));
    assertTrue(condition.evaluate(ctx));
  }

  @Test
  void parse_validBooleanJson_parsesCorrectly() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "dqse:fact_equals");
    json.addProperty("scope", "player");
    json.addProperty("fact", "has_key");
    json.addProperty("value", true);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        FactEqualsCondition.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "has_key", FactValue.of(true));
    assertTrue(condition.evaluate(ctx));
  }

  @Test
  void parse_validLongJson_parsesAsLong() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "dqse:fact_equals");
    json.addProperty("scope", "player");
    json.addProperty("fact", "coins");
    json.addProperty("value", 42);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        FactEqualsCondition.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "coins", FactValue.of(42L));
    assertTrue(condition.evaluate(ctx));
  }

  @Test
  void parse_validDoubleJson_parsesAsDouble() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "dqse:fact_equals");
    json.addProperty("scope", "player");
    json.addProperty("fact", "health");
    json.addProperty("value", 3.14);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        FactEqualsCondition.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "health", FactValue.of(3.14));
    assertTrue(condition.evaluate(ctx));
  }

  @Test
  void parse_missingScope_addsIssueAndReturnsNever() {
    JsonObject json = new JsonObject();
    json.addProperty("fact", "has_key");
    json.addProperty("value", true);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        FactEqualsCondition.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
    assertFalse(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }

  @Test
  void parse_invalidScope_addsIssueAndReturnsNever() {
    JsonObject json = new JsonObject();
    json.addProperty("scope", "invalid_scope");
    json.addProperty("fact", "has_key");
    json.addProperty("value", true);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        FactEqualsCondition.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, issues.get(0).code());
    assertFalse(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }

  @Test
  void parse_scopeCaseInsensitive_parsesCorrectly() {
    JsonObject json = new JsonObject();
    json.addProperty("scope", "player");
    json.addProperty("fact", "flag");
    json.addProperty("value", true);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        FactEqualsCondition.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    ConditionContext ctx = contextWithFact(FactScope.PLAYER, "flag", FactValue.of(true));
    assertTrue(condition.evaluate(ctx));
  }
}
