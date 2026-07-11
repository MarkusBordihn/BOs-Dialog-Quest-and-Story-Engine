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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class PlayerStateStorageTest {

  @TempDir Path temporaryDirectory;

  @Test
  void writeAtomicallyReplacesPlayerData() {
    UUID playerUuid = UUID.randomUUID();
    Path dataFile = this.temporaryDirectory.resolve("player.dat");
    CompoundTag first = new CompoundTag();
    first.putInt("revision", 1);
    CompoundTag second = new CompoundTag();
    second.putInt("revision", 2);

    assertTrue(PlayerStateStorage.write(dataFile, first, playerUuid));
    assertTrue(PlayerStateStorage.write(dataFile, second, playerUuid));

    assertEquals(2, PlayerStateStorage.read(dataFile, playerUuid).getInt("revision"));
    assertFalse(Files.exists(this.temporaryDirectory.resolve("player.dat.tmp")));
  }
}
