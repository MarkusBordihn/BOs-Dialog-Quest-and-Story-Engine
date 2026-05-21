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

package de.markusbordihn.dialogqueststoryengine.content.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class InteractionDefinitionsTest {

  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "interaction_a");
  private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");

  @Test
  void toEntryReturnsEmptyForUnboundBinding() {
    InteractionDefinition definition =
        new InteractionDefinition(
            TEST_ID,
            1,
            InteractionEventType.ON_HOLOPAD_USE,
            InteractionBinding.UnboundBinding.INSTANCE,
            ConditionGroup.ALWAYS_TRUE,
            List.of());

    Optional<InteractionEntry> entry = InteractionDefinitions.toEntry(definition);

    assertTrue(entry.isEmpty());
  }

  @Test
  void toEntryPopulatesSourceAndLabelForEntity() {
    UUID targetId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
    InteractionDefinition definition =
        new InteractionDefinition(
            TEST_ID,
            1,
            InteractionEventType.ON_ENTITY_INTERACT,
            new InteractionBinding.EntityBinding(targetId, OVERWORLD),
            ConditionGroup.ALWAYS_TRUE,
            List.of());

    Optional<InteractionEntry> entry = InteractionDefinitions.toEntry(definition);

    assertTrue(entry.isPresent());
    assertEquals(TEST_ID.toString(), entry.get().label());
    assertEquals(targetId, entry.get().targetId());
    assertEquals(OVERWORLD, entry.get().dimension());
  }

  @Test
  void toEntryPopulatesBlockData() {
    BlockPos pos = new BlockPos(10, 64, -5);
    InteractionDefinition definition =
        new InteractionDefinition(
            TEST_ID,
            1,
            InteractionEventType.ON_BLOCK_INTERACT,
            new InteractionBinding.BlockBinding(pos, OVERWORLD),
            ConditionGroup.ALWAYS_TRUE,
            List.of());

    Optional<InteractionEntry> entry = InteractionDefinitions.toEntry(definition);

    assertTrue(entry.isPresent());
    assertEquals(OVERWORLD, entry.get().dimension());
    assertEquals(pos, entry.get().blockPos());
  }
}
