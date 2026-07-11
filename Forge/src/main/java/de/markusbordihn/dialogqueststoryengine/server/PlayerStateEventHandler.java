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

package de.markusbordihn.dialogqueststoryengine.server;

import de.markusbordihn.dialogqueststoryengine.session.SessionCloseReason;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import de.markusbordihn.dialogqueststoryengine.state.PlayerProgressSync;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateStorage;
import java.io.File;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class PlayerStateEventHandler {

  private static final String DATA_FILE_SUFFIX = ".dqse.dat";

  @SubscribeEvent
  public static void onPlayerLoad(PlayerEvent.LoadFromFile event) {
    if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
      return;
    }
    UUID playerUuid = serverPlayer.getUUID();
    File dataFile = getDataFile(event.getPlayerDirectory(), playerUuid);
    CompoundTag nbt = readNbt(dataFile, playerUuid);
    PlayerStateService.onPlayerDataLoaded(playerUuid, nbt != null ? nbt : new CompoundTag());
  }

  @SubscribeEvent
  public static void onPlayerSave(PlayerEvent.SaveToFile event) {
    if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
      return;
    }
    UUID playerUuid = serverPlayer.getUUID();
    CompoundTag nbt = PlayerStateService.getPlayerDataForSave(playerUuid);
    if (nbt == null) {
      return;
    }
    File dataFile = getDataFile(event.getPlayerDirectory(), playerUuid);
    if (writeNbt(dataFile, nbt, playerUuid)) {
      PlayerStateService.markPlayerDataSaved(playerUuid);
    }
  }

  @SubscribeEvent
  public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
    if (!(event.getEntity() instanceof ServerPlayer serverPlayer)) {
      return;
    }
    UUID playerUuid = serverPlayer.getUUID();
    CompoundTag nbt = PlayerStateService.getPlayerDataForSave(playerUuid);
    if (nbt != null) {
      File dataFile = getDataFile(resolvePlayerDirectory(serverPlayer), playerUuid);
      if (writeNbt(dataFile, nbt, playerUuid)) {
        PlayerStateService.markPlayerDataSaved(playerUuid);
      }
    }
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    SessionManager.invalidatePlayerSessions(playerUuid);
  }

  @SubscribeEvent
  public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
      PlayerProgressSync.send(serverPlayer);
    }
  }

  @SubscribeEvent
  public static void onPlayerChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
      SessionManager.closePlayerSessions(serverPlayer, SessionCloseReason.CONTEXT_CHANGED);
    }
  }

  @SubscribeEvent
  public static void onPlayerRespawn(PlayerEvent.PlayerRespawnEvent event) {
    if (event.getEntity() instanceof ServerPlayer serverPlayer) {
      SessionManager.closePlayerSessions(serverPlayer, SessionCloseReason.CONTEXT_CHANGED);
    }
  }

  private static File getDataFile(File playerDirectory, UUID playerUuid) {
    return new File(playerDirectory, playerUuid + DATA_FILE_SUFFIX);
  }

  private static File resolvePlayerDirectory(ServerPlayer serverPlayer) {
    return serverPlayer.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();
  }

  private static CompoundTag readNbt(File dataFile, UUID playerUuid) {
    return PlayerStateStorage.read(dataFile.toPath(), playerUuid);
  }

  private static boolean writeNbt(File dataFile, CompoundTag nbt, UUID playerUuid) {
    return PlayerStateStorage.write(dataFile.toPath(), nbt, playerUuid);
  }
}
