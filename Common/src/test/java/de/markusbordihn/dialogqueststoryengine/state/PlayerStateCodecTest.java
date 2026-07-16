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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class PlayerStateCodecTest {

  private static final UUID TEST_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "main_quest");
  private static final ResourceLocation STORY_ID = new ResourceLocation("test", "prologue");

  @Test
  void emptyStateRoundTrip() {
    PlayerState original = new PlayerState(TEST_UUID);
    CompoundTag nbt = PlayerStateCodec.toNbt(original);
    PlayerState restored = PlayerStateCodec.fromNbt(nbt, TEST_UUID);

    assertEquals(TEST_UUID, restored.playerUuid());
    assertTrue(restored.allQuests().isEmpty());
    assertTrue(restored.stories().unlockedIds().isEmpty());
    assertFalse(restored.isDirty());
  }

  @Test
  void factRoundTrip() {
    PlayerState original = new PlayerState(TEST_UUID);
    original.setFact(FactScope.PLAYER, "coins", FactValue.of(100L));
    original.setFact(FactScope.PLAYER, "name", FactValue.of("hero"));

    PlayerState restored = PlayerStateCodec.fromNbt(PlayerStateCodec.toNbt(original), TEST_UUID);

    FactValue coins = restored.getFact(FactScope.PLAYER, "coins");
    assertNotNull(coins);
    assertEquals(100L, ((FactValue.LongValue) coins).value());

    FactValue name = restored.getFact(FactScope.PLAYER, "name");
    assertEquals("hero", ((FactValue.StringValue) name).value());
  }

  @Test
  void questRoundTrip() {
    PlayerState original = new PlayerState(TEST_UUID);
    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    questProgress.putStep("kill_wolves", StepProgress.active(5));
    questProgress.incrementStep("kill_wolves", 3);
    original.putQuestDirect(QUEST_ID, questProgress);

    PlayerState restored = PlayerStateCodec.fromNbt(PlayerStateCodec.toNbt(original), TEST_UUID);

    assertTrue(restored.hasQuest(QUEST_ID));
    QuestProgress restoredQuest = restored.getQuest(QUEST_ID);
    assertEquals(QuestState.ACTIVE, restoredQuest.state());
    StepProgress restoredStep = restoredQuest.steps().get("kill_wolves");
    assertNotNull(restoredStep);
    assertEquals(3, restoredStep.progress());
    assertEquals(5, restoredStep.required());
    assertFalse(restoredStep.complete());
  }

  @Test
  void completedQuestRoundTrip() {
    PlayerState original = new PlayerState(TEST_UUID);
    QuestProgress questProgress = new QuestProgress(QuestState.COMPLETED);
    questProgress.putStep("find_artifact", StepProgress.completed(1));
    questProgress.setLastRewardedRevision(questProgress.revision());
    original.putQuestDirect(QUEST_ID, questProgress);

    PlayerState restored = PlayerStateCodec.fromNbt(PlayerStateCodec.toNbt(original), TEST_UUID);

    QuestProgress restoredQuest = restored.getQuest(QUEST_ID);
    assertEquals(QuestState.COMPLETED, restoredQuest.state());
    StepProgress restoredStep = restoredQuest.steps().get("find_artifact");
    assertTrue(restoredStep.complete());
    assertEquals(StepState.COMPLETED, restoredStep.state());
    assertEquals(questProgress.lastRewardedRevision(), restoredQuest.lastRewardedRevision());
  }

  @Test
  void storyRoundTrip() {
    PlayerState original = new PlayerState(TEST_UUID);
    original.unlockStory(STORY_ID);
    original.markStoryRead(STORY_ID);

    PlayerState restored = PlayerStateCodec.fromNbt(PlayerStateCodec.toNbt(original), TEST_UUID);

    assertTrue(restored.stories().isUnlocked(STORY_ID));
    assertTrue(restored.stories().isRead(STORY_ID));
  }

  @Test
  void corruptNbtReturnsEmptyState() {
    CompoundTag corruptNbt = new CompoundTag();
    corruptNbt.putString("dialog_quest_and_story_engine", "not_a_compound");

    PlayerState recovered = PlayerStateCodec.fromNbt(corruptNbt, TEST_UUID);

    assertEquals(TEST_UUID, recovered.playerUuid());
    assertTrue(recovered.allQuests().isEmpty());
  }

  @Test
  void emptyNbtReturnsEmptyState() {
    PlayerState recovered = PlayerStateCodec.fromNbt(new CompoundTag(), TEST_UUID);

    assertEquals(TEST_UUID, recovered.playerUuid());
    assertTrue(recovered.allQuests().isEmpty());
  }

  @Test
  void restoredStateIsNotDirty() {
    PlayerState original = new PlayerState(TEST_UUID);
    original.setFact(FactScope.PLAYER, "x", FactValue.of(1L));

    PlayerState restored = PlayerStateCodec.fromNbt(PlayerStateCodec.toNbt(original), TEST_UUID);

    assertFalse(restored.isDirty());
  }
}
