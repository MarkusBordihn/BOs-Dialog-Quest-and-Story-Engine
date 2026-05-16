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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InteractionStoreTest {

  private static final UUID TARGET_A = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final UUID TARGET_B = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final ResourceLocation OVERWORLD = new ResourceLocation("minecraft", "overworld");

  private InteractionStore store;

  @BeforeEach
  void freshStore() {
    this.store = new InteractionStore();
  }

  @Test
  void sizeIsZeroInitially() {
    assertEquals(0, this.store.size());
  }

  @Test
  void registerIncreasesSize() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "NPC", OVERWORLD));
    assertEquals(1, this.store.size());
  }

  @Test
  void registerReplacesSameEventType() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "Old", OVERWORLD));
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "New", OVERWORLD));

    assertEquals(1, this.store.size());
    assertEquals(
        "New",
        this.store.getInteraction(TARGET_A, InteractionEventType.ON_ENTITY_INTERACT).label());
  }

  @Test
  void registerAllowsDifferentEventTypesForSameTarget() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "Entity", OVERWORLD));
    this.store.register(
        InteractionEntry.forBlockInteract(
            TARGET_A,
            new BlockPos(0, 64, 0),
            TargetKind.BLOCK,
            InteractionType.RIGHT_CLICK,
            "Block",
            OVERWORLD));

    assertEquals(2, this.store.size());
  }

  @Test
  void unregisterRemovesEntry() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "NPC", OVERWORLD));
    boolean removed = this.store.unregister(TARGET_A, InteractionEventType.ON_ENTITY_INTERACT);

    assertTrue(removed);
    assertEquals(0, this.store.size());
    assertNull(this.store.getInteraction(TARGET_A, InteractionEventType.ON_ENTITY_INTERACT));
  }

  @Test
  void unregisterReturnsFalseWhenNotFound() {
    assertFalse(this.store.unregister(TARGET_A, InteractionEventType.ON_ENTITY_INTERACT));
  }

  @Test
  void unregisterAllRemovesAllForTarget() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "NPC", OVERWORLD));
    this.store.register(
        InteractionEntry.forBlockInteract(
            TARGET_A,
            new BlockPos(0, 64, 0),
            TargetKind.BLOCK,
            InteractionType.RIGHT_CLICK,
            "Block",
            OVERWORLD));
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_B, InteractionType.RIGHT_CLICK, "Other", OVERWORLD));

    assertEquals(2, this.store.unregisterAll(TARGET_A));
    assertEquals(1, this.store.size());
  }

  @Test
  void clearAllEmptiesStore() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "NPC", OVERWORLD));
    this.store.clearAll();

    assertEquals(0, this.store.size());
    assertTrue(this.store.getAllEntries().isEmpty());
  }

  @Test
  void hasStepOnInteractionsFalseByDefault() {
    assertFalse(this.store.hasStepOnInteractions());
  }

  @Test
  void hasStepOnInteractionsTrueAfterStepOnRegistered() {
    this.store.register(
        InteractionEntry.forBlockInteract(
            TARGET_A,
            new BlockPos(0, 64, 0),
            TargetKind.BLOCK,
            InteractionType.STEP_ON,
            "Plate",
            OVERWORLD));

    assertTrue(this.store.hasStepOnInteractions());
  }

  @Test
  void hasStepOnInteractionsFalseAfterUnregister() {
    this.store.register(
        InteractionEntry.forBlockInteract(
            TARGET_A,
            new BlockPos(0, 64, 0),
            TargetKind.BLOCK,
            InteractionType.STEP_ON,
            "Plate",
            OVERWORLD));
    this.store.unregister(TARGET_A, InteractionEventType.ON_STEP_ON);

    assertFalse(this.store.hasStepOnInteractions());
  }

  @Test
  void getAllEntriesContainsAllRegistered() {
    this.store.register(
        InteractionEntry.forEntityInteract(TARGET_A, InteractionType.RIGHT_CLICK, "A", OVERWORLD));
    this.store.register(
        InteractionEntry.forEntityInteract(TARGET_B, InteractionType.RIGHT_CLICK, "B", OVERWORLD));

    assertEquals(2, this.store.getAllEntries().size());
  }

  @Test
  void getInteractionsForTargetReturnsUnmodifiableEmptyListForUnknown() {
    assertTrue(this.store.getInteractionsForTarget(TARGET_A).isEmpty());
  }

  @Test
  void getFirstInteractionReturnsNullForUnknownTarget() {
    assertNull(this.store.getFirstInteraction(TARGET_A));
  }

  @Test
  void hasInteractionReflectsRegistrationState() {
    assertFalse(this.store.hasInteraction(TARGET_A, InteractionEventType.ON_ENTITY_INTERACT));
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "NPC", OVERWORLD));
    assertTrue(this.store.hasInteraction(TARGET_A, InteractionEventType.ON_ENTITY_INTERACT));
  }

  @Test
  void cachedAllEntriesRefreshedAfterRegister() {
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_A, InteractionType.RIGHT_CLICK, "First", OVERWORLD));
    assertNotNull(this.store.getAllEntries());
    this.store.register(
        InteractionEntry.forEntityInteract(
            TARGET_B, InteractionType.RIGHT_CLICK, "Second", OVERWORLD));
    assertEquals(2, this.store.getAllEntries().size());
  }
}
