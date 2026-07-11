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

package de.markusbordihn.dialogqueststoryengine.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PlayerStateServiceTest {

  private static final UUID PLAYER_UUID = UUID.fromString("22222222-2222-2222-2222-222222222222");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "main_quest");

  @BeforeEach
  void setUp() {
    QuestTestFixtures.install(QUEST_ID);
  }

  @AfterEach
  void tearDown() {
    PlayerStateService.markPlayerDataSaved(PLAYER_UUID);
    PlayerStateService.onPlayerLoggedOut(PLAYER_UUID);
    PlayerStateEvents.clearAll();
    QuestTestFixtures.clear();
  }

  @Test
  void playerNotLoadedBeforeDataArrives() {
    assertFalse(PlayerStateService.isLoaded(PLAYER_UUID));
    assertTrue(PlayerStateService.get(PLAYER_UUID).isEmpty());
  }

  @Test
  void onPlayerDataLoadedCachesState() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    assertTrue(PlayerStateService.isLoaded(PLAYER_UUID));
    assertTrue(PlayerStateService.get(PLAYER_UUID).isPresent());
  }

  @Test
  void getPlayerDataForSaveReturnsNullWhenNotDirty() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    assertNull(PlayerStateService.getPlayerDataForSave(PLAYER_UUID));
  }

  @Test
  void getPlayerDataForSaveReturnsNbtWhenDirty() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.setFact(PLAYER_UUID, FactScope.PLAYER, "coins", FactValue.of(5L));

    CompoundTag savedNbt = PlayerStateService.getPlayerDataForSave(PLAYER_UUID);

    assertNotNull(savedNbt);
    assertNotNull(
        PlayerStateService.getPlayerDataForSave(PLAYER_UUID),
        "Dirty state must remain available until the write succeeds");
    PlayerStateService.markPlayerDataSaved(PLAYER_UUID);
    assertNull(
        PlayerStateService.getPlayerDataForSave(PLAYER_UUID),
        "Second call should return null — not dirty anymore");
  }

  @Test
  void onPlayerLoggedOutRetainsDirtyCache() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.setFact(PLAYER_UUID, FactScope.PLAYER, "coins", FactValue.of(5L));

    PlayerStateService.onPlayerLoggedOut(PLAYER_UUID);

    assertTrue(PlayerStateService.isLoaded(PLAYER_UUID));
  }

  @Test
  void onPlayerLoggedOutEvictsCache() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateService.onPlayerLoggedOut(PLAYER_UUID);

    assertFalse(PlayerStateService.isLoaded(PLAYER_UUID));
  }

  @Test
  void setFactFiresEvent() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    UUID[] firedUuid = new UUID[1];
    FactScope[] firedScope = new FactScope[1];
    PlayerStateEvents.addFactChangedListener(
        (uuid, scope, key, value) -> {
          firedUuid[0] = uuid;
          firedScope[0] = scope;
        });

    PlayerStateService.setFact(PLAYER_UUID, FactScope.PLAYER, "coins", FactValue.of(1L));

    assertEquals(PLAYER_UUID, firedUuid[0]);
    assertEquals(FactScope.PLAYER, firedScope[0]);
  }

  @Test
  void startQuestFiresEventAndInitializesSteps() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    UUID[] firedUuid = new UUID[1];
    ResourceLocation[] firedQuestId = new ResourceLocation[1];
    PlayerStateEvents.addQuestStartedListener(
        (uuid, questId, questProgress) -> {
          firedUuid[0] = uuid;
          firedQuestId[0] = questId;
        });

    var questProgress = PlayerStateService.startQuest(PLAYER_UUID, QUEST_ID);

    assertTrue(questProgress.isPresent());
    assertEquals(QuestState.ACTIVE, questProgress.get().state());
    assertEquals(PLAYER_UUID, firedUuid[0]);
    assertEquals(QUEST_ID, firedQuestId[0]);
  }

  @Test
  void startQuestTwiceReturnsSameActiveQuest() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    PlayerStateService.startQuest(PLAYER_UUID, QUEST_ID);
    var second = PlayerStateService.startQuest(PLAYER_UUID, QUEST_ID);

    assertTrue(second.isPresent());
    assertEquals(QuestState.ACTIVE, second.get().state());
  }

  @Test
  void progressStepUpdatesState() {
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerState playerState = PlayerStateService.get(PLAYER_UUID).get();
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep("kill_wolves", StepProgress.active(3));
    playerState.putQuestDirect(QUEST_ID, questProgress);

    var step = PlayerStateService.progressStep(PLAYER_UUID, QUEST_ID, "kill_wolves", 2);

    assertTrue(step.isPresent());
    assertEquals(2, step.get().progress());
    assertFalse(step.get().complete());
  }

  @Test
  void progressStepForUnloadedPlayerReturnsEmpty() {
    var step = PlayerStateService.progressStep(PLAYER_UUID, QUEST_ID, "step", 1);

    assertTrue(step.isEmpty());
  }

  @Test
  void playerStateLoadedEventFires() {
    UUID[] firedUuid = new UUID[1];
    PlayerStateEvents.addPlayerStateLoadedListener((uuid, state) -> firedUuid[0] = uuid);

    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());

    assertEquals(PLAYER_UUID, firedUuid[0]);
  }
}
