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

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestDefinition;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PlayerStateService {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final ConcurrentHashMap<UUID, PlayerState> cache = new ConcurrentHashMap<>();

  private PlayerStateService() {}

  public static void onPlayerDataLoaded(UUID playerUuid, CompoundTag nbt) {
    PlayerState playerState = PlayerStateCodec.fromNbt(nbt, playerUuid);
    cache.put(playerUuid, playerState);
    PlayerStateEvents.firePlayerStateLoaded(playerUuid, playerState);
    log.debug("{} Loaded player state for {}.", Constants.LOG_PREFIX, playerUuid);
  }

  public static CompoundTag getPlayerDataForSave(UUID playerUuid) {
    PlayerState playerState = cache.get(playerUuid);
    if (playerState == null) {
      return null;
    }
    if (!playerState.isDirty()) {
      return null;
    }
    CompoundTag nbt = PlayerStateCodec.toNbt(playerState);
    playerState.clearDirty();
    return nbt;
  }

  public static void onPlayerLoggedOut(UUID playerUuid) {
    cache.remove(playerUuid);
    log.debug("{} Evicted player state cache for {}.", Constants.LOG_PREFIX, playerUuid);
  }

  public static void onServerStopping(MinecraftServer server) {
    log.info(
        "{} Server stopping — clearing player state cache ({} entries).",
        Constants.LOG_PREFIX,
        cache.size());
    cache.clear();
  }

  public static Optional<PlayerState> get(UUID playerUuid) {
    return Optional.ofNullable(cache.get(playerUuid));
  }

  public static boolean isLoaded(UUID playerUuid) {
    return cache.containsKey(playerUuid);
  }

  public static Optional<QuestProgress> startQuest(UUID playerUuid, ResourceLocation questId) {
    PlayerState playerState = cache.get(playerUuid);
    if (playerState == null) {
      log.warn(
          "{} Cannot start quest {} — player {} not loaded.",
          Constants.LOG_PREFIX,
          questId,
          playerUuid);
      return Optional.empty();
    }
    if (playerState.hasQuest(questId)
        && playerState.getQuest(questId).state() == QuestState.ACTIVE) {
      return Optional.of(playerState.getQuest(questId));
    }

    QuestProgress questProgress = new QuestProgress(QuestState.ACTIVE);
    Optional<QuestDefinition> definition = QuestContentRegistry.get(questId);
    if (definition.isPresent()) {
      for (String stepId : definition.get().logic().steps().keySet()) {
        questProgress.putStep(stepId, StepProgress.locked());
      }
    } else {
      log.warn(
          "{} Quest {} not found in registry — starting with empty steps.",
          Constants.LOG_PREFIX,
          questId);
    }

    playerState.putQuestDirect(questId, questProgress);
    PlayerStateEvents.fireQuestStarted(playerUuid, questId, questProgress);
    return Optional.of(questProgress);
  }

  public static Optional<QuestProgress> completeQuest(UUID playerUuid, ResourceLocation questId) {
    PlayerState playerState = cache.get(playerUuid);
    if (playerState == null || !playerState.hasQuest(questId)) {
      return Optional.empty();
    }
    QuestProgress questProgress = playerState.getQuest(questId);
    questProgress.setState(QuestState.COMPLETED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestCompleted(playerUuid, questId, questProgress);
    return Optional.of(questProgress);
  }

  public static Optional<StepProgress> progressStep(
      UUID playerUuid, ResourceLocation questId, String stepId, int delta) {
    PlayerState playerState = cache.get(playerUuid);
    if (playerState == null || !playerState.hasQuest(questId)) {
      return Optional.empty();
    }
    StepProgress stepProgress = playerState.getQuest(questId).incrementStep(stepId, delta);
    playerState.markDirty();
    PlayerStateEvents.fireStepProgressed(playerUuid, questId, stepId, stepProgress);
    return Optional.of(stepProgress);
  }

  public static void setFact(UUID playerUuid, FactScope scope, String key, FactValue value) {
    PlayerState playerState = cache.get(playerUuid);
    if (playerState == null) {
      return;
    }
    playerState.setFact(scope, key, value);
    PlayerStateEvents.fireFactChanged(playerUuid, scope, key, value);
  }

  public static Optional<FactValue> getFact(UUID playerUuid, FactScope scope, String key) {
    PlayerState playerState = cache.get(playerUuid);
    if (playerState == null) {
      return Optional.empty();
    }
    return Optional.ofNullable(playerState.getFact(scope, key));
  }
}
