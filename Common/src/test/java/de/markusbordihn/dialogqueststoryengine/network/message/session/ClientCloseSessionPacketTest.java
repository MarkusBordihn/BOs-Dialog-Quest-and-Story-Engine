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

import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class ClientCloseSessionPacketTest {

  @Test
  void roundTripPreservesSessionId() {
    UUID sessionId = UUID.fromString("11111111-2222-3333-4444-555555555555");
    ClientCloseSessionPacket original = new ClientCloseSessionPacket(sessionId);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    ClientCloseSessionPacket decoded = ClientCloseSessionPacket.create(buffer);

    assertEquals(sessionId, decoded.sessionId());
  }

  @Test
  void roundTripWithRandomSessionIds() {
    for (int i = 0; i < 10; i++) {
      UUID sessionId = UUID.randomUUID();
      ClientCloseSessionPacket original = new ClientCloseSessionPacket(sessionId);

      FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
      original.write(buffer);

      assertEquals(sessionId, ClientCloseSessionPacket.create(buffer).sessionId());
    }
  }
}
