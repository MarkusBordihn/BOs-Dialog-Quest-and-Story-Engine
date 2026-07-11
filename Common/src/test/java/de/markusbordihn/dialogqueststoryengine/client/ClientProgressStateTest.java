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

package de.markusbordihn.dialogqueststoryengine.client;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ClientProgressStateTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  @AfterEach
  void tearDown() {
    ClientProgressState.clear();
  }

  @Test
  void staleQuestDeltaDoesNotOverwriteNewerState() {
    ClientProgressState.apply(
        new QuestDeltaPacket(
            QUEST_ID, QuestState.ACTIVE, Map.of("step", StepProgress.active(5)), 2));
    ClientProgressState.apply(new QuestDeltaPacket(QUEST_ID, QuestState.COMPLETED, Map.of(), 1));

    ClientProgressState.ClientQuestProgress progress = ClientProgressState.quests().get(QUEST_ID);
    assertEquals(QuestState.ACTIVE, progress.state());
    assertEquals(2, progress.revision());
  }
}
