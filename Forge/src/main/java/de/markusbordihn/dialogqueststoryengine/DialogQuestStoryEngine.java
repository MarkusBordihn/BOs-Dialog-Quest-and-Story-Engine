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

import de.markusbordihn.dialogqueststoryengine.commands.CommandsEventHandler;
import de.markusbordihn.dialogqueststoryengine.content.DataPackReloadEventHandler;
import de.markusbordihn.dialogqueststoryengine.entity.InteractionEventHandler;
import de.markusbordihn.dialogqueststoryengine.item.ModItems;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandler;
import de.markusbordihn.dialogqueststoryengine.server.PlayerStateEventHandler;
import de.markusbordihn.dialogqueststoryengine.server.ServerEventHandler;
import de.markusbordihn.dialogqueststoryengine.tabs.ModTabs;
import de.markusbordihn.dialogqueststoryengine.validation.BuiltinValidators;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
@Mod(Constants.MOD_ID)
public class DialogQuestStoryEngine {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @SuppressWarnings({"java:S1118", "java:S2440"})
  public DialogQuestStoryEngine(FMLJavaModLoadingContext context) {
    final IEventBus modEventBus = context.getModEventBus();

    log.info("Initializing {} (Forge) ...", Constants.MOD_NAME);

    log.info("{} Constants ...", Constants.LOG_REGISTER_PREFIX);
    Constants.GAME_DIR = FMLPaths.GAMEDIR.get();
    Constants.CONFIG_DIR = FMLPaths.CONFIGDIR.get();

    log.info("{} Validators ...", Constants.LOG_REGISTER_PREFIX);
    BuiltinValidators.register();

    log.info("{} Items ...", Constants.LOG_REGISTER_PREFIX);
    ModItems.ITEMS.register(modEventBus);

    log.info("{} Creative Tabs ...", Constants.LOG_REGISTER_PREFIX);
    ModTabs.CREATIVE_TABS.register(modEventBus);

    log.info("{} Network ...", Constants.LOG_REGISTER_PREFIX);
    NetworkHandler.register();

    log.info("{} Forge Event Handlers ...", Constants.LOG_REGISTER_PREFIX);
    MinecraftForge.EVENT_BUS.register(CommandsEventHandler.class);
    MinecraftForge.EVENT_BUS.register(DataPackReloadEventHandler.class);
    MinecraftForge.EVENT_BUS.register(PlayerStateEventHandler.class);
    MinecraftForge.EVENT_BUS.register(ServerEventHandler.class);
    MinecraftForge.EVENT_BUS.register(InteractionEventHandler.class);

    DistExecutor.unsafeRunWhenOn(
        Dist.CLIENT, () -> () -> new DialogQuestStoryEngineClient(modEventBus));
  }
}
