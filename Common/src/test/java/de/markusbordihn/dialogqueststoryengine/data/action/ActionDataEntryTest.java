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

package de.markusbordihn.dialogqueststoryengine.data.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ActionDataEntryTest {

  @Test
  void openStoryRoundTrip() {
    ResourceLocation storyId = new ResourceLocation("dqse", "my_story");
    ResourceLocation themeId = new ResourceLocation("dqse", "my_theme");
    ActionDataEntry entry = ActionDataEntry.openStory(storyId, themeId);

    CompoundTag tag = entry.save();
    ActionDataEntry restored = ActionDataEntry.load(tag);

    assertNotNull(restored);
    assertEquals(ActionType.OPEN_STORY, restored.type());
    assertEquals(storyId, restored.storyId());
    assertEquals(themeId, restored.themeOverrideId());
  }

  @Test
  void openStoryWithoutThemeRoundTrip() {
    ResourceLocation storyId = new ResourceLocation("dqse", "my_story");
    ActionDataEntry entry = ActionDataEntry.openStory(storyId, null);

    CompoundTag tag = entry.save();
    ActionDataEntry restored = ActionDataEntry.load(tag);

    assertNotNull(restored);
    assertEquals(ActionType.OPEN_STORY, restored.type());
    assertEquals(storyId, restored.storyId());
    assertNull(restored.themeOverrideId());
  }

  @Test
  void openInteractiveStoryRoundTrip() {
    ResourceLocation storyId = new ResourceLocation("dqse", "interactive_story");
    ActionDataEntry entry = ActionDataEntry.openInteractiveStory(storyId);

    CompoundTag tag = entry.save();
    ActionDataEntry restored = ActionDataEntry.load(tag);

    assertNotNull(restored);
    assertEquals(ActionType.OPEN_INTERACTIVE_STORY, restored.type());
    assertEquals(storyId, restored.storyId());
  }

  @Test
  void runCommandRoundTrip() {
    ActionDataEntry entry = ActionDataEntry.runCommand("/say hello");

    CompoundTag tag = entry.save();
    ActionDataEntry restored = ActionDataEntry.load(tag);

    assertNotNull(restored);
    assertEquals(ActionType.RUN_COMMAND, restored.type());
    assertEquals("/say hello", restored.command());
  }

  @Test
  void setFactRoundTrip() {
    ResourceLocation factId = new ResourceLocation("dqse", "my_fact");
    ActionDataEntry entry = ActionDataEntry.setFact(factId, "true");

    CompoundTag tag = entry.save();
    ActionDataEntry restored = ActionDataEntry.load(tag);

    assertNotNull(restored);
    assertEquals(ActionType.SET_FACT, restored.type());
    assertEquals(factId, restored.factId());
    assertEquals("true", restored.factValue());
  }

  @Test
  void idPreservedOnRoundTrip() {
    ActionDataEntry entry = ActionDataEntry.runCommand("/say test");
    CompoundTag tag = entry.save();
    ActionDataEntry restored = ActionDataEntry.load(tag);

    assertNotNull(restored);
    assertEquals(entry.id(), restored.id());
  }
}
