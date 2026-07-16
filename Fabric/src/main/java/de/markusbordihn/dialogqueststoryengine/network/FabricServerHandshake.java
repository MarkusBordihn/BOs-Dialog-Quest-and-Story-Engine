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

import de.markusbordihn.dialogqueststoryengine.Constants;
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs;
import net.fabricmc.fabric.api.networking.v1.ServerLoginConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerLoginNetworking;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FabricServerHandshake {

  static final ResourceLocation CHANNEL =
      new ResourceLocation(Constants.MOD_ID, "protocol_handshake");
  private static final Component MISMATCH =
      Component.translatable("message." + Constants.MOD_ID + ".protocol.mismatch");

  private FabricServerHandshake() {}

  public static void register() {
    ServerLoginConnectionEvents.QUERY_START.register(
        (handler, server, sender, synchronizer) -> {
          FriendlyByteBuf buffer = PacketByteBufs.create();
          buffer.writeVarInt(ProtocolVersion.CURRENT);
          sender.sendPacket(CHANNEL, buffer);
        });

    ServerLoginNetworking.registerGlobalReceiver(
        CHANNEL,
        (server, handler, understood, buffer, synchronizer, responseSender) -> {
          if (!understood) {
            handler.disconnect(MISMATCH);
            return;
          }

          if (!ProtocolVersion.isCompatible(buffer.readVarInt())) {
            handler.disconnect(MISMATCH);
          }
        });
  }
}
