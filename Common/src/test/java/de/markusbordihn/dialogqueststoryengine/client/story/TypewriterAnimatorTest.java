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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class TypewriterAnimatorTest {

  @Test
  void initialStateIsIdle() {
    TypewriterAnimator animator = new TypewriterAnimator();
    assertEquals(PlaybackState.IDLE, animator.state());
    assertFalse(animator.isPlaying());
    assertFalse(animator.isComplete());
    assertTrue(animator.getRevealedLines().isEmpty());
    assertEquals("", animator.getPartialLine());
  }

  @Test
  void startWithEmptyLinesIsComplete() {
    TypewriterAnimator animator = new TypewriterAnimator();
    animator.start(List.of());
    assertTrue(animator.isComplete());
    assertTrue(animator.getRevealedLines().isEmpty());
  }

  @Test
  void startWithLinesIsPlaying() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("Hello"));
    assertTrue(animator.isPlaying());
    assertFalse(animator.isComplete());
  }

  @Test
  void tickAdvancesPartialLine() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("Hi"));

    assertEquals("", animator.getPartialLine());
    animator.tick();
    assertEquals("H", animator.getPartialLine());
    animator.tick();
    assertTrue(animator.isComplete());
    assertEquals("", animator.getPartialLine());
  }

  @Test
  void tickAdvancesMultipleCharsPerTick() {
    TypewriterAnimator animator = new TypewriterAnimator(3);
    animator.start(List.of("ABCDE"));

    animator.tick();
    assertEquals("ABC", animator.getPartialLine());
    animator.tick();
    assertTrue(animator.isComplete());
  }

  @Test
  void tickAcrossMultipleLines() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("AB", "CD"));

    for (int i = 0; i < 2; i++) {
      animator.tick();
    }
    assertEquals(List.of("AB"), animator.getRevealedLines());

    animator.tick();
    assertEquals("C", animator.getPartialLine());
    animator.tick();
    assertTrue(animator.isComplete());
    assertEquals(List.of("AB", "CD"), animator.getRevealedLines());
  }

  @Test
  void emptyLinesAreSkipped() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("", "Hi"));

    assertTrue(animator.isPlaying());
    assertEquals("", animator.getRevealedLines().stream().findFirst().orElse(null));
  }

  @Test
  void allEmptyLinesIsComplete() {
    TypewriterAnimator animator = new TypewriterAnimator();
    animator.start(List.of("", "", ""));
    assertTrue(animator.isComplete());
  }

  @Test
  void skipCompletesImmediately() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("Hello", "World"));
    animator.tick();
    animator.skip();

    assertTrue(animator.isComplete());
    assertEquals(List.of("Hello", "World"), animator.getRevealedLines());
    assertEquals("", animator.getPartialLine());
  }

  @Test
  void skipOnIdleStaysIdle() {
    TypewriterAnimator animator = new TypewriterAnimator();
    animator.skip();
    assertEquals(PlaybackState.IDLE, animator.state());
  }

  @Test
  void revealedLinesExcludesCurrentPartialLine() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("AB", "CD"));
    animator.tick();
    animator.tick();
    animator.tick();

    assertEquals(List.of("AB"), animator.getRevealedLines());
    assertEquals("C", animator.getPartialLine());
  }

  @Test
  void restartResetsState() {
    TypewriterAnimator animator = new TypewriterAnimator(1);
    animator.start(List.of("First"));
    animator.skip();
    assertTrue(animator.isComplete());

    animator.start(List.of("Second"));
    assertTrue(animator.isPlaying());
    assertTrue(animator.getRevealedLines().isEmpty());
  }

  @Test
  void invalidCharsPerTickThrows() {
    assertThrows(IllegalArgumentException.class, () -> new TypewriterAnimator(0));
    assertThrows(IllegalArgumentException.class, () -> new TypewriterAnimator(-1));
  }

  @Test
  void defaultCharsPerTickIsTwo() {
    assertEquals(2, TypewriterAnimator.DEFAULT_CHARS_PER_TICK);
    TypewriterAnimator animator = new TypewriterAnimator();
    animator.start(List.of("ABCD"));
    animator.tick();
    assertEquals("AB", animator.getPartialLine());
  }
}
