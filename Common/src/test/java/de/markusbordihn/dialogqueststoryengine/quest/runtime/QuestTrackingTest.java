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
import static org.junit.jupiter.api.Assertions.assertNull;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestTrackingTest {

  private static final UUID PLAYER = UUID.fromString("77777777-7777-7777-7777-777777777777");
  private static final ResourceLocation QUEST_1 = new ResourceLocation("test", "quest_one");
  private static final ResourceLocation QUEST_2 = new ResourceLocation("test", "quest_two");

  private PlayerState playerState;

  @BeforeEach
  void setUp() {
    QuestTestFixtures.install(QUEST_1, QUEST_2);
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
  void firstStartedQuestIsAutoTracked() {
    QuestService.startQuest(PLAYER, QUEST_1);

    assertEquals(QUEST_1, this.playerState.trackedQuestId());
  }

  @Test
  void laterStartDoesNotReplaceTrackedQuest() {
    QuestService.startQuest(PLAYER, QUEST_1);
    QuestService.startQuest(PLAYER, QUEST_2);

    assertEquals(QUEST_1, this.playerState.trackedQuestId());
  }

  @Test
  void completionClearsTrackedQuest() {
    QuestService.startQuest(PLAYER, QUEST_1);
    QuestService.completeQuest(PLAYER, QUEST_1);

    assertNull(this.playerState.trackedQuestId());
  }

  @Test
  void failureClearsTrackedQuest() {
    QuestService.startQuest(PLAYER, QUEST_1);
    QuestService.failQuest(PLAYER, QUEST_1);

    assertNull(this.playerState.trackedQuestId());
  }

  @Test
  void trackedQuestPersistsThroughReload() {
    QuestService.startQuest(PLAYER, QUEST_1);

    CompoundTag saved = PlayerStateService.getPlayerDataForSave(PLAYER);
    PlayerStateService.markPlayerDataSaved(PLAYER);
    PlayerStateService.onPlayerLoggedOut(PLAYER);
    PlayerStateService.onPlayerDataLoaded(PLAYER, saved);

    assertEquals(QUEST_1, PlayerStateService.get(PLAYER).orElseThrow().trackedQuestId());
  }
}
