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

package de.markusbordihn.dialogqueststoryengine.api.quest;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.quest.reward.RewardClaimService;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestChangeResult;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestTrackingService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class QuestManager {

  private QuestManager() {}

  public static Optional<QuestChangeResult> start(ServerPlayer player, ResourceLocation questId) {
    return QuestService.startQuest(player, questId);
  }

  public static Optional<QuestChangeResult> complete(
      ServerPlayer player, ResourceLocation questId) {
    return QuestService.completeQuest(player, questId);
  }

  public static Optional<QuestChangeResult> fail(ServerPlayer player, ResourceLocation questId) {
    return QuestService.failQuest(player.getUUID(), questId);
  }

  public static Optional<QuestChangeResult> progressStep(
      ServerPlayer player, ResourceLocation questId, String stepId, int amount) {
    return QuestService.progressStep(player.getUUID(), questId, stepId, amount);
  }

  public static Optional<QuestChangeResult> changeStatus(
      ServerPlayer player, ResourceLocation questId, QuestState targetState) {
    return switch (targetState) {
      case ACTIVE -> start(player, questId);
      case COMPLETED -> complete(player, questId);
      case FAILED -> fail(player, questId);
      case NOT_STARTED -> Optional.empty();
    };
  }

  public static void track(ServerPlayer player, ResourceLocation questId) {
    QuestTrackingService.handleTrackRequest(player, Optional.of(questId));
  }

  public static void untrack(ServerPlayer player) {
    QuestTrackingService.handleTrackRequest(player, Optional.empty());
  }

  public static void claimRewards(ServerPlayer player, ResourceLocation questId) {
    QuestProgress progress =
        PlayerStateService.get(player.getUUID()).map(state -> state.getQuest(questId)).orElse(null);
    if (progress != null) {
      RewardClaimService.claim(player, questId, progress.revision());
    }
  }
}
