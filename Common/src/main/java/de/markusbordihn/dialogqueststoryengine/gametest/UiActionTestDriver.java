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

import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.network.message.session.ClientCloseSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.SubmitChoicePacket;
import io.netty.buffer.Unpooled;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;

public final class UiActionTestDriver {

  private UiActionTestDriver() {}

  public static <T extends NetworkMessageRecord> void receiveOnServer(
      ServerPlayer player, T packet, Function<FriendlyByteBuf, T> decoder) {
    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    packet.write(buffer);
    decoder.apply(buffer).handleServer(player);
  }

  public static void pressChoice(
      ServerPlayer player, UUID sessionId, String choiceId, int revision) {
    receiveOnServer(
        player, new SubmitChoicePacket(sessionId, choiceId, revision), SubmitChoicePacket::create);
  }

  public static void closeSession(ServerPlayer player, UUID sessionId) {
    receiveOnServer(
        player, new ClientCloseSessionPacket(sessionId), ClientCloseSessionPacket::create);
  }
}
