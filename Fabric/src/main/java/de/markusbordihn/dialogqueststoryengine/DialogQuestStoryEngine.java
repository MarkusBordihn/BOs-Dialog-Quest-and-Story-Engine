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

package de.markusbordihn.dialogqueststoryengine;

import de.markusbordihn.dialogqueststoryengine.block.ModBlocks;
import de.markusbordihn.dialogqueststoryengine.commands.manager.CommandManager;
import de.markusbordihn.dialogqueststoryengine.config.DqseSecurityConfig;
import de.markusbordihn.dialogqueststoryengine.content.DataPackReloadNotifier;
import de.markusbordihn.dialogqueststoryengine.content.ResourceServerEventsFabric;
import de.markusbordihn.dialogqueststoryengine.core.DqseBootstrap;
import de.markusbordihn.dialogqueststoryengine.entity.InteractionEventHandler;
import de.markusbordihn.dialogqueststoryengine.entity.QuestStepEventHandler;
import de.markusbordihn.dialogqueststoryengine.item.ModItems;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandler;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManagerType;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import de.markusbordihn.dialogqueststoryengine.session.SessionCloseReason;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import de.markusbordihn.dialogqueststoryengine.state.PlayerProgressSync;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateStorage;
import de.markusbordihn.dialogqueststoryengine.tabs.ModTabs;
import java.io.File;
import java.nio.file.Path;
import java.util.UUID;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.entity.event.v1.ServerEntityWorldChangeEvents;
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.packs.PackType;
import net.minecraft.world.level.storage.LevelResource;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DialogQuestStoryEngine implements ModInitializer {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static File resolvePlayerDataFile(Path worldRoot, UUID playerUuid) {
    return worldRoot.resolve("dqse_playerdata").resolve(playerUuid + ".dat").toFile();
  }

  private static CompoundTag readPlayerNbt(File dataFile, UUID playerUuid) {
    return PlayerStateStorage.read(dataFile.toPath(), playerUuid);
  }

  private static boolean writePlayerNbt(File dataFile, CompoundTag nbt, UUID playerUuid) {
    return PlayerStateStorage.write(dataFile.toPath(), nbt, playerUuid);
  }

  @Override
  public void onInitialize() {
    log.info("Initializing {} (Fabric) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FabricLoader.getInstance().getGameDir();
    Constants.CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    DqseSecurityConfig.load(Constants.CONFIG_DIR);
    DqseBootstrap.initialize();

    log.info("{} Blocks ...", Constants.LOG_REGISTER_PREFIX);
    ModBlocks.registerModBlocks();

    log.info("{} Items ...", Constants.LOG_REGISTER_PREFIX);
    ModItems.registerModItems();

    log.info("{} Creative Tabs ...", Constants.LOG_REGISTER_PREFIX);
    ModTabs.registerCreativeTabs();

    log.info("{} Network ...", Constants.LOG_REGISTER_PREFIX);
    NetworkHandler.register();
    NetworkHandlerManager.registerNetworkMessages(NetworkHandlerManagerType.SERVER);

    log.info("{} Commands ...", Constants.LOG_REGISTER_PREFIX);
    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> CommandManager.registerCommands(dispatcher));

    log.info("{} Fabric Event Handlers ...", Constants.LOG_REGISTER_PREFIX);
    ServerLifecycleEvents.SERVER_STARTED.register(ServerEvents::handleServerStarting);
    ServerLifecycleEvents.SERVER_STOPPING.register(ServerEvents::handleServerStopping);

    ServerPlayConnectionEvents.JOIN.register(
        (handler, packetSender, server) -> {
          UUID playerUuid = handler.player.getUUID();
          File dataFile =
              resolvePlayerDataFile(server.getWorldPath(LevelResource.ROOT), playerUuid);
          CompoundTag nbt = readPlayerNbt(dataFile, playerUuid);
          PlayerStateService.onPlayerDataLoaded(playerUuid, nbt != null ? nbt : new CompoundTag());
          PlayerProgressSync.send(handler.player);
        });

    ServerPlayConnectionEvents.DISCONNECT.register(
        (handler, server) -> {
          UUID playerUuid = handler.player.getUUID();
          CompoundTag nbt = PlayerStateService.getPlayerDataForSave(playerUuid);
          if (nbt != null) {
            File dataFile =
                resolvePlayerDataFile(server.getWorldPath(LevelResource.ROOT), playerUuid);
            if (writePlayerNbt(dataFile, nbt, playerUuid)) {
              PlayerStateService.markPlayerDataSaved(playerUuid);
            }
          }
          PlayerStateService.onPlayerLoggedOut(playerUuid);
          SessionManager.invalidatePlayerSessions(playerUuid);
        });

    ServerEntityWorldChangeEvents.AFTER_PLAYER_CHANGE_WORLD.register(
        (player, origin, destination) ->
            SessionManager.closePlayerSessions(player, SessionCloseReason.CONTEXT_CHANGED));
    ServerPlayerEvents.AFTER_RESPAWN.register(
        (oldPlayer, newPlayer, alive) ->
            SessionManager.closePlayerSessions(newPlayer, SessionCloseReason.CONTEXT_CHANGED));

    InteractionEventHandler.registerEvents();
    QuestStepEventHandler.registerEvents();

    log.info("{} Data Pack Reload Listeners ...", Constants.LOG_REGISTER_PREFIX);
    ResourceServerEventsFabric.registerReloadListeners(
        ResourceManagerHelper.get(PackType.SERVER_DATA));
    DataPackReloadNotifier.subscribe(SessionManager::invalidateAll);

    log.info("{} Server Tick Events ...", Constants.LOG_REGISTER_PREFIX);
    ServerTickEvents.END_SERVER_TICK.register(ServerEvents::handleServerTick);
  }
}
