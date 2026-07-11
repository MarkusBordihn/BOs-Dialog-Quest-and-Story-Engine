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

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class QuestProgress {

  private final Map<String, StepProgress> steps;
  private QuestState state;
  private int revision;
  private int lastRewardedRevision;

  public QuestProgress(QuestState state) {
    this.state = state;
    this.revision = 0;
    this.lastRewardedRevision = -1;
    this.steps = new LinkedHashMap<>();
  }

  public QuestProgress(QuestState state, int revision, Map<String, StepProgress> steps) {
    this(state, revision, -1, steps);
  }

  public QuestProgress(
      QuestState state, int revision, int lastRewardedRevision, Map<String, StepProgress> steps) {
    this.state = state;
    this.revision = revision;
    this.lastRewardedRevision = lastRewardedRevision;
    this.steps = new LinkedHashMap<>(steps);
  }

  public QuestState state() {
    return this.state;
  }

  public int revision() {
    return this.revision;
  }

  public int lastRewardedRevision() {
    return this.lastRewardedRevision;
  }

  public Map<String, StepProgress> steps() {
    return Collections.unmodifiableMap(this.steps);
  }

  public void setState(QuestState newState) {
    this.state = newState;
    bump();
  }

  public void putStep(String stepId, StepProgress stepProgress) {
    this.steps.put(stepId, stepProgress);
    bump();
  }

  public void setLastRewardedRevision(int revision) {
    this.lastRewardedRevision = revision;
  }

  public StepProgress incrementStep(String stepId, int delta) {
    StepProgress current = this.steps.getOrDefault(stepId, StepProgress.locked());
    StepProgress updated = current.withProgress(current.progress() + delta);
    this.steps.put(stepId, updated);
    bump();
    return updated;
  }

  private void bump() {
    this.revision++;
  }
}
