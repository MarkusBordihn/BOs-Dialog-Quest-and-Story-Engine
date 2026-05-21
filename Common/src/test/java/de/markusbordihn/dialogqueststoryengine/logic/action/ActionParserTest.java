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
import de.markusbordihn.dialogqueststoryengine.logic.action.types.AdvanceQuestStepAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.CompleteQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveItemAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.MarkStoryReadAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenDialogAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenStoryAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunCommandAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunFunctionAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.SetFactAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.StartQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.UnlockStoryAction;
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
  static void registerTestHandlers() {
    Registries.ACTIONS.register(
        new ResourceLocation("test", "custom_action"), (json, ct, id, fp, issues) -> Action.NOOP);
  }

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  @Test
  void infer_itemKey_parsesGiveItemAction() {
    JsonObject json = new JsonObject();
    json.addProperty("item", "minecraft:diamond");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(GiveItemAction.class, action);
  }

  @Test
  void infer_dialogKey_parsesOpenDialogAction() {
    JsonObject json = new JsonObject();
    json.addProperty("dialog", "test:my_dialog");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(OpenDialogAction.class, action);
  }

  @Test
  void infer_functionKey_parsesRunFunctionAction() {
    JsonObject json = new JsonObject();
    json.addProperty("function", "test:my_function");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(RunFunctionAction.class, action);
  }

  @Test
  void infer_commandKey_parsesRunCommandAction() {
    JsonObject json = new JsonObject();
    json.addProperty("command", "say hello");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(RunCommandAction.class, action);
  }

  @Test
  void infer_factAndValueKeys_parsesSetFactAction() {
    JsonObject json = new JsonObject();
    json.addProperty("fact", "coins");
    json.addProperty("value", 42);
    json.addProperty("scope", "player");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(SetFactAction.class, action);
  }

  @Test
  void infer_questWithStepKey_parsesAdvanceQuestStepAction() {
    JsonObject json = new JsonObject();
    json.addProperty("quest", "test:my_quest");
    json.addProperty("step", "find_artifact");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(AdvanceQuestStepAction.class, action);
  }

  @Test
  void infer_questWithCompleteTrue_parsesCompleteQuestAction() {
    JsonObject json = new JsonObject();
    json.addProperty("quest", "test:my_quest");
    json.addProperty("complete", true);
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(CompleteQuestAction.class, action);
  }

  @Test
  void infer_questAlone_parsesStartQuestAction() {
    JsonObject json = new JsonObject();
    json.addProperty("quest", "test:my_quest");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(StartQuestAction.class, action);
  }

  @Test
  void infer_storyWithReadTrue_parsesMarkStoryReadAction() {
    JsonObject json = new JsonObject();
    json.addProperty("story", "test:my_story");
    json.addProperty("read", true);
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(MarkStoryReadAction.class, action);
  }

  @Test
  void infer_storyWithOpenTrue_parsesOpenStoryAction() {
    JsonObject json = new JsonObject();
    json.addProperty("story", "test:my_story");
    json.addProperty("open", true);
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(OpenStoryAction.class, action);
  }

  @Test
  void infer_storyAlone_parsesUnlockStoryAction() {
    JsonObject json = new JsonObject();
    json.addProperty("story", "test:my_story");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(UnlockStoryAction.class, action);
  }

  @Test
  void explicitType_registeredHandler_delegatesToHandler() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "test:custom_action");
    List<ContentIssue> issues = noIssues();

    Action action =
        ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertEquals(Action.NOOP, action);
  }

  @Test
  void explicitType_unknownType_addsIssueAndReturnsNoop() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "test:does_not_exist");
    List<ContentIssue> issues = noIssues();

    ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void noMatchingKey_addsIssueAndReturnsNoop() {
    JsonObject json = new JsonObject();
    json.addProperty("unknown_field", "value");
    List<ContentIssue> issues = noIssues();

    ActionParser.parseSingle(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_ACTION_TYPE, issues.get(0).code());
  }

  @Test
  void parseList_emptyArray_returnsEmpty() {
    ActionList list =
        ActionParser.parseList(
            new JsonArray(), ContentType.QUEST, CONTENT_ID, "test.json", noIssues());
    assertTrue(list.isEmpty());
  }

  @Test
  void parseList_nonObjectElement_addsIssueAndSkipsElement() {
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
