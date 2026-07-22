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
import static org.junit.jupiter.api.Assertions.assertNull;

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class InteractionEntryTest {

  private static final UUID TARGET_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");

  @Test
  void forEntityInteractSetsExpectedFields() {
    InteractionEntry entry =
        InteractionEntry.forEntityInteract(
            TARGET_ID, InteractionType.RIGHT_CLICK, "NPC", OVERWORLD);

    assertEquals(InteractionSource.WAND, entry.source());
    assertEquals(InteractionEventType.ON_ENTITY_INTERACT, entry.eventType());
    assertEquals(TARGET_ID, entry.targetId());
    assertEquals(TargetKind.ENTITY, entry.targetKind());
    assertEquals(InteractionType.RIGHT_CLICK, entry.interactionType());
    assertEquals("NPC", entry.label());
    assertEquals(OVERWORLD, entry.dimension());
    assertNull(entry.blockPos());
  }

  @Test
  void forBlockInteractWithRightClickUsesBlockInteractType() {
    BlockPos position = new BlockPos(1, 64, 1);
    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID, position, TargetKind.BLOCK, InteractionType.RIGHT_CLICK, "Chest", OVERWORLD);

    assertEquals(InteractionEventType.ON_BLOCK_INTERACT, entry.eventType());
    assertEquals(TargetKind.BLOCK, entry.targetKind());
    assertEquals(position, entry.blockPos());
  }

  @Test
  void forBlockInteractWithStepOnUsesStepOnType() {
    BlockPos position = new BlockPos(5, 64, 5);
    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID,
            position,
            TargetKind.BLOCK,
            InteractionType.STEP_ON,
            "Pressure Plate",
            OVERWORLD);

    assertEquals(InteractionEventType.ON_STEP_ON, entry.eventType());
  }

  @Test
  void toStringIncludesSourceAndEventTypePath() {
    InteractionEntry entry =
        InteractionEntry.forEntityInteract(
            TARGET_ID, InteractionType.RIGHT_CLICK, "Goblin", OVERWORLD);
    String result = entry.toString();

    assertEquals(true, result.contains("WAND"));
    assertEquals(true, result.contains("on_entity_interact"));
    assertEquals(true, result.contains("Goblin"));
    assertEquals(true, result.contains("11111111"));
  }

  @Test
  void toStringIncludesBlockPosWhenPresent() {
    BlockPos position = new BlockPos(10, 64, 20);
    InteractionEntry entry =
        InteractionEntry.forBlockInteract(
            TARGET_ID, position, TargetKind.BLOCK, InteractionType.RIGHT_CLICK, "Door", OVERWORLD);

    assertEquals(true, entry.toString().contains(position.toShortString()));
  }

  @Test
  void fromNameReturnsNullForUnknownSource() {
    assertNull(InteractionSource.fromName("invalid_source"));
  }

  @Test
  void fromNameIsCaseInsensitive() {
    assertEquals(InteractionSource.WAND, InteractionSource.fromName("wand"));
    assertEquals(InteractionSource.WAND, InteractionSource.fromName("WAND"));
    assertEquals(InteractionSource.DATAPACK, InteractionSource.fromName("Datapack"));
    assertEquals(InteractionSource.API, InteractionSource.fromName("api"));
  }

  @Test
  void eventTypePathMatchesConstantName() {
    assertEquals(
        "on_entity_interact",
        new ResourceLocation(Constants.MOD_ID, "on_entity_interact").getPath());
  }
}
