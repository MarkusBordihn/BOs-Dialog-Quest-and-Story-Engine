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

import de.markusbordihn.dialogqueststoryengine.commands.manager.CommandManager;
import de.markusbordihn.dialogqueststoryengine.content.ResourceServerEventsFabric;
import de.markusbordihn.dialogqueststoryengine.entity.InteractionEventHandler;
import de.markusbordihn.dialogqueststoryengine.item.ModItems;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandler;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManagerType;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import de.markusbordihn.dialogqueststoryengine.tabs.ModTabs;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.resource.ResourceManagerHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.packs.PackType;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class DialogQuestStoryEngine implements ModInitializer {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @Override
  public void onInitialize() {
    log.info("Initializing {} (Fabric) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FabricLoader.getInstance().getGameDir();
    Constants.CONFIG_DIR = FabricLoader.getInstance().getConfigDir();

    ModItems.registerModItems();
    ModTabs.registerCreativeTabs();

    log.info("{} Network ...", Constants.LOG_REGISTER_PREFIX);
    NetworkHandler.register();
    NetworkHandlerManager.registerNetworkMessages(NetworkHandlerManagerType.SERVER);

    CommandRegistrationCallback.EVENT.register(
        (dispatcher, registryAccess, environment) -> CommandManager.registerCommands(dispatcher));

    ServerLifecycleEvents.SERVER_STARTED.register(ServerEvents::handleServerStarting);
    ServerLifecycleEvents.SERVER_STOPPING.register(ServerEvents::handleServerStopping);

    InteractionEventHandler.registerEvents();

    ResourceServerEventsFabric.registerReloadListeners(
        ResourceManagerHelper.get(PackType.SERVER_DATA));

    ServerTickEvents.END_SERVER_TICK.register(ServerEvents::handleServerTick);
  }
}
