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
import de.markusbordihn.dialogqueststoryengine.content.quest.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.content.quest.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import de.markusbordihn.dialogqueststoryengine.state.StepState;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
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
    QuestProgress existing = playerState.getQuest(questId);
    if (existing != null && existing.state() != QuestState.NOT_STARTED) {
      return Optional.of(new QuestChangeResult(questId, existing, Map.of(), false));
    }

    if (QuestContentRegistry.get(questId).isEmpty()) {
      log.warn("{} startQuest: quest '{}' is not loaded.", Constants.LOG_PREFIX, questId);
      return Optional.empty();
    }

    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    Map<String, StepProgress> changedSteps = initialiseSteps(questId, questProgress);

    playerState.putQuestDirect(questId, questProgress);
    PlayerStateEvents.fireQuestStarted(playerState.playerUuid(), questId, questProgress);
    QuestStepEvents.handleQuestStarted(playerState, questId);
    return Optional.of(new QuestChangeResult(questId, questProgress, changedSteps, true));
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
      return Optional.of(new QuestChangeResult(questId, questProgress, Map.of(), false));
    }

    questProgress.setState(QuestState.FAILED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestFailed(playerState.playerUuid(), questId, questProgress);
    return Optional.of(new QuestChangeResult(questId, questProgress, Map.of(), true));
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
      return Optional.of(new QuestChangeResult(questId, questProgress, Map.of(), applied));
    }

    questProgress.setState(QuestState.COMPLETED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestCompleted(playerState.playerUuid(), questId, questProgress);
    QuestStepEvents.handleQuestCompleted(playerState, questId);
    applyCompletionActionsOnce(playerState, actionContext, definition, questProgress);
    return Optional.of(new QuestChangeResult(questId, questProgress, Map.of(), true));
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
      return Optional.of(new QuestChangeResult(questId, questProgress, Map.of(), false));
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

    StepProgress updated = updateStepProgress(current, value, overwrite);
    if (updated.equals(current)) {
      return Optional.of(new QuestChangeResult(questId, questProgress, Map.of(), false));
    }

    questProgress.putStep(stepId, updated);
    playerState.markDirty();
    PlayerStateEvents.fireStepProgressed(playerState.playerUuid(), questId, stepId, updated);

    Map<String, StepProgress> changedSteps = new LinkedHashMap<>();
    changedSteps.put(stepId, updated);
    boolean completed = evaluateCompletion(playerState, actionContext, questId, questProgress);
    return Optional.of(new QuestChangeResult(questId, questProgress, changedSteps, completed));
  }

  private static Map<String, StepProgress> initialiseSteps(
      ResourceLocation questId, QuestProgress questProgress) {
    Map<String, StepProgress> changedSteps = new LinkedHashMap<>();
    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    if (definition.isEmpty()) {
      log.warn(
          "{} Quest {} not found in registry — starting with empty steps.",
          Constants.LOG_PREFIX,
          questId);
      return changedSteps;
    }

    for (RawQuestStep step : definition.get().logic().steps().values()) {
      StepProgress stepProgress = StepProgress.active(requiredFor(step));
      questProgress.putStep(step.id(), stepProgress);
      changedSteps.put(step.id(), stepProgress);
    }
    return changedSteps;
  }

  private static StepProgress updateStepProgress(
      StepProgress current, int value, boolean overwrite) {
    StepProgress active =
        current.state() == StepState.LOCKED ? current.withState(StepState.ACTIVE) : current;
    if (active.required() <= 0) {
      return StepProgress.completed(0);
    }
    int newProgress = overwrite ? Math.max(0, value) : active.progress() + Math.max(0, value);
    return active.withProgress(newProgress);
  }

  private static boolean evaluateCompletion(
      PlayerState playerState,
      ActionContext actionContext,
      ResourceLocation questId,
      QuestProgress questProgress) {
    if (questProgress.state() != QuestState.ACTIVE) {
      return false;
    }

    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    CompletionPolicy policy =
        definition
            .map(questDefinition -> questDefinition.logic().completionPolicy())
            .orElse(CompletionPolicy.ALL_STEPS);
    boolean complete =
        switch (policy) {
          case ANY_STEP -> questProgress.steps().values().stream().anyMatch(StepProgress::complete);
          case ALL_STEPS ->
              !questProgress.steps().isEmpty()
                  && questProgress.steps().values().stream().allMatch(StepProgress::complete);
        };

    if (!complete) {
      return false;
    }

    questProgress.setState(QuestState.COMPLETED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestCompleted(playerState.playerUuid(), questId, questProgress);
    QuestStepEvents.handleQuestCompleted(playerState, questId);
    applyCompletionActionsOnce(playerState, actionContext, definition, questProgress);
    return true;
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

  private static int requiredFor(RawQuestStep step) {
    JsonObject jsonObject = step.jsonObject();
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
