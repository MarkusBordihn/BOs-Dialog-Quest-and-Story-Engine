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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class InteractionEntryActionDataTest {

  private static final UUID TARGET_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");
  private static final BlockPos POS = new BlockPos(10, 64, 10);

  @Test
  void forBlockInteractHasEmptyActionDataSetByDefault() {
    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID, POS, TargetKind.BLOCK, InteractionType.OPEN_HOLOPAD, "Holopad", OVERWORLD);

    assertNotNull(entry.actionDataSet());
    assertTrue(entry.actionDataSet().isEmpty());
  }

  @Test
  void withEditsPreservesActionDataSet() {
    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.openStory(new ResourceLocation("dqse", "s"), null));

    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID, POS, TargetKind.BLOCK, InteractionType.OPEN_HOLOPAD, "Holopad", OVERWORLD);
    InteractionEntry updated =
        entry.withEdits(InteractionType.OPEN_HOLOPAD, "Updated", actionDataSet);

    assertFalse(updated.actionDataSet().isEmpty());
    assertEquals(1, updated.actionDataSet().entries().size());
  }

  @Test
  void nbtRoundTripWithActionDataSet() {
    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.runCommand("/say hello"));
    actionDataSet.add(ActionDataEntry.openStory(new ResourceLocation("dqse", "s"), null));

    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
                TARGET_ID,
                POS,
                TargetKind.BLOCK,
                InteractionType.OPEN_HOLOPAD,
                "Holopad",
                OVERWORLD)
            .withEdits(InteractionType.OPEN_HOLOPAD, "Holopad", actionDataSet);

    CompoundTag tag = entry.save();
    InteractionEntry restored = InteractionEntry.load(tag);

    assertNotNull(restored);
    assertFalse(restored.actionDataSet().isEmpty());
    assertEquals(2, restored.actionDataSet().entries().size());
  }

  @Test
  void nbtRoundTripEmptyActionDataSetOmitsTag() {
    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID, POS, TargetKind.BLOCK, InteractionType.RIGHT_CLICK, "Block", OVERWORLD);

    CompoundTag tag = entry.save();
    assertFalse(tag.contains("ActionData"));

    InteractionEntry restored = InteractionEntry.load(tag);
    assertNotNull(restored);
    assertTrue(restored.actionDataSet().isEmpty());
  }

  @Test
  void holopadInteractionTypeResolvesToOnHolopadUseEvent() {
    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID, POS, TargetKind.BLOCK, InteractionType.OPEN_HOLOPAD, "Holopad", OVERWORLD);

    assertEquals(InteractionEventType.ON_HOLOPAD_USE, entry.eventType());
  }
}
