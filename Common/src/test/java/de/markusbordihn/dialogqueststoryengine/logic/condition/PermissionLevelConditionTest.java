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
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.PermissionLevelCondition;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PermissionLevelConditionTest {

  private static final ResourceLocation CONTENT_ID =
      new ResourceLocation("test", "permission_level_test");

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  @Test
  void evaluate_nullPlayer_returnsFalse() {
    PermissionLevelCondition condition = new PermissionLevelCondition(2);
    ConditionContext context = ConditionContext.ofTest(new PlayerState(UUID.randomUUID()));
    assertFalse(condition.evaluate(context));
  }

  @Test
  void parse_validLevel_parsesCorrectly() {
    for (int level = 0; level <= 4; level++) {
      JsonObject json = new JsonObject();
      json.addProperty("level", level);
      List<ContentIssue> issues = noIssues();

      Condition condition =
          PermissionLevelCondition.parse(
              json, ContentType.INTERACTION, CONTENT_ID, "test.json", issues);

      assertTrue(issues.isEmpty(), "Expected no issues for level " + level);
      assertEquals(level, ((PermissionLevelCondition) condition).level());
    }
  }

  @Test
  void parse_missingLevel_addsIssue() {
    JsonObject json = new JsonObject();
    List<ContentIssue> issues = noIssues();

    Condition condition =
        PermissionLevelCondition.parse(
            json, ContentType.INTERACTION, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
    assertFalse(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }

  @Test
  void parse_levelBelowZero_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("level", -1);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        PermissionLevelCondition.parse(
            json, ContentType.INTERACTION, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, issues.get(0).code());
  }

  @Test
  void parse_levelAboveFour_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("level", 5);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        PermissionLevelCondition.parse(
            json, ContentType.INTERACTION, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, issues.get(0).code());
  }
}
