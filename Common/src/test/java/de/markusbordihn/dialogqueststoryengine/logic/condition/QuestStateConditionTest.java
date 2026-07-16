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
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.QuestStateCondition;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class QuestStateConditionTest {

  private static final ResourceLocation CONTENT_ID =
      new ResourceLocation("test", "quest_state_test");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "my_quest");

  private static List<ContentIssue> noIssues() {
    return new ArrayList<>();
  }

  @Test
  void evaluate_questInExpectedState_returnsTrue() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    ConditionContext ctx = ConditionContext.ofTest(playerState);

    QuestStateCondition condition = new QuestStateCondition(QUEST_ID, QuestState.ACTIVE);
    assertTrue(condition.evaluate(ctx));
  }

  @Test
  void evaluate_questInDifferentState_returnsFalse() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    ConditionContext ctx = ConditionContext.ofTest(playerState);

    QuestStateCondition condition = new QuestStateCondition(QUEST_ID, QuestState.COMPLETED);
    assertFalse(condition.evaluate(ctx));
  }

  @Test
  void evaluate_questMissing_treatedAsNotStarted() {
    ConditionContext ctx = ConditionContext.ofTest(new PlayerState(UUID.randomUUID()));

    QuestStateCondition notStartedCondition =
        new QuestStateCondition(QUEST_ID, QuestState.NOT_STARTED);
    assertTrue(notStartedCondition.evaluate(ctx));

    QuestStateCondition activeCondition = new QuestStateCondition(QUEST_ID, QuestState.ACTIVE);
    assertFalse(activeCondition.evaluate(ctx));
  }

  @Test
  void parse_validJson_parsesCorrectly() {
    JsonObject json = new JsonObject();
    json.addProperty("type", "dqse:quest_state");
    json.addProperty("quest", "test:my_quest");
    json.addProperty("state", "active");
    List<ContentIssue> issues = noIssues();

    Condition condition =
        QuestStateCondition.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    assertTrue(condition.evaluate(ConditionContext.ofTest(playerState)));
  }

  @Test
  void parse_missingQuestField_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("state", "active");
    List<ContentIssue> issues = noIssues();

    Condition condition =
        QuestStateCondition.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
    assertFalse(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }

  @Test
  void parse_invalidStateValue_addsIssue() {
    JsonObject json = new JsonObject();
    json.addProperty("quest", "test:my_quest");
    json.addProperty("state", "not_a_real_state");
    List<ContentIssue> issues = noIssues();

    Condition condition =
        QuestStateCondition.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, issues.get(0).code());
  }

  @Test
  void parse_stateCaseInsensitive_parsesCorrectly() {
    JsonObject json = new JsonObject();
    json.addProperty("quest", "test:my_quest");
    json.addProperty("state", "not_started");
    List<ContentIssue> issues = noIssues();

    Condition condition =
        QuestStateCondition.parse(json, ContentType.QUEST, CONTENT_ID, "test.json", issues);

    assertTrue(issues.isEmpty());
    assertTrue(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }
}
