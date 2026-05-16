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

package de.markusbordihn.dialogqueststoryengine.server;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionData;
import de.markusbordihn.dialogqueststoryengine.entity.InteractionEvents;
import de.markusbordihn.dialogqueststoryengine.item.InteractionWandItem;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.SyncInteractionDataMessage;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ServerEvents {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final int SYNC_INTERVAL = 20;
  private static final Map<UUID, PlayerSyncState> syncStates = new HashMap<>();
  private static MinecraftServer currentServer;

  private ServerEvents() {}

  public static void handleServerStarting(MinecraftServer server) {
    log.info("{} server is starting ...", Constants.MOD_NAME);
    currentServer = server;
    Registries.freezeAll();
    syncStates.clear();
    InteractionData.init(server);
  }

  public static void handleServerStopping(MinecraftServer server) {
    log.info("{} server is stopping ...", Constants.MOD_NAME);
    currentServer = null;
    syncStates.clear();
    InteractionData.reset();
    InteractionEvents.clearTrackingData();
  }

  public static MinecraftServer getServer() {
    return currentServer;
  }

  public static void handleServerTick(MinecraftServer server) {
    if (server.getTickCount() % SYNC_INTERVAL != 0) {
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null) {
      return;
    }

    long currentVersion = data.getSyncVersion();
    SyncInteractionDataMessage syncMessage = null;

    List<ServerPlayer> players = server.getPlayerList().getPlayers();

    if (syncStates.size() > players.size()) {
      Set<UUID> onlineIds = new HashSet<>(players.size());
      for (ServerPlayer player : players) {
        onlineIds.add(player.getUUID());
      }
      syncStates.keySet().retainAll(onlineIds);
    }

    for (ServerPlayer player : players) {
      boolean holdsWand =
          player.getMainHandItem().getItem() instanceof InteractionWandItem
              || player.getOffhandItem().getItem() instanceof InteractionWandItem;

      PlayerSyncState state =
          syncStates.computeIfAbsent(player.getUUID(), k -> new PlayerSyncState());

      if (holdsWand) {
        if ((!state.wasHoldingWand || state.lastSyncedVersion != currentVersion)
            && data.size() > 0) {
          if (syncMessage == null) {
            syncMessage = new SyncInteractionDataMessage(data.getAllEntries());
          }
          NetworkHandlerManager.sendToPlayer(player, syncMessage);
          state.lastSyncedVersion = currentVersion;
        }
        state.wasHoldingWand = true;
      } else if (state.wasHoldingWand) {
        NetworkHandlerManager.sendToPlayer(player, new SyncInteractionDataMessage(null));
        state.wasHoldingWand = false;
      }
    }
  }

  private static class PlayerSyncState {
    boolean wasHoldingWand;
    long lastSyncedVersion = -1;
  }
}
