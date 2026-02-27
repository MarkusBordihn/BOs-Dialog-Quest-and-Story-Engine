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

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

@SuppressWarnings("unused")
public class InteractionEventHandler {

  @SubscribeEvent
  public static void handleEntityInteract(PlayerInteractEvent.EntityInteract event) {
    if (!event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
      InteractionEvents.handleRightClickEntity(event.getEntity(), event.getTarget());
    }
  }

  @SubscribeEvent
  public static void handleRightClickBlock(PlayerInteractEvent.RightClickBlock event) {
    if (!event.getLevel().isClientSide() && event.getHand() == InteractionHand.MAIN_HAND) {
      InteractionEvents.handleRightClickBlock(event.getEntity(), event.getPos(), event.getLevel());
    }
  }

  @SubscribeEvent
  public static void handleLivingTick(LivingEvent.LivingTickEvent event) {
    if (event.getEntity() instanceof Player player) {
      InteractionEvents.handlePlayerTick(player);
    }
  }
}
