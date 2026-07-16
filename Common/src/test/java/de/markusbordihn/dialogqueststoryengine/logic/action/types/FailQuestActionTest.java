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
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class FailQuestActionTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  private static Action parse(String raw, List<ContentIssue> issues) {
    return FailQuestAction.parse(
        GSON.fromJson(raw, JsonObject.class), ContentType.DIALOG, TEST_ID, "test.json", issues);
  }

  @Test
  void executeFailsActiveQuest() {
    Action action = parse("{\"quest\":\"test:quest\"}", new ArrayList<>());
    assertInstanceOf(FailQuestAction.class, action);

    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.putQuestDirect(QUEST_ID, new QuestProgress(QuestState.ACTIVE));

    action.execute(ActionContext.ofTest(playerState));

    assertEquals(QuestState.FAILED, playerState.getQuest(QUEST_ID).state());
  }

  @Test
  void missingQuestIsRejected() {
    List<ContentIssue> issues = new ArrayList<>();
    Action action = parse("{}", issues);

    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
    assertSame(Action.NOOP, action);
  }
}
