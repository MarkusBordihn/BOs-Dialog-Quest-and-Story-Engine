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

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestDisplayCatalogEntry;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDisplayCatalogResetPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.ResetProgressPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.StoryDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.TrackedQuestPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestDisplayCatalogService;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.server.level.ServerPlayer;

public final class LoginProgressSync {

  private LoginProgressSync() {}

  public static void send(ServerPlayer player) {
    PlayerStateService.get(player.getUUID()).ifPresent(playerState -> send(player, playerState));
  }

  private static void send(ServerPlayer player, PlayerState playerState) {
    NetworkHandlerManager.sendToPlayer(player, new ResetProgressPacket());
    List<QuestDisplayCatalogEntry> catalog =
        QuestDisplayCatalogService.visibleCatalog(player, playerState);
    NetworkHandlerManager.sendToPlayer(player, new QuestDisplayCatalogResetPacket(catalog));
    QuestProgressSync.seedCatalog(player.getUUID(), catalog);
    playerState
        .allQuests()
        .forEach(
            (questId, progress) ->
                NetworkHandlerManager.sendToPlayer(
                    player,
                    new QuestDeltaPacket(
                        questId,
                        progress.state(),
                        progress.steps(),
                        progress.rewardClaimState(),
                        progress.revision())));
    NetworkHandlerManager.sendToPlayer(
        player,
        new StoryDeltaPacket(
            new ArrayList<>(playerState.stories().unlockedIds()),
            new ArrayList<>(playerState.stories().readIds()),
            playerState.stories().revision()));
    NetworkHandlerManager.sendToPlayer(
        player, new TrackedQuestPacket(Optional.ofNullable(playerState.trackedQuestId())));
  }
}
