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

package de.markusbordihn.dialogqueststoryengine.quest.step;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.content.quest.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.registry.QuestStepHandler;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import de.markusbordihn.dialogqueststoryengine.state.StepState;
import java.util.Map;
import java.util.function.BiConsumer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

public final class QuestStepEvents {

  private QuestStepEvents() {}

  public static void handleEntityInteract(Player player, Entity target) {
    dispatch(player, (handler, context) -> handler.onEntityInteract(context, target));
  }

  public static void handleItemPickup(Player player) {
    dispatch(player, QuestStepHandler::onItemPickup);
  }

  public static void handleEntityKilled(LivingEntity killed, DamageSource source) {
    if (killed.level().isClientSide() || !(source.getEntity() instanceof Player player)) {
      return;
    }
    dispatch(player, (handler, context) -> handler.onEntityKilled(context, killed));
  }

  public static void handleQuestStarted(PlayerState playerState, ResourceLocation questId) {
    handleQuestStarted(playerState, resolvePlayer(playerState), questId);
  }

  public static void handleQuestStarted(
      PlayerState playerState, Player player, ResourceLocation questId) {
    dispatchLifecycle(playerState, player, questId, QuestStepHandler::onQuestStarted);
  }

  public static void handleQuestCompleted(PlayerState playerState, ResourceLocation questId) {
    handleQuestCompleted(playerState, resolvePlayer(playerState), questId);
  }

  public static void handleQuestCompleted(
      PlayerState playerState, Player player, ResourceLocation questId) {
    dispatchLifecycle(playerState, player, questId, QuestStepHandler::onQuestCompleted);
  }

  private static void dispatch(Player player, BiConsumer<QuestStepHandler, QuestStepContext> hook) {
    if (player == null || player.level().isClientSide()) {
      return;
    }

    PlayerStateService.get(player.getUUID())
        .ifPresent(
            playerState -> {
              for (Map.Entry<ResourceLocation, QuestProgress> questEntry :
                  playerState.allQuests().entrySet()) {
                QuestProgress questProgress = questEntry.getValue();
                if (questProgress.state() != QuestState.ACTIVE) {
                  continue;
                }
                QuestContentRegistry.get(questEntry.getKey())
                    .ifPresent(
                        definition ->
                            dispatchSteps(
                                player, playerState, definition, questProgress, hook, true));
              }
            });
  }

  private static void dispatchLifecycle(
      PlayerState playerState,
      Player player,
      ResourceLocation questId,
      BiConsumer<QuestStepHandler, QuestStepContext> hook) {
    QuestProgress questProgress = playerState.getQuest(questId);
    if (questProgress == null) {
      return;
    }

    QuestContentRegistry.get(questId)
        .ifPresent(
            definition ->
                dispatchSteps(player, playerState, definition, questProgress, hook, false));
  }

  private static void dispatchSteps(
      Player player,
      PlayerState playerState,
      QuestDefinition definition,
      QuestProgress questProgress,
      BiConsumer<QuestStepHandler, QuestStepContext> hook,
      boolean activeStepsOnly) {
    for (RawQuestStep step : definition.logic().steps().values()) {
      StepProgress stepProgress = questProgress.steps().get(step.id());
      if (stepProgress == null || (activeStepsOnly && stepProgress.state() != StepState.ACTIVE)) {
        continue;
      }

      Registries.QUEST_STEPS
          .get(step.type())
          .filter(QuestStepHandler::supported)
          .ifPresent(
              handler ->
                  hook.accept(
                      handler,
                      new QuestStepContext(
                          player, playerState, definition.id(), step, stepProgress)));
    }
  }

  private static Player resolvePlayer(PlayerState playerState) {
    MinecraftServer server = ServerEvents.getServer();
    return server != null ? server.getPlayerList().getPlayer(playerState.playerUuid()) : null;
  }
}
