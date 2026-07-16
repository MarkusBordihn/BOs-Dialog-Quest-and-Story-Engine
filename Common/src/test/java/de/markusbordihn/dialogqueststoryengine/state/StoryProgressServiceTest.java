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

import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class StoryProgressServiceTest {

  private static final ResourceLocation STORY = new ResourceLocation("test", "story_a");

  @Test
  void unlockMutatesAndBumpsRevision() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());

    boolean changed = StoryProgressService.unlock(null, playerState, STORY);

    assertTrue(changed);
    assertTrue(playerState.stories().isUnlocked(STORY));
    assertEquals(1, playerState.stories().revision());
  }

  @Test
  void redundantUnlockDoesNotBumpRevision() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    StoryProgressService.unlock(null, playerState, STORY);
    int revisionAfterFirst = playerState.stories().revision();

    boolean changed = StoryProgressService.unlock(null, playerState, STORY);

    assertFalse(changed);
    assertEquals(revisionAfterFirst, playerState.stories().revision());
  }

  @Test
  void markReadImpliesUnlockedAndBumpsRevision() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());

    boolean changed = StoryProgressService.markRead(null, playerState, STORY);

    assertTrue(changed);
    assertTrue(playerState.stories().isRead(STORY));
    assertTrue(playerState.stories().isUnlocked(STORY));
    assertEquals(1, playerState.stories().revision());
  }
}
