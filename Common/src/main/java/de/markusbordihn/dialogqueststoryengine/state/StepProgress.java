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

public record StepProgress(StepState state, int progress, int required) {

  public static StepProgress locked() {
    return new StepProgress(StepState.LOCKED, 0, 0);
  }

  public static StepProgress active(int required) {
    return new StepProgress(StepState.ACTIVE, 0, required);
  }

  public static StepProgress completed(int required) {
    return new StepProgress(StepState.COMPLETED, required, required);
  }

  public boolean complete() {
    return this.state == StepState.COMPLETED;
  }

  public StepProgress withState(StepState newState) {
    return new StepProgress(newState, this.progress, this.required);
  }

  public StepProgress withProgress(int newProgress) {
    int clamped = Math.max(0, Math.min(newProgress, this.required));
    if (this.state == StepState.LOCKED) {
      return new StepProgress(StepState.LOCKED, clamped, this.required);
    }
    boolean nowComplete = this.required > 0 && clamped >= this.required;
    return new StepProgress(
        nowComplete ? StepState.COMPLETED : StepState.ACTIVE, clamped, this.required);
  }
}
