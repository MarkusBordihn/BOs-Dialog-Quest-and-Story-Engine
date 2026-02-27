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
import de.markusbordihn.dialogqueststoryengine.network.message.SyncInteractionDataMessage;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NetworkHandlerManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static NetworkHandlerInterface networkHandler;
  private static NetworkHandlerManagerType networkHandlerManagerType =
      NetworkHandlerManagerType.BOTH;

  private NetworkHandlerManager() {}

  public static void registerHandler(NetworkHandlerInterface handler) {
    networkHandler = handler;
    log.info("{} Network Handler ...", Constants.LOG_REGISTER_PREFIX);
  }

  public static void registerNetworkMessages(NetworkHandlerManagerType type) {
    log.info("Registering network messages for {} side ...", type);
    networkHandlerManagerType = type;
    registerClientMessages();
  }

  public static boolean isClientNetworkHandler() {
    return networkHandlerManagerType == NetworkHandlerManagerType.CLIENT
        || networkHandlerManagerType == NetworkHandlerManagerType.BOTH;
  }

  private static void registerClientMessages() {
    if (networkHandler == null) {
      log.error("Cannot register client messages: no network handler registered.");
      return;
    }
    if (!isClientNetworkHandler()) {
      return;
    }
    networkHandler.registerClientNetworkMessageHandler(
        SyncInteractionDataMessage.MESSAGE_ID,
        SyncInteractionDataMessage.class,
        SyncInteractionDataMessage::create);
  }

  public static void sendToPlayer(ServerPlayer player, NetworkMessageRecord message) {
    if (networkHandler != null) {
      networkHandler.sendToPlayer(player, message);
    }
  }
}
