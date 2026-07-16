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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;
import net.minecraft.resources.ResourceLocation;

public final class PlayerStateEvents {

  private static final List<PlayerStateLoadedListener> playerStateLoadedListeners =
      new CopyOnWriteArrayList<>();
  private static final List<QuestStartedListener> questStartedListeners =
      new CopyOnWriteArrayList<>();
  private static final List<QuestCompletedListener> questCompletedListeners =
      new CopyOnWriteArrayList<>();
  private static final List<QuestFailedListener> questFailedListeners =
      new CopyOnWriteArrayList<>();
  private static final List<StepProgressedListener> stepProgressedListeners =
      new CopyOnWriteArrayList<>();
  private static final List<FactChangedListener> factChangedListeners =
      new CopyOnWriteArrayList<>();

  private PlayerStateEvents() {}

  public static void addPlayerStateLoadedListener(PlayerStateLoadedListener listener) {
    playerStateLoadedListeners.add(listener);
  }

  public static void addQuestStartedListener(QuestStartedListener listener) {
    questStartedListeners.add(listener);
  }

  public static void addQuestCompletedListener(QuestCompletedListener listener) {
    questCompletedListeners.add(listener);
  }

  public static void addQuestFailedListener(QuestFailedListener listener) {
    questFailedListeners.add(listener);
  }

  public static void addStepProgressedListener(StepProgressedListener listener) {
    stepProgressedListeners.add(listener);
  }

  public static void addFactChangedListener(FactChangedListener listener) {
    factChangedListeners.add(listener);
  }

  public static void firePlayerStateLoaded(UUID playerUuid, PlayerState playerState) {
    for (PlayerStateLoadedListener listener : playerStateLoadedListeners) {
      listener.onPlayerStateLoaded(playerUuid, playerState);
    }
  }

  public static void fireQuestStarted(
      UUID playerUuid, ResourceLocation questId, QuestProgress questProgress) {
    for (QuestStartedListener listener : questStartedListeners) {
      listener.onQuestStarted(playerUuid, questId, questProgress);
    }
  }

  public static void fireQuestCompleted(
      UUID playerUuid, ResourceLocation questId, QuestProgress questProgress) {
    for (QuestCompletedListener listener : questCompletedListeners) {
      listener.onQuestCompleted(playerUuid, questId, questProgress);
    }
  }

  public static void fireQuestFailed(
      UUID playerUuid, ResourceLocation questId, QuestProgress questProgress) {
    for (QuestFailedListener listener : questFailedListeners) {
      listener.onQuestFailed(playerUuid, questId, questProgress);
    }
  }

  public static void fireStepProgressed(
      UUID playerUuid, ResourceLocation questId, String stepId, StepProgress stepProgress) {
    for (StepProgressedListener listener : stepProgressedListeners) {
      listener.onStepProgressed(playerUuid, questId, stepId, stepProgress);
    }
  }

  public static void fireFactChanged(
      UUID playerUuid, FactScope scope, String key, FactValue value) {
    for (FactChangedListener listener : factChangedListeners) {
      listener.onFactChanged(playerUuid, scope, key, value);
    }
  }

  public static void clearAll() {
    playerStateLoadedListeners.clear();
    questStartedListeners.clear();
    questCompletedListeners.clear();
    questFailedListeners.clear();
    stepProgressedListeners.clear();
    factChangedListeners.clear();
  }

  @FunctionalInterface
  public interface PlayerStateLoadedListener {
    void onPlayerStateLoaded(UUID playerUuid, PlayerState playerState);
  }

  @FunctionalInterface
  public interface QuestStartedListener {
    void onQuestStarted(UUID playerUuid, ResourceLocation questId, QuestProgress questProgress);
  }

  @FunctionalInterface
  public interface QuestCompletedListener {
    void onQuestCompleted(UUID playerUuid, ResourceLocation questId, QuestProgress questProgress);
  }

  @FunctionalInterface
  public interface QuestFailedListener {
    void onQuestFailed(UUID playerUuid, ResourceLocation questId, QuestProgress questProgress);
  }

  @FunctionalInterface
  public interface StepProgressedListener {
    void onStepProgressed(
        UUID playerUuid, ResourceLocation questId, String stepId, StepProgress stepProgress);
  }

  @FunctionalInterface
  public interface FactChangedListener {
    void onFactChanged(UUID playerUuid, FactScope scope, String key, FactValue value);
  }
}
