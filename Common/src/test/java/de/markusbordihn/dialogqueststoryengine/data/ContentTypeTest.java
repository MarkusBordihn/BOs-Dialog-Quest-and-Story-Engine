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

package de.markusbordihn.dialogqueststoryengine.data;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import org.junit.jupiter.api.Test;

class ContentTypeTest {

  @Test
  void allContentTypesHaveValidCurrentSchema() {
    for (ContentType type : ContentType.values()) {
      assertTrue(type.currentSchema() >= 1, type.name() + " must have currentSchema >= 1");
    }
  }

  @Test
  void playerStateSchemaCurrentIsValid() {
    assertTrue(PlayerStateSchema.CURRENT >= 1);
  }

  @Test
  void interactionContentTypeIsPresent() {
    assertNotNull(ContentType.valueOf("INTERACTION"));
  }

  @Test
  void triggerContentTypeDoesNotExist() {
    assertThrows(IllegalArgumentException.class, () -> ContentType.valueOf("TRIGGER"));
  }

  @Test
  void interactionHasCurrentSchemaOne() {
    assertTrue(ContentType.INTERACTION.currentSchema() >= 1);
  }

  @Test
  void allFourDataPackContentTypesPresent() {
    boolean hasDialog =
        Arrays.stream(ContentType.values()).anyMatch(t -> t.name().equals("DIALOG"));
    boolean hasQuest = Arrays.stream(ContentType.values()).anyMatch(t -> t.name().equals("QUEST"));
    boolean hasInteractiveStory =
        Arrays.stream(ContentType.values()).anyMatch(t -> t.name().equals("INTERACTIVE_STORY"));
    boolean hasInteraction =
        Arrays.stream(ContentType.values()).anyMatch(t -> t.name().equals("INTERACTION"));

    assertTrue(hasDialog && hasQuest && hasInteractiveStory && hasInteraction);
  }
}
