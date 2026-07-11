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

package de.markusbordihn.dialogqueststoryengine.content.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateCodec;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepState;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class QuestServiceTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  private static QuestDefinition quest(CompletionPolicy completionPolicy) {
    return new QuestDefinition(
        QUEST_ID,
        1,
        new DisplaySection("title", "desc", Optional.empty()),
        new LogicSection(
            Optional.empty(),
            Map.of(
                "collect",
                step("collect", "dqse:collect_item", "\"count\": 5"),
                "talk",
                step("talk", "dqse:manual", "")),
            completionPolicy,
            false,
            SyncScope.PLAYER),
        ActionList.EMPTY);
  }

  private static QuestDefinition rewardQuest(ResourceLocation questId) {
    return new QuestDefinition(
        questId,
        1,
        new DisplaySection("title", "desc", Optional.empty()),
        new LogicSection(
            Optional.empty(),
            Map.of("done", step("done", "dqse:manual", "")),
            CompletionPolicy.ALL_STEPS,
            false,
            SyncScope.PLAYER),
        new ActionList(
            List.of(
                context -> {
                  FactValue existing =
                      context.playerState().getFact(FactScope.PLAYER, "reward_count");
                  long current = existing instanceof FactValue.LongValue value ? value.value() : 0L;
                  context
                      .playerState()
                      .setFact(FactScope.PLAYER, "reward_count", FactValue.of(current + 1));
                })));
  }

  private static RawQuestStep step(String id, String type, String extraFields) {
    String suffix = extraFields.isEmpty() ? "" : ", " + extraFields;
    return new RawQuestStep(
        id,
        new ResourceLocation(type),
        JsonParser.parseString("{\"type\":\"" + type + "\"" + suffix + "}").getAsJsonObject());
  }

  @AfterEach
  void tearDown() {
    QuestContentRegistry.clear();
  }

  @Test
  void startQuestInitializesAllStepsAsActive() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ALL_STEPS)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());

    var result = QuestService.startQuest(ActionContext.ofTest(playerState), QUEST_ID);

    assertTrue(result.isPresent());
    assertEquals(QuestState.ACTIVE, result.get().questProgress().state());
    assertEquals(2, result.get().questProgress().steps().size());
    assertEquals(StepState.ACTIVE, result.get().questProgress().steps().get("collect").state());
    assertEquals(5, result.get().questProgress().steps().get("collect").required());
    assertEquals(StepState.ACTIVE, result.get().questProgress().steps().get("talk").state());
    assertEquals(1, result.get().questProgress().steps().get("talk").required());
  }

  @Test
  void startQuestRejectsMissingDefinition() {
    PlayerState playerState = new PlayerState(UUID.randomUUID());

    var result = QuestService.startQuest(ActionContext.ofTest(playerState), QUEST_ID);

    assertTrue(result.isEmpty());
    assertFalse(playerState.hasQuest(QUEST_ID));
  }

  @Test
  void allStepsCompletionCompletesQuestAfterEveryStepCompletes() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ALL_STEPS)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);

    QuestService.progressStep(context, QUEST_ID, "collect", 5);

    assertEquals(QuestState.ACTIVE, playerState.getQuest(QUEST_ID).state());

    QuestService.progressStep(context, QUEST_ID, "talk", 1);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
  }

  @Test
  void progressStepReportsOnlyChangedStep() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ALL_STEPS)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);

    var result = QuestService.progressStep(context, QUEST_ID, "collect", 2);

    assertTrue(result.isPresent());
    assertTrue(result.get().changed());
    assertEquals(1, result.get().changedSteps().size());
    assertTrue(result.get().changedSteps().containsKey("collect"));
    assertFalse(result.get().changedSteps().containsKey("talk"));
  }

  @Test
  void anyStepCompletionCompletesQuestAfterOneStepCompletes() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ANY_STEP)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);

    QuestService.progressStep(context, QUEST_ID, "talk", 1);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
  }

  @Test
  void setStepProgressOverwritesAndClampsProgress() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ALL_STEPS)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);

    QuestService.setStepProgress(context, QUEST_ID, "collect", 99);

    assertEquals(5, playerState.getQuest(QUEST_ID).steps().get("collect").progress());
    assertEquals(
        StepState.COMPLETED, playerState.getQuest(QUEST_ID).steps().get("collect").state());
  }

  @Test
  void missingStepIsRejectedWithoutCreatingProgressEntry() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ALL_STEPS)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);

    var result = QuestService.progressStep(context, QUEST_ID, "missing", 1);

    assertTrue(result.isEmpty());
    assertFalse(playerState.getQuest(QUEST_ID).steps().containsKey("missing"));
  }

  @Test
  void rewardsRunOnlyOnceForSameManualCompletion() {
    ResourceLocation rewardQuest = new ResourceLocation("test", "reward_quest");
    QuestContentRegistry.replaceAll(Map.of(rewardQuest, rewardQuest(rewardQuest)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, rewardQuest);

    QuestService.completeQuest(context, rewardQuest);
    QuestService.completeQuest(context, rewardQuest);

    FactValue rewardCount = playerState.getFact(FactScope.PLAYER, "reward_count");
    assertEquals(1L, ((FactValue.LongValue) rewardCount).value());
    assertEquals(
        playerState.getQuest(rewardQuest).revision(),
        playerState.getQuest(rewardQuest).lastRewardedRevision());
  }

  @Test
  void rewardsRunOnlyOnceForAutoCompletion() {
    ResourceLocation rewardQuest = new ResourceLocation("test", "reward_quest");
    QuestContentRegistry.replaceAll(Map.of(rewardQuest, rewardQuest(rewardQuest)));
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, rewardQuest);

    QuestService.progressStep(context, rewardQuest, "done", 1);
    QuestService.completeQuest(context, rewardQuest);

    FactValue rewardCount = playerState.getFact(FactScope.PLAYER, "reward_count");
    assertEquals(1L, ((FactValue.LongValue) rewardCount).value());
  }

  @Test
  void rewardMarkerSurvivesReloadAndPreventsSecondReward() {
    ResourceLocation rewardQuest = new ResourceLocation("test", "reward_quest");
    UUID playerUuid = UUID.randomUUID();
    QuestContentRegistry.replaceAll(Map.of(rewardQuest, rewardQuest(rewardQuest)));
    PlayerState playerState = new PlayerState(playerUuid);
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, rewardQuest);
    QuestService.completeQuest(context, rewardQuest);

    PlayerState restored =
        PlayerStateCodec.fromNbt(PlayerStateCodec.toNbt(playerState), playerUuid);
    QuestService.completeQuest(ActionContext.ofTest(restored), rewardQuest);

    FactValue rewardCount = restored.getFact(FactScope.PLAYER, "reward_count");
    assertEquals(1L, ((FactValue.LongValue) rewardCount).value());
    assertEquals(
        restored.getQuest(rewardQuest).revision(),
        restored.getQuest(rewardQuest).lastRewardedRevision());
  }

  @Test
  void twoPlayersProgressSameQuestIndependently() {
    QuestContentRegistry.replaceAll(Map.of(QUEST_ID, quest(CompletionPolicy.ALL_STEPS)));
    PlayerState firstPlayer = new PlayerState(UUID.randomUUID());
    PlayerState secondPlayer = new PlayerState(UUID.randomUUID());
    ActionContext firstContext = ActionContext.ofTest(firstPlayer);
    ActionContext secondContext = ActionContext.ofTest(secondPlayer);
    QuestService.startQuest(firstContext, QUEST_ID);
    QuestService.startQuest(secondContext, QUEST_ID);

    QuestService.progressStep(firstContext, QUEST_ID, "collect", 3);

    assertEquals(3, firstPlayer.getQuest(QUEST_ID).steps().get("collect").progress());
    assertEquals(0, secondPlayer.getQuest(QUEST_ID).steps().get("collect").progress());
  }
}
