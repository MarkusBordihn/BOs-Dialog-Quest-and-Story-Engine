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

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.TrackedQuestPacket;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class QuestTrackingService {

  private QuestTrackingService() {}

  public static void handleTrackRequest(ServerPlayer player, Optional<ResourceLocation> questId) {
    PlayerState playerState = PlayerStateService.get(player.getUUID()).orElse(null);
    if (playerState == null) {
      return;
    }

    if (questId.isEmpty()) {
      setTracked(player, playerState, null);
      return;
    }

    ResourceLocation id = questId.get();
    QuestProgress progress = playerState.getQuest(id);
    if (progress == null || progress.state() != QuestState.ACTIVE) {
      sendTracked(player, playerState);
      return;
    }

    setTracked(player, playerState, id);
  }

  public static void autoTrackIfNone(
      ServerPlayer player, PlayerState playerState, ResourceLocation questId) {
    if (playerState.trackedQuestId() == null) {
      setTracked(player, playerState, questId);
    }
  }

  public static void clearIfTracked(
      ServerPlayer player, PlayerState playerState, ResourceLocation questId) {
    if (questId.equals(playerState.trackedQuestId())) {
      setTracked(player, playerState, null);
    }
  }

  private static void setTracked(
      ServerPlayer player, PlayerState playerState, ResourceLocation questId) {
    playerState.setTrackedQuestId(questId);
    sendTracked(player, playerState);
  }

  private static void sendTracked(ServerPlayer player, PlayerState playerState) {
    if (player != null) {
      NetworkHandlerManager.sendToPlayer(
          player, new TrackedQuestPacket(Optional.ofNullable(playerState.trackedQuestId())));
    }
  }
}
