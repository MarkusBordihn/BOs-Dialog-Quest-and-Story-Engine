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

package de.markusbordihn.dialogqueststoryengine.mixin;

import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepEvents;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/** Fabric API 1.20.1 has no item pickup callback, so the pickup is detected via playerTouch. */
@Mixin(ItemEntity.class)
public abstract class ItemEntityMixin {

  @Unique private int dqse$countBefore;

  @Inject(method = "playerTouch", at = @At("HEAD"))
  private void dqse$capturePrePickupCount(Player player, CallbackInfo callbackInfo) {
    this.dqse$countBefore = ((ItemEntity) (Object) this).getItem().getCount();
  }

  @Inject(method = "playerTouch", at = @At("TAIL"))
  private void dqse$handlePostPickup(Player player, CallbackInfo callbackInfo) {
    ItemEntity itemEntity = (ItemEntity) (Object) this;
    if (!player.level().isClientSide()
        && (itemEntity.isRemoved() || itemEntity.getItem().getCount() < this.dqse$countBefore)) {
      QuestStepEvents.handleItemPickup(player);
    }
  }
}
