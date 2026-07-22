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

package de.markusbordihn.dialogqueststoryengine.quest.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestAvailability;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestDisplayCatalogEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.PrerequisiteMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import io.netty.buffer.Unpooled;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestDisplayCatalogTest {

  private static final UUID PLAYER = UUID.fromString("88888888-8888-8888-8888-888888888888");
  private static final ResourceLocation QUEST_A = new ResourceLocation("test", "quest_a");
  private static final ResourceLocation QUEST_B = new ResourceLocation("test", "quest_b");
  private static final ResourceLocation MISSING = new ResourceLocation("test", "missing");

  private PlayerState playerState;

  private static QuestDefinition definition(ResourceLocation id) {
    return QuestContentRegistry.get(id).orElseThrow();
  }

  private static QuestDefinition questA() {
    RawQuestStep step =
        new RawQuestStep(
            "step_a",
            new ResourceLocation("dqse", "manual"),
            Optional.empty(),
            List.of(),
            new JsonObject());
    Map<String, RawQuestStep> steps = new LinkedHashMap<>();
    steps.put("step_a", step);
    return new QuestDefinition(
        QUEST_A,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "quest.test.quest_a.title",
            "quest.test.quest_a.description",
            Optional.of(new ResourceLocation("test", "category")),
            Optional.empty(),
            Optional.empty(),
            0),
        new LogicSection(
            Optional.empty(), QuestPrerequisites.NONE, steps, CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }

  private static QuestDefinition questB() {
    Condition alwaysVisible = context -> true;
    return new QuestDefinition(
        QUEST_B,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "quest.test.quest_b.title",
            "quest.test.quest_b.description",
            Optional.empty(),
            Optional.empty(),
            Optional.empty(),
            0),
        new LogicSection(
            Optional.of(alwaysVisible),
            new QuestPrerequisites(PrerequisiteMode.ANY, List.of(QUEST_A, MISSING)),
            Map.of(),
            CompletionPolicy.ALL_STEPS,
            true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }

  @BeforeEach
  void setUp() {
    Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    quests.put(QUEST_A, questA());
    quests.put(QUEST_B, questB());
    QuestTestFixtures.installDefinitions(quests);
    PlayerStateService.onPlayerDataLoaded(PLAYER, new CompoundTag());
    this.playerState = PlayerStateService.get(PLAYER).orElseThrow();
  }

  @AfterEach
  void tearDown() {
    PlayerStateService.markPlayerDataSaved(PLAYER);
    PlayerStateService.onPlayerLoggedOut(PLAYER);
    PlayerStateEvents.clearAll();
    QuestTestFixtures.clear();
  }

  @Test
  void derivesCategoryAndStepKeysAndActiveAvailability() {
    QuestService.startQuest(PLAYER, QUEST_A);

    QuestDisplayCatalogEntry entry =
        QuestDisplayCatalogService.buildEntry(null, this.playerState, definition(QUEST_A));

    assertEquals("quest_category.test.category", entry.categoryKey());
    assertEquals("quest.test.quest_a.step.step_a", entry.steps().get(0).descriptionKey());
    assertEquals(QuestAvailability.ACTIVE, entry.derivedAvailability());
  }

  @Test
  void visiblePrerequisitesFilterAndAvailabilityLockedThenAvailable() {
    QuestService.startQuest(PLAYER, QUEST_A);

    QuestDisplayCatalogEntry locked =
        QuestDisplayCatalogService.buildEntry(null, this.playerState, definition(QUEST_B));
    assertEquals(List.of(QUEST_A), locked.visiblePrerequisiteIds());
    assertEquals(QuestAvailability.LOCKED, locked.derivedAvailability());

    QuestService.completeQuest(PLAYER, QUEST_A);
    QuestDisplayCatalogEntry available =
        QuestDisplayCatalogService.buildEntry(null, this.playerState, definition(QUEST_B));
    assertEquals(QuestAvailability.AVAILABLE, available.derivedAvailability());
  }

  @Test
  void catalogEntryRoundTripsThroughBuffer() {
    QuestService.startQuest(PLAYER, QUEST_A);
    QuestDisplayCatalogEntry original =
        QuestDisplayCatalogService.buildEntry(null, this.playerState, definition(QUEST_B));

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    QuestDisplayCatalogEntry decoded = QuestDisplayCatalogEntry.read(buffer);

    assertEquals(original, decoded);
    assertTrue(decoded.visiblePrerequisiteIds().contains(QUEST_A));
  }
}
