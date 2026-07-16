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

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestChangeResult;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestChangeEventTest {

  private static final UUID PLAYER_UUID = UUID.fromString("44444444-4444-4444-4444-444444444444");
  private static final ResourceLocation QUEST_1 = new ResourceLocation("test", "quest_one");

  private final List<QuestChangeResult> changes = new ArrayList<>();

  @BeforeEach
  void setUp() {
    QuestTestFixtures.install(QUEST_1);
    PlayerStateService.onPlayerDataLoaded(PLAYER_UUID, new CompoundTag());
    PlayerStateEvents.addQuestChangedListener((uuid, change) -> changes.add(change));
  }

  @AfterEach
  void tearDown() {
    PlayerStateService.markPlayerDataSaved(PLAYER_UUID);
    PlayerStateService.onPlayerLoggedOut(PLAYER_UUID);
    PlayerStateEvents.clearAll();
    QuestTestFixtures.clear();
  }

  @Test
  void startQuestPublishesExactlyOneChange() {
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    assertEquals(1, changes.size());
    assertEquals(QUEST_1, changes.get(0).questId());
    assertEquals(QuestState.ACTIVE, changes.get(0).questProgress().state());
  }

  @Test
  void completeQuestPublishesExactlyOneChange() {
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);
    changes.clear();

    PlayerStateService.completeQuest(PLAYER_UUID, QUEST_1);

    assertEquals(1, changes.size());
    assertEquals(QuestState.COMPLETED, changes.get(0).questProgress().state());
  }

  @Test
  void redundantStartPublishesNoChange() {
    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);
    changes.clear();

    PlayerStateService.startQuest(PLAYER_UUID, QUEST_1);

    assertEquals(0, changes.size());
  }
}
