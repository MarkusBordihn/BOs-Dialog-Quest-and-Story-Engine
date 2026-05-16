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

package de.markusbordihn.dialogqueststoryengine.item;

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionSavedData;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.InteractionListMessage;
import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class InteractionWandItem extends Item {

  public InteractionWandItem() {
    super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Player player = context.getPlayer();
    Level level = context.getLevel();
    if (player == null) {
      return InteractionResult.PASS;
    }
    if (level.isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    if (!player.hasPermissions(2)) {
      player.sendSystemMessage(
          Component.literal("Insufficient permissions.").withStyle(ChatFormatting.RED));
      return InteractionResult.FAIL;
    }

    BlockPos pos = context.getClickedPos();
    UUID targetId = BlockUUID.fromBlockPos(level.dimension(), pos);
    TargetKind kind =
        level.getBlockEntity(pos) != null ? TargetKind.BLOCK_ENTITY : TargetKind.BLOCK;

    List<InteractionEntry> existing =
        InteractionSavedData.get(((ServerPlayer) player).server).getInteractionsForTarget(targetId);
    NetworkHandlerManager.sendToPlayer(
        (ServerPlayer) player,
        new InteractionListMessage(existing, targetId, kind, level.dimension().location(), pos));
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
    if (player.level().isClientSide()) {
      return InteractionResult.SUCCESS;
    }
    if (!player.hasPermissions(2)) {
      player.sendSystemMessage(
          Component.literal("Insufficient permissions.").withStyle(ChatFormatting.RED));
      return InteractionResult.FAIL;
    }

    UUID targetId = target.getUUID();

    List<InteractionEntry> existing =
        InteractionSavedData.get(((ServerPlayer) player).server).getInteractionsForTarget(targetId);
    NetworkHandlerManager.sendToPlayer(
        (ServerPlayer) player,
        new InteractionListMessage(
            existing, targetId, TargetKind.ENTITY, target.level().dimension().location(), null));
    return InteractionResult.SUCCESS;
  }

  @Override
  public void appendHoverText(
      ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    tooltip.add(
        Component.literal("Right-click block/entity: open interaction config")
            .withStyle(ChatFormatting.GRAY));
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return true;
  }
}
