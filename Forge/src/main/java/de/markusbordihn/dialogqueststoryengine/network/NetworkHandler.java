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
import java.util.Optional;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public final class NetworkHandler implements NetworkHandlerInterface {

  private static final String PROTOCOL_VERSION = "1";
  private static final SimpleChannel CHANNEL =
      NetworkRegistry.newSimpleChannel(
          ResourceLocation.tryParse(Constants.MOD_ID + ":network"),
          () -> PROTOCOL_VERSION,
          s -> true,
          s -> true);

  private int registrationId;

  public static void register() {
    NetworkHandlerManager.registerHandler(new NetworkHandler());
    NetworkHandlerManager.registerNetworkMessages(NetworkHandlerManagerType.BOTH);
  }

  @Override
  public <M extends NetworkMessageRecord> void registerClientNetworkMessageHandler(
      ResourceLocation messageId, Class<M> networkMessage, Function<FriendlyByteBuf, M> creator) {
    CHANNEL.registerMessage(
        registrationId++,
        networkMessage,
        NetworkMessageRecord::write,
        creator::apply,
        (message, contextSupplier) -> {
          contextSupplier.get().enqueueWork(message::handleClient);
          contextSupplier.get().setPacketHandled(true);
        },
        Optional.of(NetworkDirection.PLAY_TO_CLIENT));
  }

  @Override
  public <M extends NetworkMessageRecord> void registerServerNetworkMessageHandler(
      ResourceLocation messageId, Class<M> networkMessage, Function<FriendlyByteBuf, M> creator) {
    CHANNEL.registerMessage(
        registrationId++,
        networkMessage,
        NetworkMessageRecord::write,
        creator::apply,
        (message, contextSupplier) -> {
          ServerPlayer sender = contextSupplier.get().getSender();
          if (sender != null) {
            contextSupplier.get().enqueueWork(() -> message.handleServer(sender));
          }
          contextSupplier.get().setPacketHandled(true);
        },
        Optional.of(NetworkDirection.PLAY_TO_SERVER));
  }

  @Override
  public void sendToPlayer(ServerPlayer player, NetworkMessageRecord message) {
    CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), message);
  }

  @Override
  public void sendToServer(NetworkMessageRecord message) {
    CHANNEL.sendToServer(message);
  }
}
