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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class StepProgressTest {

  @Test
  void lockedHasNoProgress() {
    StepProgress locked = StepProgress.locked();

    assertEquals(StepState.LOCKED, locked.state());
    assertEquals(0, locked.progress());
    assertEquals(0, locked.required());
    assertFalse(locked.complete());
  }

  @Test
  void activeStartsAtZero() {
    StepProgress active = StepProgress.active(5);

    assertEquals(StepState.ACTIVE, active.state());
    assertEquals(0, active.progress());
    assertEquals(5, active.required());
    assertFalse(active.complete());
  }

  @Test
  void completedIsFullyDone() {
    StepProgress completed = StepProgress.completed(3);

    assertEquals(StepState.COMPLETED, completed.state());
    assertEquals(3, completed.progress());
    assertEquals(3, completed.required());
    assertTrue(completed.complete());
  }

  @Test
  void withProgressClampsAtRequired() {
    StepProgress step = StepProgress.active(3).withProgress(10);

    assertEquals(3, step.progress());
    assertEquals(StepState.COMPLETED, step.state());
    assertTrue(step.complete());
  }

  @Test
  void withProgressCompletesExactly() {
    StepProgress step = StepProgress.active(2).withProgress(2);

    assertTrue(step.complete());
    assertEquals(StepState.COMPLETED, step.state());
  }

  @Test
  void withProgressBeforeRequiredStaysActive() {
    StepProgress step = StepProgress.active(5).withProgress(3);

    assertEquals(3, step.progress());
    assertFalse(step.complete());
    assertEquals(StepState.ACTIVE, step.state());
  }

  @Test
  void withStateChangesOnlyState() {
    StepProgress step = StepProgress.active(5).withState(StepState.HIDDEN);

    assertEquals(StepState.HIDDEN, step.state());
    assertEquals(0, step.progress());
    assertEquals(5, step.required());
    assertFalse(step.complete());
  }
}
