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

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class StoryProgressTest {

  private static final ResourceLocation STORY_A = new ResourceLocation("test", "story_a");
  private static final ResourceLocation STORY_B = new ResourceLocation("test", "story_b");

  @Test
  void initiallyNothingIsUnlockedOrRead() {
    StoryProgress storyProgress = new StoryProgress();

    assertFalse(storyProgress.isUnlocked(STORY_A));
    assertFalse(storyProgress.isRead(STORY_A));
  }

  @Test
  void unlockMakesStoryAccessible() {
    StoryProgress storyProgress = new StoryProgress();
    storyProgress.unlock(STORY_A);

    assertTrue(storyProgress.isUnlocked(STORY_A));
    assertFalse(storyProgress.isUnlocked(STORY_B));
  }

  @Test
  void markReadTracksReadStories() {
    StoryProgress storyProgress = new StoryProgress();
    storyProgress.unlock(STORY_A);
    storyProgress.markRead(STORY_A);

    assertTrue(storyProgress.isRead(STORY_A));
    assertFalse(storyProgress.isRead(STORY_B));
  }

  @Test
  void readImpliesUnlocked() {
    StoryProgress storyProgress = new StoryProgress();
    storyProgress.markRead(STORY_B);

    assertTrue(storyProgress.isRead(STORY_B));
    assertTrue(storyProgress.isUnlocked(STORY_B));
  }

  @Test
  void unlockedIdsContainsAllUnlocked() {
    StoryProgress storyProgress = new StoryProgress();
    storyProgress.unlock(STORY_A);
    storyProgress.unlock(STORY_B);

    assertTrue(storyProgress.unlockedIds().contains(STORY_A));
    assertTrue(storyProgress.unlockedIds().contains(STORY_B));
  }
}
