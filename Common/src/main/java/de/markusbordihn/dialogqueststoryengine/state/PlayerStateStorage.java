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

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.io.IOException;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtIo;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PlayerStateStorage {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private PlayerStateStorage() {}

  public static CompoundTag read(Path dataFile, UUID playerUuid) {
    if (!Files.isRegularFile(dataFile)) {
      return null;
    }
    try {
      return NbtIo.readCompressed(dataFile.toFile());
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

  public static boolean write(Path dataFile, CompoundTag nbt, UUID playerUuid) {
    Path temporaryFile = dataFile.resolveSibling(dataFile.getFileName() + ".tmp");
    try {
      Files.createDirectories(dataFile.getParent());
      NbtIo.writeCompressed(nbt, temporaryFile.toFile());
      moveIntoPlace(temporaryFile, dataFile);
      return true;
    } catch (IOException ioException) {
      log.error(
          "{} Failed to write player state file {} for {}: {}",
          Constants.LOG_PREFIX,
          dataFile,
          playerUuid,
          ioException.getMessage());
      try {
        Files.deleteIfExists(temporaryFile);
      } catch (IOException cleanupException) {
        log.debug("{} Failed to remove temporary file {}.", Constants.LOG_PREFIX, temporaryFile);
      }
      return false;
    }
  }

  private static void moveIntoPlace(Path temporaryFile, Path dataFile) throws IOException {
    try {
      Files.move(
          temporaryFile,
          dataFile,
          StandardCopyOption.ATOMIC_MOVE,
          StandardCopyOption.REPLACE_EXISTING);
    } catch (AtomicMoveNotSupportedException exception) {
      Files.move(temporaryFile, dataFile, StandardCopyOption.REPLACE_EXISTING);
    }
  }
}
