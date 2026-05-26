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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ActionDataSetTest {

  @Test
  void emptyByDefault() {
    ActionDataSet set = new ActionDataSet();
    assertTrue(set.isEmpty());
    assertEquals(0, set.entries().size());
  }

  @Test
  void addAndRetrieve() {
    ActionDataSet set = new ActionDataSet();
    ActionDataEntry entry = ActionDataEntry.runCommand("/say hi");
    set.add(entry);

    assertFalse(set.isEmpty());
    assertEquals(1, set.entries().size());
  }

  @Test
  void removeById() {
    ActionDataSet set = new ActionDataSet();
    ActionDataEntry entry = ActionDataEntry.runCommand("/say hi");
    set.add(entry);
    set.remove(entry.id());

    assertTrue(set.isEmpty());
  }

  @Test
  void replace() {
    ActionDataSet set = new ActionDataSet();
    ActionDataEntry original = ActionDataEntry.runCommand("/say hi");
    set.add(original);

    ActionDataEntry updated = original.withData(original.data());
    set.replace(updated);

    assertEquals(1, set.entries().size());
    assertEquals(original.id(), set.entries().iterator().next().id());
  }

  @Test
  void hasStoryActionOpenStory() {
    ActionDataSet set = new ActionDataSet();
    set.add(ActionDataEntry.openStory(new ResourceLocation("dqse", "s"), null));
    assertTrue(set.hasStoryAction());
  }

  @Test
  void hasStoryActionInteractive() {
    ActionDataSet set = new ActionDataSet();
    set.add(ActionDataEntry.openInteractiveStory(new ResourceLocation("dqse", "s")));
    assertTrue(set.hasStoryAction());
  }

  @Test
  void hasStoryActionFalseForCommand() {
    ActionDataSet set = new ActionDataSet();
    set.add(ActionDataEntry.runCommand("/say hi"));
    assertFalse(set.hasStoryAction());
  }

  @Test
  void nbtRoundTrip() {
    ActionDataSet set = new ActionDataSet();
    set.add(ActionDataEntry.openStory(new ResourceLocation("dqse", "s"), null));
    set.add(ActionDataEntry.runCommand("/say test"));

    CompoundTag tag = set.save();
    ActionDataSet restored = ActionDataSet.load(tag);

    assertEquals(2, restored.entries().size());
  }

  @Test
  void copyIsIndependent() {
    ActionDataSet original = new ActionDataSet();
    original.add(ActionDataEntry.runCommand("/say hi"));

    ActionDataSet copy = original.copy();
    copy.add(ActionDataEntry.runCommand("/say world"));

    assertEquals(1, original.entries().size());
    assertEquals(2, copy.entries().size());
    assertNotSame(original, copy);
  }
}
