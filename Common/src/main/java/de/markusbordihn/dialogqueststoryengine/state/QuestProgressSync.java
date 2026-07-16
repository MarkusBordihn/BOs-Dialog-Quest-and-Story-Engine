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

package de.markusbordihn.dialogqueststoryengine.state;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestDisplayCatalogEntry;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDisplayCatalogUpsertPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestChangeResult;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestDisplayCatalogService;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public final class QuestProgressSync {

  private static final Map<UUID, Map<ResourceLocation, QuestDisplayCatalogEntry>> sentCatalog =
      new ConcurrentHashMap<>();

  private QuestProgressSync() {}

  public static void register() {
    PlayerStateEvents.addQuestChangedListener(QuestProgressSync::onQuestChanged);
  }

  public static void seedCatalog(UUID playerUuid, List<QuestDisplayCatalogEntry> entries) {
    Map<ResourceLocation, QuestDisplayCatalogEntry> snapshot = new ConcurrentHashMap<>();
    for (QuestDisplayCatalogEntry entry : entries) {
      snapshot.put(entry.questId(), entry);
    }
    sentCatalog.put(playerUuid, snapshot);
  }

  public static void forget(UUID playerUuid) {
    sentCatalog.remove(playerUuid);
  }

  private static void onQuestChanged(UUID playerUuid, QuestChangeResult change) {
    ServerPlayer player = resolveOnlinePlayer(playerUuid);
    if (player == null) {
      return;
    }
    PlayerState playerState = PlayerStateService.get(playerUuid).orElse(null);
    if (playerState == null) {
      return;
    }

    upsertCatalog(player, playerState, change.questId());

    QuestProgress progress = change.questProgress();
    NetworkHandlerManager.sendToPlayer(
        player,
        new QuestDeltaPacket(
            change.questId(),
            progress.state(),
            change.changedSteps(),
            progress.rewardClaimState(),
            progress.revision()));

    for (ResourceLocation dependent : QuestContentRegistry.dependentsOf(change.questId())) {
      upsertCatalog(player, playerState, dependent);
    }
  }

  private static void upsertCatalog(
      ServerPlayer player, PlayerState playerState, ResourceLocation questId) {
    QuestContentRegistry.get(questId)
        .filter(definition -> QuestDisplayCatalogService.isVisible(player, playerState, definition))
        .ifPresent(
            definition -> {
              QuestDisplayCatalogEntry entry =
                  QuestDisplayCatalogService.buildEntry(player, playerState, definition);
              Map<ResourceLocation, QuestDisplayCatalogEntry> cache =
                  sentCatalog.computeIfAbsent(
                      player.getUUID(), ignored -> new ConcurrentHashMap<>());
              if (!entry.equals(cache.get(questId))) {
                NetworkHandlerManager.sendToPlayer(
                    player, new QuestDisplayCatalogUpsertPacket(entry));
                cache.put(questId, entry);
              }
            });
  }

  private static ServerPlayer resolveOnlinePlayer(UUID playerUuid) {
    MinecraftServer server = ServerEvents.getServer();
    return server == null ? null : server.getPlayerList().getPlayer(playerUuid);
  }
}
