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

package de.markusbordihn.dialogqueststoryengine.gametest;

import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;

public class GameTestHelpers {

  private GameTestHelpers() {}

  // Mirrors GameTestHelper#makeMockServerPlayerInLevel() but backs the connection with a live
  // EmbeddedChannel. The vanilla helper leaves the connection channel-less, which makes the Forge
  // post-login network sync fail with a Connection.channel() NPE.
  public static ServerPlayer mockConnectedServerPlayer(GameTestHelper helper) {
    ServerLevel level = helper.getLevel();
    ServerPlayer serverPlayer =
        new ServerPlayer(
            level.getServer(), level, new GameProfile(UUID.randomUUID(), "test-mock-player")) {
          @Override
          public boolean isSpectator() {
            return false;
          }

          @Override
          public boolean isCreative() {
            return true;
          }
        };
    Connection connection = new Connection(PacketFlow.SERVERBOUND);
    new EmbeddedChannel(connection);
    level.getServer().getPlayerList().placeNewPlayer(connection, serverPlayer);
    return serverPlayer;
  }

  public static void assertTrue(GameTestHelper helper, String message, boolean condition) {
    if (condition) {
      helper.succeed();
    } else {
      helper.fail(message);
    }
  }

  public static void assertEquals(
      GameTestHelper helper, String message, Object expected, Object actual) {
    if (expected == null && actual == null) {
      helper.succeed();
    } else if (expected != null && expected.equals(actual)) {
      helper.succeed();
    } else {
      helper.fail(message + " (expected: " + expected + ", actual: " + actual + ")");
    }
  }

  public static void assertNotNull(GameTestHelper helper, String message, Object object) {
    assertTrue(helper, message, object != null);
  }
}
