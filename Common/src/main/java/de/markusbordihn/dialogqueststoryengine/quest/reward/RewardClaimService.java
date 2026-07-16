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

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestRewardClaimReason;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardClaimState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestRewardClaimResultPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestChangeResult;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class RewardClaimService {

  private RewardClaimService() {}

  public static Optional<QuestRewardClaimReason> claim(
      ServerPlayer player, ResourceLocation questId, int knownRevision) {
    PlayerState playerState = PlayerStateService.get(player.getUUID()).orElse(null);
    if (playerState == null) {
      return reject(player, questId, QuestRewardClaimReason.QUEST_NOT_COMPLETED);
    }

    QuestProgress progress = playerState.getQuest(questId);
    if (progress == null || progress.state() != QuestState.COMPLETED) {
      return reject(player, questId, QuestRewardClaimReason.QUEST_NOT_COMPLETED);
    }

    if (progress.rewardClaimState() != RewardClaimState.AVAILABLE) {
      return reject(player, questId, QuestRewardClaimReason.REWARDS_NOT_AVAILABLE);
    }

    if (progress.revision() != knownRevision) {
      sendCurrentDelta(player, questId, progress);
      return reject(player, questId, QuestRewardClaimReason.STALE_REVISION);
    }

    QuestDefinition definition = QuestContentRegistry.get(questId).orElse(null);
    if (definition == null) {
      return reject(player, questId, QuestRewardClaimReason.REWARDS_NOT_AVAILABLE);
    }

    Optional<QuestRewardClaimReason> failure =
        RewardGrantService.grantAll(player, definition.rewards().entries());
    if (failure.isPresent()) {
      return reject(player, questId, failure.get());
    }

    progress.setRewardClaimState(RewardClaimState.CLAIMED);
    playerState.markDirty();
    PlayerStateEvents.fireQuestChanged(
        player.getUUID(), new QuestChangeResult(questId, progress, Map.of(), true));
    NetworkHandlerManager.sendToPlayer(player, QuestRewardClaimResultPacket.success(questId));
    return Optional.empty();
  }

  private static Optional<QuestRewardClaimReason> reject(
      ServerPlayer player, ResourceLocation questId, QuestRewardClaimReason reason) {
    NetworkHandlerManager.sendToPlayer(
        player, QuestRewardClaimResultPacket.rejected(questId, reason));
    return Optional.of(reason);
  }

  private static void sendCurrentDelta(
      ServerPlayer player, ResourceLocation questId, QuestProgress progress) {
    NetworkHandlerManager.sendToPlayer(
        player,
        new QuestDeltaPacket(
            questId, progress.state(), Map.of(), progress.rewardClaimState(), progress.revision()));
  }
}
