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

package de.markusbordihn.dialogqueststoryengine.logic.action.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class SetFactActionTest {

  private static final ResourceLocation CONTENT_ID = new ResourceLocation("test", "set_fact_test");

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  @Test
  void execute_playerScope_setsFactOnPlayerState() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext ctx = ActionContext.ofTest(playerState);

    new SetFactAction(FactScope.PLAYER, "coins", FactValue.of(42L)).execute(ctx);

    assertEquals(FactValue.of(42L), playerState.getFact(FactScope.PLAYER, "coins"));
  }

  @Test
  void execute_nonPlayerScope_doesNotSetFact() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext ctx = ActionContext.ofTest(playerState);

    new SetFactAction(FactScope.WORLD, "global_event", FactValue.of(true)).execute(ctx);

    assertNull(playerState.getFact(FactScope.WORLD, "global_event"));
  }

  @Test
  void parse_validJson_parsesCorrectly() {
    JsonObject json = new JsonObject();
    json.addProperty("scope", "player");
    json.addProperty("fact", "has_key");
    json.addProperty("value", true);
    List<ContentIssue> issues = noIssues();

    Action action = SetFactAction.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    action.execute(ActionContext.ofTest(playerState));
    assertEquals(FactValue.of(true), playerState.getFact(FactScope.PLAYER, "has_key"));
  }

  @Test
  void parse_missingScopeField_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("fact", "coins");
    json.addProperty("value", 10);
    List<ContentIssue> issues = noIssues();

    SetFactAction.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void parse_missingFactField_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("scope", "player");
    json.addProperty("value", 10);
    List<ContentIssue> issues = noIssues();

    SetFactAction.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void parse_missingValueField_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("scope", "player");
    json.addProperty("fact", "coins");
    List<ContentIssue> issues = noIssues();

    SetFactAction.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void parse_invalidScopeValue_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("scope", "nonexistent_scope");
    json.addProperty("fact", "coins");
    json.addProperty("value", 10);
    List<ContentIssue> issues = noIssues();

    SetFactAction.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, issues.get(0).code());
  }
}
