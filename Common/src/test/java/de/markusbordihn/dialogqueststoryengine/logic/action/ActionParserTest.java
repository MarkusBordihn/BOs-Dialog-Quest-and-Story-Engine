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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveExperienceAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveItemAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenDialogAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunCommandAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunFunctionAction;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ActionParserTest {

  private static final ResourceLocation CONTENT_ID =
      new ResourceLocation("test", "action_parser_test");

  @BeforeAll
  static void registerHandlers() {
    BuiltinActions.register();
    Registries.ACTIONS.register(
        new ResourceLocation("test", "custom_action"), (json, ct, id, fp, issues) -> Action.NOOP);
  }

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  private static Action parse(JsonObject json, List<ContentIssue> issues) {
    return ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);
  }

  private static JsonObject withProperty(String key, String value) {
    JsonObject json = new JsonObject();
    json.addProperty(key, value);
    return json;
  }

  @Test
  void itemShorthandParsesGiveItemAction() {
    List<ContentIssue> issues = noIssues();
    Action action = parse(withProperty("item", "minecraft:diamond"), issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(GiveItemAction.class, action);
  }

  @Test
  void dialogShorthandParsesOpenDialogAction() {
    List<ContentIssue> issues = noIssues();
    Action action = parse(withProperty("dialog", "test:my_dialog"), issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(OpenDialogAction.class, action);
  }

  @Test
  void functionShorthandParsesRunFunctionAction() {
    List<ContentIssue> issues = noIssues();
    Action action = parse(withProperty("function", "test:my_function"), issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(RunFunctionAction.class, action);
  }

  @Test
  void commandShorthandParsesRunCommandAction() {
    List<ContentIssue> issues = noIssues();
    Action action = parse(withProperty("command", "say hello"), issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(RunCommandAction.class, action);
  }

  @Test
  void giveExperienceRequiresExplicitType() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "dqse:give_experience");
    json.addProperty("amount", 25);
    List<ContentIssue> issues = noIssues();

    Action action = parse(json, issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(GiveExperienceAction.class, action);
  }

  @Test
  void questShorthandIsAmbiguous() {
    List<ContentIssue> issues = noIssues();
    parse(withProperty("quest", "test:my_quest"), issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.AMBIGUOUS_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void storyShorthandIsAmbiguous() {
    List<ContentIssue> issues = noIssues();
    parse(withProperty("story", "test:my_story"), issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.AMBIGUOUS_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void factShorthandIsAmbiguous() {
    JsonObject json = new JsonObject();
    json.addProperty("fact", "coins");
    json.addProperty("value", 42);
    json.addProperty("scope", "player");
    List<ContentIssue> issues = noIssues();

    parse(json, issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.AMBIGUOUS_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void multipleShorthandKeysAreAmbiguous() {
    JsonObject json = new JsonObject();
    json.addProperty("item", "minecraft:diamond");
    json.addProperty("dialog", "test:my_dialog");
    List<ContentIssue> issues = noIssues();

    parse(json, issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.AMBIGUOUS_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void explicitTypeDelegatesToHandler() {
    List<ContentIssue> issues = noIssues();
    Action action = parse(withProperty("type", "test:custom_action"), issues);

    assertTrue(issues.isEmpty());
    assertEquals(Action.NOOP, action);
  }

  @Test
  void explicitUnknownTypeReportsUnknownActionType() {
    List<ContentIssue> issues = noIssues();
    parse(withProperty("type", "test:does_not_exist"), issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void noMatchingKeyReportsUnknownActionType() {
    List<ContentIssue> issues = noIssues();
    parse(withProperty("unknown_field", "value"), issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void parseListEmptyArrayReturnsEmpty() {
    ActionList list =
        ActionParser.parseList(
            new JsonArray(), ContentType.QUEST, CONTENT_ID, "test.json", noIssues());
    assertTrue(list.isEmpty());
  }

  @Test
  void parseListNonObjectElementReportsIssueAndSkips() {
    JsonArray array = new JsonArray();
    array.add("not an object");
    List<ContentIssue> issues = noIssues();

    ActionList list =
        ActionParser.parseList(array, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, issues.get(0).code());
    assertTrue(list.isEmpty());
  }
}
