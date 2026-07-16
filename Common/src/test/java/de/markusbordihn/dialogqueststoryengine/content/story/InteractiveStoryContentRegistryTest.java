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

package de.markusbordihn.dialogqueststoryengine.content.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.story.InteractiveStoryDefinition;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InteractiveStoryContentRegistryTest {

  private static final ResourceLocation TEST_ID_A = new ResourceLocation("test", "story_a");
  private static final ResourceLocation TEST_ID_B = new ResourceLocation("test", "story_b");
  private static final ResourceLocation LAYOUT = new ResourceLocation("test", "layout");

  private static InteractiveStoryDefinition minimal(ResourceLocation id) {
    return new InteractiveStoryDefinition(id, 1, LAYOUT, ActionList.EMPTY, List.of());
  }

  @AfterEach
  void clearRegistry() {
    InteractiveStoryContentRegistry.clear();
  }

  @Test
  void replaceAllIsAtomic() {
    InteractiveStoryContentRegistry.replaceAll(Map.of(TEST_ID_A, minimal(TEST_ID_A)));

    assertEquals(1, InteractiveStoryContentRegistry.size());
    assertTrue(InteractiveStoryContentRegistry.get(TEST_ID_A).isPresent());

    InteractiveStoryContentRegistry.replaceAll(Map.of(TEST_ID_B, minimal(TEST_ID_B)));

    assertEquals(1, InteractiveStoryContentRegistry.size());
    assertTrue(InteractiveStoryContentRegistry.get(TEST_ID_B).isPresent());
    assertTrue(InteractiveStoryContentRegistry.get(TEST_ID_A).isEmpty());
  }

  @Test
  void getReturnsEmptyForUnknown() {
    Optional<InteractiveStoryDefinition> result =
        InteractiveStoryContentRegistry.get(new ResourceLocation("test", "missing"));

    assertTrue(result.isEmpty());
  }

  @Test
  void allReturnsAllEntries() {
    InteractiveStoryContentRegistry.replaceAll(
        Map.of(TEST_ID_A, minimal(TEST_ID_A), TEST_ID_B, minimal(TEST_ID_B)));

    assertEquals(2, InteractiveStoryContentRegistry.all().size());
  }
}
