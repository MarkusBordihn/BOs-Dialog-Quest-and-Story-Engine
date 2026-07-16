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

package de.markusbordihn.dialogqueststoryengine.network.message.session;

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.dialogqueststoryengine.data.session.SessionCloseReason;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class CloseSessionPacketTest {

  private static final UUID SESSION_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");

  @Test
  void roundTripPlayerClosed() {
    CloseSessionPacket original =
        new CloseSessionPacket(SESSION_ID, SessionCloseReason.PLAYER_CLOSED);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    CloseSessionPacket decoded = CloseSessionPacket.create(buffer);

    assertEquals(original.sessionId(), decoded.sessionId());
    assertEquals(SessionCloseReason.PLAYER_CLOSED, decoded.reason());
  }

  @Test
  void roundTripReload() {
    CloseSessionPacket original = new CloseSessionPacket(SESSION_ID, SessionCloseReason.RELOAD);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    CloseSessionPacket decoded = CloseSessionPacket.create(buffer);

    assertEquals(SessionCloseReason.RELOAD, decoded.reason());
  }

  @Test
  void roundTripAllReasons() {
    for (SessionCloseReason reason : SessionCloseReason.values()) {
      CloseSessionPacket original = new CloseSessionPacket(SESSION_ID, reason);

      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
      original.write(buffer);
      CloseSessionPacket decoded = CloseSessionPacket.create(buffer);

      assertEquals(reason, decoded.reason(), "Failed for reason: " + reason);
    }
  }
}
