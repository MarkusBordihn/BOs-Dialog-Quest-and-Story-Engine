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
import static org.junit.jupiter.api.Assertions.assertNull;

import io.netty.buffer.Unpooled;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class DialogSessionPacketTest {

  private static final UUID SESSION_ID = UUID.fromString("bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb");
  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "greet");

  @Test
  void roundTripOpenDialog() {
    DialogSessionPacket original =
        new DialogSessionPacket(
            SESSION_ID,
            DialogSessionPacketType.OPEN_DIALOG,
            DIALOG_ID,
            "start",
            "npc.guard",
            "dialog.guard.greet",
            List.of("choice_yes", "choice_no"),
            Map.of("choice_yes", "dialog.choice.yes", "choice_no", "dialog.choice.no"),
            Map.of("ctx_key", "ctx_val"),
            5);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    DialogSessionPacket decoded = DialogSessionPacket.create(buffer);

    assertEquals(SESSION_ID, decoded.sessionId());
    assertEquals(DialogSessionPacketType.OPEN_DIALOG, decoded.type());
    assertEquals(DIALOG_ID, decoded.dialogId());
    assertEquals("start", decoded.nodeId());
    assertEquals("npc.guard", decoded.speakerKey());
    assertEquals("dialog.guard.greet", decoded.textKey());
    assertEquals(List.of("choice_yes", "choice_no"), decoded.allowedChoiceIds());
    assertEquals("dialog.choice.yes", decoded.choiceLabels().get("choice_yes"));
    assertEquals("ctx_val", decoded.context().get("ctx_key"));
    assertEquals(5, decoded.revision());
  }

  @Test
  void roundTripNavigateNode() {
    DialogSessionPacket original =
        new DialogSessionPacket(
            SESSION_ID,
            DialogSessionPacketType.NAVIGATE_NODE,
            null,
            "node_2",
            "npc.guard",
            "dialog.guard.reply",
            List.of("choice_end"),
            Map.of("choice_end", "dialog.choice.end"),
            Map.of(),
            6);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    DialogSessionPacket decoded = DialogSessionPacket.create(buffer);

    assertEquals(DialogSessionPacketType.NAVIGATE_NODE, decoded.type());
    assertNull(decoded.dialogId());
    assertEquals("node_2", decoded.nodeId());
    assertEquals("npc.guard", decoded.speakerKey());
    assertEquals("dialog.guard.reply", decoded.textKey());
    assertEquals(1, decoded.allowedChoiceIds().size());
    assertEquals(0, decoded.context().size());
    assertEquals(6, decoded.revision());
  }

  @Test
  void roundTripEmptyChoices() {
    DialogSessionPacket original =
        new DialogSessionPacket(
            SESSION_ID,
            DialogSessionPacketType.NAVIGATE_NODE,
            null,
            "end",
            "",
            "dialog.end.text",
            List.of(),
            Map.of(),
            Map.of(),
            0);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    DialogSessionPacket decoded = DialogSessionPacket.create(buffer);

    assertEquals(0, decoded.allowedChoiceIds().size());
    assertEquals(0, decoded.revision());
    assertEquals("", decoded.speakerKey());
  }
}
