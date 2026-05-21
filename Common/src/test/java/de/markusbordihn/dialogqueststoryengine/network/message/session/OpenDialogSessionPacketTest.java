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
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class OpenDialogSessionPacketTest {

  private static final UUID SESSION_ID = UUID.fromString("aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa");
  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "dialog");

  @Test
  void roundTrip() {
    OpenDialogSessionPacket original =
        new OpenDialogSessionPacket(
            SESSION_ID,
            DIALOG_ID,
            "start",
            List.of("choice_yes", "choice_no"),
            Map.of("key1", "value1"),
            3);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    OpenDialogSessionPacket decoded = OpenDialogSessionPacket.create(buffer);

    assertEquals(original.sessionId(), decoded.sessionId());
    assertEquals(original.dialogId(), decoded.dialogId());
    assertEquals(original.nodeId(), decoded.nodeId());
    assertEquals(original.allowedChoiceIds(), decoded.allowedChoiceIds());
    assertEquals(original.contextArgs(), decoded.contextArgs());
    assertEquals(original.revision(), decoded.revision());
  }

  @Test
  void roundTripEmptyChoices() {
    OpenDialogSessionPacket original =
        new OpenDialogSessionPacket(SESSION_ID, DIALOG_ID, "end", List.of(), Map.of(), 0);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    OpenDialogSessionPacket decoded = OpenDialogSessionPacket.create(buffer);

    assertEquals(0, decoded.allowedChoiceIds().size());
    assertEquals(0, decoded.contextArgs().size());
    assertEquals(0, decoded.revision());
  }
}
