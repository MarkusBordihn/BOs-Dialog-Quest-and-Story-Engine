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

import de.markusbordihn.dialogqueststoryengine.config.FeedbackConfig;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class QuestChatFeedback {

  private QuestChatFeedback() {}

  public static void register() {
    PlayerStateEvents.addQuestStartedListener(
        (playerUuid, questId, questProgress) ->
            message(playerUuid, ChatFormatting.AQUA, "▶ Quest started: " + questId));

    PlayerStateEvents.addStepProgressedListener(
        (playerUuid, questId, stepId, stepProgress) -> {
          if (stepProgress.complete()) {
            message(playerUuid, ChatFormatting.GREEN, "  ✔ " + stepId);
          } else if (stepProgress.required() > 1) {
            message(
                playerUuid,
                ChatFormatting.GRAY,
                "  • " + stepId + ": " + stepProgress.progress() + "/" + stepProgress.required());
          }
        });

    PlayerStateEvents.addQuestCompletedListener(
        (playerUuid, questId, questProgress) ->
            message(playerUuid, ChatFormatting.GREEN, "✔ Quest complete: " + questId));

    PlayerStateEvents.addQuestFailedListener(
        (playerUuid, questId, questProgress) ->
            message(playerUuid, ChatFormatting.RED, "✖ Quest failed: " + questId));
  }

  private static void message(UUID playerUuid, ChatFormatting color, String text) {
    if (!FeedbackConfig.isQuestChatFeedback()) {
      return;
    }

    MinecraftServer server = ServerEvents.getServer();
    if (server == null) {
      return;
    }

    ServerPlayer player = server.getPlayerList().getPlayer(playerUuid);
    if (player != null) {
      player.sendSystemMessage(Component.literal(text).withStyle(color));
    }
  }
}
