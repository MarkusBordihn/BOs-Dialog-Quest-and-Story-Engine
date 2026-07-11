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

import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.ResetProgressPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.StoryDeltaPacket;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.server.level.ServerPlayer;

public final class PlayerProgressSync {

  private static final UUID INITIAL_SYNC_SESSION_ID = new UUID(0L, 0L);

  private PlayerProgressSync() {}

  public static void send(ServerPlayer player) {
    PlayerStateService.get(player.getUUID()).ifPresent(playerState -> send(player, playerState));
  }

  private static void send(ServerPlayer player, PlayerState playerState) {
    NetworkHandlerManager.sendToPlayer(player, new ResetProgressPacket());
    playerState
        .allQuests()
        .forEach(
            (questId, progress) ->
                NetworkHandlerManager.sendToPlayer(
                    player,
                    new QuestDeltaPacket(
                        questId, progress.state(), progress.steps(), progress.revision())));
    NetworkHandlerManager.sendToPlayer(
        player,
        new StoryDeltaPacket(
            INITIAL_SYNC_SESSION_ID,
            new ArrayList<>(playerState.stories().unlockedIds()),
            new ArrayList<>(playerState.stories().readIds()),
            0));
  }
}
