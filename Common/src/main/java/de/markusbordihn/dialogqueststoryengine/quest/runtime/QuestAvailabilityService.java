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

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestAvailability;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public final class QuestAvailabilityService {

  private QuestAvailabilityService() {}

  public static QuestAvailability availabilityFor(
      PlayerState playerState, ResourceLocation questId) {
    QuestAvailability started = startedAvailability(playerState.getQuest(questId));
    if (started != null) {
      return started;
    }

    return QuestContentRegistry.get(questId)
        .map(definition -> prerequisiteAvailability(playerState, definition))
        .orElse(QuestAvailability.LOCKED);
  }

  public static QuestAvailability availabilityFor(
      PlayerState playerState, QuestDefinition definition) {
    QuestAvailability started = startedAvailability(playerState.getQuest(definition.id()));
    return started != null ? started : prerequisiteAvailability(playerState, definition);
  }

  private static QuestAvailability startedAvailability(QuestProgress progress) {
    if (progress == null) {
      return null;
    }

    return switch (progress.state()) {
      case ACTIVE -> QuestAvailability.ACTIVE;
      case COMPLETED -> QuestAvailability.COMPLETED;
      case FAILED -> QuestAvailability.FAILED;
      case NOT_STARTED -> null;
    };
  }

  private static QuestAvailability prerequisiteAvailability(
      PlayerState playerState, QuestDefinition definition) {
    return prerequisitesMet(playerState, definition.logic().prerequisites())
        ? QuestAvailability.AVAILABLE
        : QuestAvailability.LOCKED;
  }

  public static boolean prerequisitesMet(
      PlayerState playerState, QuestPrerequisites prerequisites) {
    List<ResourceLocation> quests = prerequisites.quests();
    if (quests.isEmpty()) {
      return true;
    }

    return switch (prerequisites.mode()) {
      case ALL -> quests.stream().allMatch(id -> isCompleted(playerState, id));
      case ANY -> quests.stream().anyMatch(id -> isCompleted(playerState, id));
    };
  }

  private static boolean isCompleted(PlayerState playerState, ResourceLocation questId) {
    QuestProgress progress = playerState.getQuest(questId);
    return progress != null && progress.state() == QuestState.COMPLETED;
  }
}
