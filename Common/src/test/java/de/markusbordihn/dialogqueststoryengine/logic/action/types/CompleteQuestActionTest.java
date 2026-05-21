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

import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class CompleteQuestActionTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "my_quest");

  @Test
  void execute_activeQuest_becomesCompleted() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.getOrCreateQuest(QUEST_ID, QuestState.ACTIVE);
    ActionContext ctx = ActionContext.ofTest(playerState);

    new CompleteQuestAction(QUEST_ID).execute(ctx);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
  }

  @Test
  void execute_alreadyCompleted_isIdempotent() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.getOrCreateQuest(QUEST_ID, QuestState.COMPLETED);
    ActionContext ctx = ActionContext.ofTest(playerState);

    new CompleteQuestAction(QUEST_ID).execute(ctx);
    new CompleteQuestAction(QUEST_ID).execute(ctx);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
  }

  @Test
  void execute_questNotInPlayerState_doesNotCrash() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext ctx = ActionContext.ofTest(playerState);

    new CompleteQuestAction(QUEST_ID).execute(ctx);

    assertNull(playerState.getQuest(QUEST_ID));
  }
}
