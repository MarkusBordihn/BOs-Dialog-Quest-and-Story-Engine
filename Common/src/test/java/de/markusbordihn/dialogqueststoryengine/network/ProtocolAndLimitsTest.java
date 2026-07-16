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

package de.markusbordihn.dialogqueststoryengine.network;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.network.message.session.SubmitChoicePacket;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import org.junit.jupiter.api.Test;

class ProtocolAndLimitsTest {

  @Test
  void protocolVersionMatchesOnlyCurrent() {
    assertTrue(ProtocolVersion.isCompatible(ProtocolVersion.CURRENT));
    assertFalse(ProtocolVersion.isCompatible(ProtocolVersion.CURRENT - 1));
    assertFalse(ProtocolVersion.isCompatible(ProtocolVersion.CURRENT + 1));
  }

  @Test
  void rateLimiterAllowsBurstThenBlocks() {
    UUID player = UUID.randomUUID();
    try {
      int allowed = 0;
      while (C2SRateLimiter.allow(player)) {
        allowed++;
        if (allowed > 1000) {
          break;
        }
      }
      assertTrue(allowed > 0 && allowed <= 1000, "Limiter should allow a bounded burst");
      assertFalse(C2SRateLimiter.allow(player), "Limiter should block once the window is full");
    } finally {
      C2SRateLimiter.clear(player);
    }
  }

  @Test
  void submitChoiceDecoderRejectsOversizedChoiceId() {
    SubmitChoicePacket oversized = new SubmitChoicePacket(UUID.randomUUID(), "x".repeat(2048), 0);
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    oversized.write(buffer);

    assertThrows(RuntimeException.class, () -> SubmitChoicePacket.create(buffer));
  }
}
