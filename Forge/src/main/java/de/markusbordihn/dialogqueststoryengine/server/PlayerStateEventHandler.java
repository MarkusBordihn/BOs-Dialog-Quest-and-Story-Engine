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

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.io.File;
import java.io.IOException;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class PlayerStateEventHandler {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
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
    writeNbt(dataFile, nbt, playerUuid);
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
      writeNbt(dataFile, nbt, playerUuid);
    }
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    SessionManager.invalidatePlayerSessions(playerUuid);
  }

  private static File getDataFile(File playerDirectory, UUID playerUuid) {
    return new File(playerDirectory, playerUuid + DATA_FILE_SUFFIX);
  }

  private static File resolvePlayerDirectory(ServerPlayer serverPlayer) {
    return serverPlayer.getServer().getWorldPath(LevelResource.PLAYER_DATA_DIR).toFile();
  }

  private static CompoundTag readNbt(File dataFile, UUID playerUuid) {
    if (!dataFile.exists()) {
      return null;
    }
    try {
      return NbtIo.readCompressed(dataFile);
    } catch (IOException ioException) {
      log.warn(
          "{} Failed to read player state file {} for {}: {}",
          Constants.LOG_PREFIX,
          dataFile,
          playerUuid,
          ioException.getMessage());
      return null;
    }
  }

  private static void writeNbt(File dataFile, CompoundTag nbt, UUID playerUuid) {
    try {
      dataFile.getParentFile().mkdirs();
      NbtIo.writeCompressed(nbt, dataFile);
    } catch (IOException ioException) {
      log.error(
          "{} Failed to write player state file {} for {}: {}",
          Constants.LOG_PREFIX,
          dataFile,
          playerUuid,
          ioException.getMessage());
    }
  }
}
