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

package de.markusbordihn.dialogqueststoryengine.entity;

import de.markusbordihn.dialogqueststoryengine.Constants;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.event.player.UseEntityCallback;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@SuppressWarnings("unused")
public class InteractionEventHandler {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private InteractionEventHandler() {}

  public static void registerEvents() {
    log.info("{} Interaction Event Handlers ...", Constants.LOG_REGISTER_PREFIX);

    UseEntityCallback.EVENT.register(
        (player, world, hand, entity, hitResult) -> {
          if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            InteractionEvents.handleRightClickEntity(player, entity);
          }
          return InteractionResult.PASS;
        });

    UseBlockCallback.EVENT.register(
        (player, world, hand, hitResult) -> {
          if (!world.isClientSide() && hand == InteractionHand.MAIN_HAND) {
            InteractionEvents.handleRightClickBlock(player, hitResult.getBlockPos(), world);
          }
          return InteractionResult.PASS;
        });

    ServerTickEvents.END_WORLD_TICK.register(
        level -> {
          if (level instanceof ServerLevel) {
            for (Player player : level.players()) {
              InteractionEvents.handlePlayerTick(player);
            }
          }
        });
  }
}
