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
import de.markusbordihn.dialogqueststoryengine.network.message.session.StoryDeltaPacket;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class StoryProgressService {

  private StoryProgressService() {}

  public static boolean unlock(
      ServerPlayer player, PlayerState playerState, ResourceLocation storyId) {
    boolean newlyUnlocked = playerState.stories().unlock(storyId);
    if (!newlyUnlocked) {
      return false;
    }
    playerState.markDirty();
    int revision = playerState.stories().bumpRevision();
    PlayerStateEvents.fireStoryUnlocked(playerState.playerUuid(), storyId);
    sendDelta(player, List.of(storyId), List.of(), revision);
    return true;
  }

  public static boolean markRead(
      ServerPlayer player, PlayerState playerState, ResourceLocation storyId) {
    boolean newlyUnlocked = !playerState.stories().isUnlocked(storyId);
    boolean newlyRead = playerState.stories().markRead(storyId);
    if (!newlyUnlocked && !newlyRead) {
      return false;
    }
    playerState.markDirty();
    int revision = playerState.stories().bumpRevision();
    if (newlyUnlocked) {
      PlayerStateEvents.fireStoryUnlocked(playerState.playerUuid(), storyId);
    }
    if (newlyRead) {
      PlayerStateEvents.fireStoryRead(playerState.playerUuid(), storyId);
    }
    sendDelta(
        player,
        newlyUnlocked ? List.of(storyId) : List.of(),
        newlyRead ? List.of(storyId) : List.of(),
        revision);
    return true;
  }

  private static void sendDelta(
      ServerPlayer player,
      List<ResourceLocation> unlockedIds,
      List<ResourceLocation> readIds,
      int revision) {
    if (player != null) {
      NetworkHandlerManager.sendToPlayer(
          player, new StoryDeltaPacket(unlockedIds, readIds, revision));
    }
  }
}
