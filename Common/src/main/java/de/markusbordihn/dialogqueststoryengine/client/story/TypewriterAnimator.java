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

package de.markusbordihn.dialogqueststoryengine.client.story;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TypewriterAnimator {

  public static final int DEFAULT_CHARACTERS_PER_TICK = 2;

  private final int charactersPerTick;
  private List<String> lines = List.of();
  private int lineIndex;
  private int characterIndex;
  private PlaybackState state = PlaybackState.IDLE;

  public TypewriterAnimator() {
    this.charactersPerTick = DEFAULT_CHARACTERS_PER_TICK;
  }

  public TypewriterAnimator(int charactersPerTick) {
    if (charactersPerTick < 1) {
      throw new IllegalArgumentException(
          "charactersPerTick must be >= 1, got: " + charactersPerTick);
    }

    this.charactersPerTick = charactersPerTick;
  }

  public void start(List<String> inputLines) {
    this.lines = List.copyOf(inputLines);
    this.lineIndex = 0;
    this.characterIndex = 0;
    this.state = this.lines.isEmpty() ? PlaybackState.COMPLETE : PlaybackState.PLAYING;
    advancePastEmptyLines();
  }

  public void tick() {
    if (this.state != PlaybackState.PLAYING) {
      return;
    }

    int remaining = this.charactersPerTick;
    while (remaining > 0 && this.state == PlaybackState.PLAYING) {
      String currentLine = this.lines.get(this.lineIndex);
      int availableInLine = currentLine.length() - this.characterIndex;

      if (availableInLine <= remaining) {
        remaining -= availableInLine;
        this.lineIndex++;
        this.characterIndex = 0;

        if (this.lineIndex >= this.lines.size()) {
          this.state = PlaybackState.COMPLETE;
          return;
        }

        advancePastEmptyLines();
      } else {
        this.characterIndex += remaining;
        remaining = 0;
      }
    }
  }

  public void skip() {
    this.lineIndex = this.lines.size();
    this.characterIndex = 0;
    this.state = this.lines.isEmpty() ? PlaybackState.IDLE : PlaybackState.COMPLETE;
  }

  public List<String> getRevealedLines() {
    if (this.state == PlaybackState.IDLE) {
      return List.of();
    }

    if (this.state == PlaybackState.COMPLETE) {
      return this.lines;
    }

    return Collections.unmodifiableList(new ArrayList<>(this.lines.subList(0, this.lineIndex)));
  }

  public String getPartialLine() {
    if (this.state != PlaybackState.PLAYING) {
      return "";
    }

    if (this.lineIndex >= this.lines.size()) {
      return "";
    }

    return this.lines.get(this.lineIndex).substring(0, this.characterIndex);
  }

  public boolean isComplete() {
    return this.state == PlaybackState.COMPLETE;
  }

  public boolean isPlaying() {
    return this.state == PlaybackState.PLAYING;
  }

  public PlaybackState state() {
    return this.state;
  }

  private void advancePastEmptyLines() {
    while (this.lineIndex < this.lines.size() && this.lines.get(this.lineIndex).isEmpty()) {
      this.lineIndex++;
    }

    if (this.lineIndex >= this.lines.size()) {
      this.state = PlaybackState.COMPLETE;
    }
  }
}
