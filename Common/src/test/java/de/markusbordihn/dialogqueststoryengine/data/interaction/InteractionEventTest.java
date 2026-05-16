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

package de.markusbordihn.dialogqueststoryengine.data.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.markusbordihn.dialogqueststoryengine.Constants;
import org.junit.jupiter.api.Test;

class InteractionEventTest {

  @Test
  void allValuesHaveNonNullResourceLocation() {
    for (InteractionEventType type : InteractionEventType.values()) {
      assertNotNull(type.resourceLocation());
    }
  }

  @Test
  void allValuesHaveModNamespace() {
    for (InteractionEventType type : InteractionEventType.values()) {
      assertEquals(Constants.MOD_ID, type.resourceLocation().getNamespace());
    }
  }

  @Test
  void allValuesHaveNonEmptyPath() {
    for (InteractionEventType type : InteractionEventType.values()) {
      assertNotNull(type.resourceLocation().getPath());
      assertEquals(false, type.resourceLocation().getPath().isEmpty());
    }
  }

  @Test
  void expectedValuesPresent() {
    assertNotNull(InteractionEventType.ON_ENTITY_INTERACT);
    assertNotNull(InteractionEventType.ON_BLOCK_INTERACT);
    assertNotNull(InteractionEventType.ON_STEP_ON);
    assertNotNull(InteractionEventType.ON_COMMAND);
    assertNotNull(InteractionEventType.ON_HOLOPAD_USE);
    assertNotNull(InteractionEventType.ON_EASY_NPC_INTERACT);
  }

  @Test
  void fromNameReturnsCorrectValue() {
    assertEquals(
        InteractionEventType.ON_ENTITY_INTERACT,
        InteractionEventType.fromName("ON_ENTITY_INTERACT"));
    assertEquals(InteractionEventType.ON_STEP_ON, InteractionEventType.fromName("ON_STEP_ON"));
  }

  @Test
  void fromNameReturnsNullForUnknown() {
    assertNull(InteractionEventType.fromName("UNKNOWN_EVENT"));
    assertNull(InteractionEventType.fromName("on_entity_interact"));
  }
}
