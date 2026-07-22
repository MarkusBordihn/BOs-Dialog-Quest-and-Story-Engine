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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ConditionParserTest {

  private static final ResourceLocation CONTENT_ID =
      new ResourceLocation("test", "condition_parser_test");

  @BeforeAll
  static void registerTestHandlers() {
    Registries.CONDITIONS.register(
        new ResourceLocation("test", "always_true"),
        (json, contentType, id, filePath, issues) -> context -> true);
    Registries.CONDITIONS.register(
        new ResourceLocation("test", "always_false"),
        (json, contentType, id, filePath, issues) -> context -> false);
  }

  private static ConditionContext emptyContext() {
    return ConditionContext.ofTest(new PlayerState(UUID.randomUUID()));
  }

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  @Test
  void parse_knownType_returnsWorkingCondition() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "test:always_true");
    List<ContentIssue> issues = noIssues();

    Condition condition =
        ConditionParser.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertTrue(condition.evaluate(emptyContext()));
  }

  @Test
  void parse_unknownType_returnsNeverAndAddsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "test:nonexistent_type");
    List<ContentIssue> issues = noIssues();

    Condition condition =
        ConditionParser.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertFalse(condition.evaluate(emptyContext()));
    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_CONDITION_TYPE, issues.get(0).code());
  }

  @Test
  void parse_missingType_addsIssue() {
    JsonObject json = new JsonObject();
    List<ContentIssue> issues = noIssues();

    Condition condition =
        ConditionParser.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertFalse(condition.evaluate(emptyContext()));
    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void parse_allGroup_returnsAllConditionGroup() {
    JsonArray members = new JsonArray();
    JsonObject member1 = new JsonObject();
    member1.addProperty("type", "test:always_true");
    JsonObject member2 = new JsonObject();
    member2.addProperty("type", "test:always_false");
    members.add(member1);
    members.add(member2);

    JsonObject json = new JsonObject();
    json.add("all", members);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        ConditionParser.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertInstanceOf(ConditionGroup.class, condition);
    assertEquals(GroupOperator.ALL, ((ConditionGroup) condition).operator());
    assertFalse(condition.evaluate(emptyContext()));
  }

  @Test
  void parse_anyGroup_returnsAnyConditionGroup() {
    JsonArray members = new JsonArray();
    JsonObject member1 = new JsonObject();
    member1.addProperty("type", "test:always_false");
    JsonObject member2 = new JsonObject();
    member2.addProperty("type", "test:always_true");
    members.add(member1);
    members.add(member2);

    JsonObject json = new JsonObject();
    json.add("any", members);
    List<ContentIssue> issues = noIssues();

    Condition condition =
        ConditionParser.parse(json, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertInstanceOf(ConditionGroup.class, condition);
    assertEquals(GroupOperator.ANY, ((ConditionGroup) condition).operator());
    assertTrue(condition.evaluate(emptyContext()));
  }

  @Test
  void parseGroup_emptyArray_returnsAlwaysTrueGroup() {
    JsonArray empty = new JsonArray();
    List<ContentIssue> issues = noIssues();

    ConditionGroup group =
        ConditionParser.parseGroup(empty, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertTrue(group.evaluate(emptyContext()));
  }

  @Test
  void parse_notAnObject_addsIssue() {
    List<ContentIssue> issues = noIssues();

    Condition condition =
        ConditionParser.parse(
            com.google.gson.JsonNull.INSTANCE, ContentType.DIALOG, CONTENT_ID, "test.json", issues);

    assertFalse(condition.evaluate(emptyContext()));
    assertEquals(1, issues.size());
  }
}
