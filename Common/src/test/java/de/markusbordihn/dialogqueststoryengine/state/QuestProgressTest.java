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

package de.markusbordihn.dialogqueststoryengine.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import org.junit.jupiter.api.Test;

class QuestProgressTest {

  @Test
  void initialStateIsPreserved() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);

    assertEquals(QuestState.ACTIVE, questProgress.state());
    assertEquals(0, questProgress.revision());
    assertTrue(questProgress.steps().isEmpty());
  }

  @Test
  void putStepBumpsRevision() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep("kill_wolves", StepProgress.active(5));

    assertEquals(1, questProgress.revision());
    assertNotNull(questProgress.steps().get("kill_wolves"));
  }

  @Test
  void setStateBumpsRevision() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.setState(QuestState.COMPLETED);

    assertEquals(QuestState.COMPLETED, questProgress.state());
    assertEquals(1, questProgress.revision());
  }

  @Test
  void incrementStepAccumulatesProgress() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep("kill_wolves", StepProgress.active(5));

    StepProgress updated = questProgress.incrementStep("kill_wolves", 2);
    assertEquals(2, updated.progress());

    StepProgress updated2 = questProgress.incrementStep("kill_wolves", 2);
    assertEquals(4, updated2.progress());
  }

  @Test
  void incrementStepCompletesWhenReachingRequired() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep("kill_wolves", StepProgress.active(3));

    StepProgress result = questProgress.incrementStep("kill_wolves", 3);

    assertTrue(result.complete());
    assertEquals(StepState.COMPLETED, result.state());
    assertEquals(3, result.progress());
  }

  @Test
  void incrementStepClampsAtRequired() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep("step", StepProgress.active(3));

    StepProgress result = questProgress.incrementStep("step", 100);

    assertEquals(3, result.progress());
    assertEquals(3, result.required());
    assertTrue(result.complete());
  }

  @Test
  void incrementMissingStepUsesLockedDefault() {
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    StepProgress result = questProgress.incrementStep("nonexistent", 1);

    assertEquals(StepState.LOCKED, result.state());
    assertEquals(0, result.progress());
  }
}
