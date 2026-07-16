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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardClaimState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardClaimMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.quest.reward.RewardGrantService;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepEvents;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class QuestService {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private QuestService() {}

  public static Optional<QuestChangeResult> startQuest(
      ActionContext actionContext, ResourceLocation questId) {
    return startQuest(actionContext.playerState(), questId);
  }

  public static Optional<QuestChangeResult> startQuest(
      ServerPlayer player, ResourceLocation questId) {
    return PlayerStateService.get(player.getUUID())
        .flatMap(
            playerState ->
                startQuest(
                    new ActionContext(
                        player,
                        playerState,
                        player.server,
                        "command-quest-start",
                        Optional.empty()),
                    questId));
  }

  public static Optional<QuestChangeResult> startQuest(UUID playerUuid, ResourceLocation questId) {
    return PlayerStateService.get(playerUuid)
        .flatMap(playerState -> startQuest(playerState, questId));
  }

  public static Optional<QuestChangeResult> startQuest(
      PlayerState playerState, ResourceLocation questId) {
    return startQuest(playerState, questId, false);
  }

  public static Optional<QuestChangeResult> forceStartQuest(
      PlayerState playerState, ResourceLocation questId) {
    return startQuest(playerState, questId, true);
  }

  public static Optional<QuestChangeResult> forceStartQuest(
      UUID playerUuid, ResourceLocation questId) {
    return PlayerStateService.get(playerUuid)
        .flatMap(playerState -> startQuest(playerState, questId, true));
  }

  private static Optional<QuestChangeResult> startQuest(
      PlayerState playerState, ResourceLocation questId, boolean force) {
    Optional<QuestDefinition> definitionOpt = QuestContentRegistry.get(questId);
    if (definitionOpt.isEmpty()) {
      log.warn("{} startQuest: quest '{}' is not loaded.", Constants.LOG_PREFIX, questId);
      return Optional.empty();
    }
    QuestDefinition definition = definitionOpt.get();

    QuestProgress existing = playerState.getQuest(questId);
    if (existing != null) {
      QuestState state = existing.state();
      if (state == QuestState.ACTIVE || state == QuestState.COMPLETED) {
        return publish(playerState, new QuestChangeResult(questId, existing, Map.of(), false));
      }
      if (state == QuestState.FAILED && !force && !definition.logic().restartAfterFailure()) {
        log.debug(
            "{} startQuest: FAILED quest '{}' is a dead end (restart_after_failure=false).",
            Constants.LOG_PREFIX,
            questId);
        return Optional.empty();
      }
    }

    if (!force
        && !QuestAvailabilityService.prerequisitesMet(
            playerState, definition.logic().prerequisites())) {
      log.debug(
          "{} startQuest: prerequisites not met for '{}' - start rejected.",
          Constants.LOG_PREFIX,
          questId);
      return Optional.empty();
    }

    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    Map<String, StepProgress> changedSteps = initialiseSteps(questId, questProgress);

    playerState.putQuestDirect(questId, questProgress);
    QuestTrackingService.autoTrackIfNone(resolvePlayer(playerState, null), playerState, questId);
    PlayerStateEvents.fireQuestStarted(playerState.playerUuid(), questId, questProgress);
    QuestStepEvents.handleQuestStarted(playerState, questId);
    return publish(playerState, new QuestChangeResult(questId, questProgress, changedSteps, true));
  }

  public static Optional<QuestChangeResult> completeQuest(
      ActionContext actionContext, ResourceLocation questId) {
    return completeQuest(actionContext.playerState(), actionContext, questId);
  }

  public static Optional<QuestChangeResult> completeQuest(
      ServerPlayer player, ResourceLocation questId) {
    return PlayerStateService.get(player.getUUID())
        .flatMap(
            playerState ->
                completeQuest(
                    new ActionContext(
                        player,
                        playerState,
                        player.server,
                        "command-quest-complete",
                        Optional.empty()),
                    questId));
  }

  public static Optional<QuestChangeResult> completeQuest(
      UUID playerUuid, ResourceLocation questId) {
    return PlayerStateService.get(playerUuid)
        .flatMap(playerState -> completeQuest(playerState, null, questId));
  }

  public static Optional<QuestChangeResult> failQuest(
      ActionContext actionContext, ResourceLocation questId) {
    return failQuest(actionContext.playerState(), questId);
  }

  public static Optional<QuestChangeResult> failQuest(UUID playerUuid, ResourceLocation questId) {
    return PlayerStateService.get(playerUuid)
        .flatMap(playerState -> failQuest(playerState, questId));
  }

  private static Optional<QuestChangeResult> failQuest(
      PlayerState playerState, ResourceLocation questId) {
    QuestProgress questProgress = playerState.getQuest(questId);
    if (questProgress == null) {
      log.warn(
          "{} failQuest: quest '{}' not found in player state.", Constants.LOG_PREFIX, questId);
      return Optional.empty();
    }

    if (questProgress.state() == QuestState.FAILED
        || questProgress.state() == QuestState.COMPLETED) {
      return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), false));
    }

    questProgress.setState(QuestState.FAILED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestFailed(playerState.playerUuid(), questId, questProgress);
    QuestTrackingService.clearIfTracked(resolvePlayer(playerState, null), playerState, questId);
    return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), true));
  }

  public static Optional<QuestChangeResult> progressStep(
      ActionContext actionContext, ResourceLocation questId, String stepId, int amount) {
    return progressStep(actionContext.playerState(), actionContext, questId, stepId, amount, false);
  }

  public static Optional<QuestChangeResult> progressStep(
      UUID playerUuid, ResourceLocation questId, String stepId, int amount) {
    return PlayerStateService.get(playerUuid)
        .flatMap(playerState -> progressStep(playerState, null, questId, stepId, amount, false));
  }

  public static Optional<QuestChangeResult> setStepProgress(
      ActionContext actionContext, ResourceLocation questId, String stepId, int value) {
    return progressStep(actionContext.playerState(), actionContext, questId, stepId, value, true);
  }

  public static Optional<QuestChangeResult> setStepProgress(
      UUID playerUuid, ResourceLocation questId, String stepId, int value) {
    return PlayerStateService.get(playerUuid)
        .flatMap(playerState -> progressStep(playerState, null, questId, stepId, value, true));
  }

  private static Optional<QuestChangeResult> completeQuest(
      PlayerState playerState, ActionContext actionContext, ResourceLocation questId) {
    QuestProgress questProgress = playerState.getQuest(questId);
    if (questProgress == null) {
      log.warn(
          "{} completeQuest: quest '{}' not found in player state.", Constants.LOG_PREFIX, questId);
      return Optional.empty();
    }

    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    if (questProgress.state() == QuestState.COMPLETED) {
      boolean applied =
          applyCompletionActionsOnce(playerState, actionContext, definition, questProgress);
      return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), applied));
    }

    finalizeCompletion(playerState, actionContext, definition, questId, questProgress);
    return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), true));
  }

  private static Optional<QuestChangeResult> progressStep(
      PlayerState playerState,
      ActionContext actionContext,
      ResourceLocation questId,
      String stepId,
      int value,
      boolean overwrite) {
    QuestProgress questProgress = playerState.getQuest(questId);
    if (questProgress == null) {
      log.warn(
          "{} progressStep: quest '{}' not found in player state.", Constants.LOG_PREFIX, questId);
      return Optional.empty();
    }

    if (questProgress.state() != QuestState.ACTIVE) {
      return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), false));
    }

    StepProgress current = questProgress.steps().get(stepId);
    if (current == null) {
      log.warn(
          "{} progressStep: step '{}' not found in quest '{}'.",
          Constants.LOG_PREFIX,
          stepId,
          questId);
      return Optional.empty();
    }

    if (current.state() != StepState.ACTIVE) {
      return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), false));
    }

    StepProgress updated = updateStepProgress(current, value, overwrite);
    if (updated.equals(current)) {
      return publish(playerState, new QuestChangeResult(questId, questProgress, Map.of(), false));
    }

    questProgress.putStep(stepId, updated);
    playerState.markDirty();
    PlayerStateEvents.fireStepProgressed(playerState.playerUuid(), questId, stepId, updated);

    Map<String, StepProgress> changedSteps = new LinkedHashMap<>();
    changedSteps.put(stepId, updated);
    if (updated.complete()) {
      activateDependents(playerState, questId, questProgress, changedSteps);
    }
    boolean completed =
        evaluateCompletion(playerState, actionContext, questId, questProgress, changedSteps);
    return publish(
        playerState, new QuestChangeResult(questId, questProgress, changedSteps, completed));
  }

  private static Optional<QuestChangeResult> publish(
      PlayerState playerState, QuestChangeResult result) {
    if (result.changed()) {
      PlayerStateEvents.fireQuestChanged(playerState.playerUuid(), result);
    }
    return Optional.of(result);
  }

  private static Map<String, StepProgress> initialiseSteps(
      ResourceLocation questId, QuestProgress questProgress) {
    Map<String, StepProgress> changedSteps = new LinkedHashMap<>();
    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    if (definition.isEmpty()) {
      log.warn(
          "{} Quest {} not found in registry - starting with empty steps.",
          Constants.LOG_PREFIX,
          questId);
      return changedSteps;
    }

    for (RawQuestStep step : definition.get().logic().steps().values()) {
      int required = requiredAmountFor(step);
      StepProgress stepProgress =
          step.requires().isEmpty() ? StepProgress.active(required) : StepProgress.locked(required);
      questProgress.putStep(step.id(), stepProgress);
      changedSteps.put(step.id(), stepProgress);
    }
    return changedSteps;
  }

  private static StepProgress updateStepProgress(
      StepProgress current, int value, boolean overwrite) {
    if (current.required() <= 0) {
      return StepProgress.completed(0);
    }
    int newProgress = overwrite ? Math.max(0, value) : current.progress() + Math.max(0, value);
    return current.withProgress(newProgress);
  }

  private static boolean evaluateCompletion(
      PlayerState playerState,
      ActionContext actionContext,
      ResourceLocation questId,
      QuestProgress questProgress,
      Map<String, StepProgress> changedSteps) {
    if (questProgress.state() != QuestState.ACTIVE) {
      return false;
    }

    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    CompletionPolicy policy =
        definition
            .map(questDefinition -> questDefinition.logic().completionPolicy())
            .orElse(CompletionPolicy.ALL_STEPS);

    Collection<StepProgress> steps = questProgress.steps().values();
    boolean complete =
        switch (policy) {
          case ANY_STEP -> steps.stream().anyMatch(StepProgress::complete);
          case ALL_STEPS -> {
            List<StepProgress> nonHidden =
                steps.stream().filter(step -> step.state() != StepState.HIDDEN).toList();
            yield !nonHidden.isEmpty() && nonHidden.stream().allMatch(StepProgress::done);
          }
        };

    if (!complete) {
      return false;
    }

    if (policy == CompletionPolicy.ANY_STEP) {
      skipRemainingSteps(questProgress, changedSteps);
    }

    finalizeCompletion(playerState, actionContext, definition, questId, questProgress);
    return true;
  }

  private static void finalizeCompletion(
      PlayerState playerState,
      ActionContext actionContext,
      Optional<QuestDefinition> definition,
      ResourceLocation questId,
      QuestProgress questProgress) {
    questProgress.setState(QuestState.COMPLETED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestCompleted(playerState.playerUuid(), questId, questProgress);
    QuestStepEvents.handleQuestCompleted(playerState, questId);
    applyCompletionActionsOnce(playerState, actionContext, definition, questProgress);
    definition.ifPresent(
        quest -> applyCompletionRewards(playerState, actionContext, quest, questProgress));
    QuestTrackingService.clearIfTracked(
        resolvePlayer(playerState, actionContext), playerState, questId);
  }

  private static void applyCompletionRewards(
      PlayerState playerState,
      ActionContext actionContext,
      QuestDefinition definition,
      QuestProgress questProgress) {
    RewardSection rewards = definition.rewards();
    if (rewards.isEmpty() || questProgress.rewardClaimState() != RewardClaimState.NONE) {
      return;
    }

    if (rewards.claimMode() == RewardClaimMode.MANUAL) {
      questProgress.setRewardClaimState(RewardClaimState.AVAILABLE);
      return;
    }

    ServerPlayer player = resolvePlayer(playerState, actionContext);
    if (player == null) {
      questProgress.setRewardClaimState(RewardClaimState.AVAILABLE);
      return;
    }

    boolean granted = RewardGrantService.grantAll(player, rewards.entries()).isEmpty();
    questProgress.setRewardClaimState(
        granted ? RewardClaimState.CLAIMED : RewardClaimState.AVAILABLE);
  }

  private static ServerPlayer resolvePlayer(PlayerState playerState, ActionContext actionContext) {
    if (actionContext != null && actionContext.player() != null) {
      return actionContext.player();
    }
    MinecraftServer server = ServerEvents.getServer();
    return server == null ? null : server.getPlayerList().getPlayer(playerState.playerUuid());
  }

  private static void activateDependents(
      PlayerState playerState,
      ResourceLocation questId,
      QuestProgress questProgress,
      Map<String, StepProgress> changedSteps) {
    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    if (definition.isEmpty()) {
      return;
    }

    List<String> activated = new ArrayList<>();
    for (RawQuestStep step : definition.get().logic().steps().values()) {
      StepProgress progress = questProgress.steps().get(step.id());
      if (progress == null
          || progress.state() != StepState.LOCKED
          || !allRequirementsComplete(step, questProgress)) {
        continue;
      }
      StepProgress active = progress.withState(StepState.ACTIVE);
      questProgress.putStep(step.id(), active);
      changedSteps.put(step.id(), active);
      activated.add(step.id());
    }

    if (!activated.isEmpty()) {
      playerState.markDirty();
      QuestStepEvents.handleStepsActivated(playerState, questId, activated);
    }
  }

  private static boolean allRequirementsComplete(RawQuestStep step, QuestProgress questProgress) {
    for (String required : step.requires()) {
      StepProgress dependency = questProgress.steps().get(required);
      if (dependency == null || dependency.state() != StepState.COMPLETED) {
        return false;
      }
    }
    return true;
  }

  private static void skipRemainingSteps(
      QuestProgress questProgress, Map<String, StepProgress> changedSteps) {
    for (String stepId : List.copyOf(questProgress.steps().keySet())) {
      StepProgress progress = questProgress.steps().get(stepId);
      if (progress.state() == StepState.ACTIVE || progress.state() == StepState.LOCKED) {
        StepProgress skipped = progress.skipped();
        questProgress.putStep(stepId, skipped);
        changedSteps.put(stepId, skipped);
      }
    }
  }

  private static boolean applyCompletionActionsOnce(
      PlayerState playerState,
      ActionContext actionContext,
      Optional<QuestDefinition> definition,
      QuestProgress questProgress) {
    if (actionContext == null || questProgress.lastRewardedRevision() == questProgress.revision()) {
      return false;
    }

    definition.ifPresent(quest -> quest.onComplete().execute(actionContext));
    questProgress.setLastRewardedRevision(questProgress.revision());
    playerState.markDirty();
    return true;
  }

  private static int requiredAmountFor(RawQuestStep step) {
    JsonObject jsonObject = step.typeSpecificJson();
    for (String field : new String[] {"required", "amount", "count"}) {
      if (jsonObject.has(field) && jsonObject.get(field).isJsonPrimitive()) {
        try {
          return Math.max(1, jsonObject.get(field).getAsInt());
        } catch (NumberFormatException ignored) {
          return 1;
        }
      }
    }
    return 1;
  }
}
