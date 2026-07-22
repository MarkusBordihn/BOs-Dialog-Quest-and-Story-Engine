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

package de.markusbordihn.dialogqueststoryengine.quest.reward;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardClaimState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardClaimMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class RewardCompletionTest {

  private static final UUID PLAYER = UUID.fromString("66666666-6666-6666-6666-666666666666");
  private static final ResourceLocation MANUAL = new ResourceLocation("test", "manual_reward");
  private static final ResourceLocation AUTOMATIC = new ResourceLocation("test", "auto_reward");
  private static final ResourceLocation NO_REWARD = new ResourceLocation("test", "no_reward");

  private PlayerState playerState;

  private static QuestDefinition quest(ResourceLocation id, RewardSection rewards) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(
            Optional.empty(), QuestPrerequisites.NONE, Map.of(), CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        rewards);
  }

  @BeforeEach
  void setUp() {
    RewardEntry.Item emerald =
        new RewardEntry.Item(new ResourceLocation("minecraft", "emerald"), 1);
    Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    quests.put(
        MANUAL,
        quest(
            MANUAL,
            new RewardSection(
                RewardClaimMode.MANUAL, Optional.empty(), Optional.empty(), List.of(emerald))));
    quests.put(
        AUTOMATIC,
        quest(
            AUTOMATIC,
            new RewardSection(
                RewardClaimMode.AUTOMATIC, Optional.empty(), Optional.empty(), List.of(emerald))));
    quests.put(NO_REWARD, quest(NO_REWARD, RewardSection.EMPTY));
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
  void manualRewardBecomesAvailableOnCompletion() {
    QuestService.startQuest(PLAYER, MANUAL);
    QuestService.completeQuest(PLAYER, MANUAL);

    assertEquals(RewardClaimState.AVAILABLE, this.playerState.getQuest(MANUAL).rewardClaimState());
  }

  @Test
  void automaticRewardWithoutOnlinePlayerBecomesAvailable() {
    QuestService.startQuest(PLAYER, AUTOMATIC);
    QuestService.completeQuest(PLAYER, AUTOMATIC);

    assertEquals(
        RewardClaimState.AVAILABLE, this.playerState.getQuest(AUTOMATIC).rewardClaimState());
  }

  @Test
  void noRewardStaysNone() {
    QuestService.startQuest(PLAYER, NO_REWARD);
    QuestService.completeQuest(PLAYER, NO_REWARD);

    assertEquals(RewardClaimState.NONE, this.playerState.getQuest(NO_REWARD).rewardClaimState());
  }
}
