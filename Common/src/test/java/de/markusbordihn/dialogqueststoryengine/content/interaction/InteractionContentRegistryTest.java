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

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class InteractionContentRegistryTest {

  private static final ResourceLocation TEST_ID_A = new ResourceLocation("test", "interaction_a");
  private static final ResourceLocation TEST_ID_B = new ResourceLocation("test", "interaction_b");
  private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");

  @AfterEach
  void clearRegistry() {
    InteractionContentRegistry.clear();
  }

  @Test
  void replaceAllIsAtomic() {
    InteractionDefinition definitionA =
        new InteractionDefinition(
            TEST_ID_A,
            1,
            InteractionEventType.ON_ENTITY_INTERACT,
            new InteractionBinding.EntityBinding(UUID.randomUUID(), OVERWORLD),
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY);

    InteractionContentRegistry.replaceAll(Map.of(TEST_ID_A, definitionA));

    assertEquals(1, InteractionContentRegistry.size());
    assertTrue(InteractionContentRegistry.get(TEST_ID_A).isPresent());

    InteractionDefinition definitionB =
        new InteractionDefinition(
            TEST_ID_B,
            1,
            InteractionEventType.ON_HOLOPAD_USE,
            InteractionBinding.UnboundBinding.INSTANCE,
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY);

    InteractionContentRegistry.replaceAll(Map.of(TEST_ID_B, definitionB));

    assertEquals(1, InteractionContentRegistry.size());
    assertTrue(InteractionContentRegistry.get(TEST_ID_B).isPresent());
    assertTrue(InteractionContentRegistry.get(TEST_ID_A).isEmpty());
  }

  @Test
  void getReturnsEmptyForUnknown() {
    Optional<InteractionDefinition> result =
        InteractionContentRegistry.get(new ResourceLocation("test", "missing"));

    assertTrue(result.isEmpty());
  }

  @Test
  void allReturnsAllEntries() {
    InteractionDefinition definitionA =
        new InteractionDefinition(
            TEST_ID_A,
            1,
            InteractionEventType.ON_ENTITY_INTERACT,
            new InteractionBinding.EntityBinding(UUID.randomUUID(), OVERWORLD),
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY);
    InteractionDefinition definitionB =
        new InteractionDefinition(
            TEST_ID_B,
            1,
            InteractionEventType.ON_HOLOPAD_USE,
            InteractionBinding.UnboundBinding.INSTANCE,
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY);

    InteractionContentRegistry.replaceAll(Map.of(TEST_ID_A, definitionA, TEST_ID_B, definitionB));

    Collection<InteractionDefinition> all = InteractionContentRegistry.all();
    assertEquals(2, all.size());
  }
}
