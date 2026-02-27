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

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionData;
import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.List;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

public class InteractionWandItem extends Item {

  private static final String TAG_MODE = "Mode";

  public InteractionWandItem() {
    super(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC));
  }

  private static InteractionType getMode(ItemStack stack) {
    CompoundTag tag = stack.getTag();
    if (tag != null && tag.contains(TAG_MODE)) {
      InteractionType type = InteractionType.fromName(tag.getString(TAG_MODE));
      if (type != null) {
        return type;
      }
    }
    return InteractionType.RIGHT_CLICK;
  }

  private static void setMode(ItemStack stack, InteractionType type) {
    stack.getOrCreateTag().putString(TAG_MODE, type.name());
  }

  private static InteractionType cycleMode(ItemStack stack) {
    InteractionType current = getMode(stack);
    InteractionType[] values = InteractionType.values();
    InteractionType next = values[(current.ordinal() + 1) % values.length];
    setMode(stack, next);
    return next;
  }

  @Override
  public InteractionResult useOn(UseOnContext context) {
    Player player = context.getPlayer();
    Level level = context.getLevel();
    if (player == null || level.isClientSide()) {
      return InteractionResult.PASS;
    }
    if (!player.hasPermissions(2)) {
      player.sendSystemMessage(
          Component.literal("Insufficient permissions.").withStyle(ChatFormatting.RED));
      return InteractionResult.FAIL;
    }

    InteractionData data = InteractionData.get();
    if (data == null) {
      return InteractionResult.FAIL;
    }

    BlockPos pos = context.getClickedPos();
    UUID targetId = BlockUUID.fromBlockPos(level.dimension(), pos);
    InteractionType mode = getMode(context.getItemInHand());

    TargetKind kind =
        level.getBlockEntity(pos) != null ? TargetKind.BLOCK_ENTITY : TargetKind.BLOCK;

    if (data.hasInteraction(targetId, mode)) {
      data.unregister(targetId, mode);
      player.sendSystemMessage(
          Component.literal(
                  "✖ Removed "
                      + mode
                      + " interaction from "
                      + kind
                      + " at "
                      + pos.toShortString()
                      + ".")
              .withStyle(ChatFormatting.YELLOW));
    } else {
      String label = "mapped_" + mode.name().toLowerCase();
      InteractionDataEntry entry =
          new InteractionDataEntry(targetId, mode, kind, label, level.dimension().location(), pos);
      data.register(entry);
      player.sendSystemMessage(
          Component.literal(
                  "✔ Registered "
                      + mode
                      + " interaction '"
                      + label
                      + "' on "
                      + kind
                      + " at "
                      + pos.toShortString()
                      + ".")
              .withStyle(ChatFormatting.GREEN));
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResult interactLivingEntity(
      ItemStack stack, Player player, LivingEntity target, InteractionHand hand) {
    if (player.level().isClientSide()) {
      return InteractionResult.PASS;
    }
    if (!player.hasPermissions(2)) {
      player.sendSystemMessage(
          Component.literal("Insufficient permissions.").withStyle(ChatFormatting.RED));
      return InteractionResult.FAIL;
    }

    InteractionData data = InteractionData.get();
    if (data == null) {
      return InteractionResult.FAIL;
    }

    UUID targetId = target.getUUID();
    InteractionType mode = InteractionType.RIGHT_CLICK;

    if (data.hasInteraction(targetId, mode)) {
      data.unregister(targetId, mode);
      player.sendSystemMessage(
          Component.literal("✖ Removed " + mode + " interaction from " + TargetKind.ENTITY + ".")
              .withStyle(ChatFormatting.YELLOW));
    } else {
      String label = "mapped_" + mode.name().toLowerCase();
      InteractionDataEntry entry =
          new InteractionDataEntry(
              targetId,
              mode,
              TargetKind.ENTITY,
              label,
              target.level().dimension().location(),
              null);
      data.register(entry);
      player.sendSystemMessage(
          Component.literal(
                  "✔ Registered "
                      + mode
                      + " interaction '"
                      + label
                      + "' on "
                      + TargetKind.ENTITY
                      + ".")
              .withStyle(ChatFormatting.GREEN));
    }
    return InteractionResult.SUCCESS;
  }

  @Override
  public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
    ItemStack stack = player.getItemInHand(hand);
    if (level.isClientSide()) {
      return InteractionResultHolder.pass(stack);
    }
    if (player.isShiftKeyDown()) {
      InteractionType next = cycleMode(stack);
      player.sendSystemMessage(
          Component.literal("⟳ Wand mode: " + next.name()).withStyle(ChatFormatting.AQUA));
      return InteractionResultHolder.success(stack);
    }
    return InteractionResultHolder.pass(stack);
  }

  @Override
  public void appendHoverText(
      ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flag) {
    InteractionType mode = getMode(stack);
    tooltip.add(Component.literal("Mode: " + mode.name()).withStyle(ChatFormatting.AQUA));
    tooltip.add(
        Component.literal("Target is auto-detected as ENTITY, BLOCK or BLOCK_ENTITY")
            .withStyle(ChatFormatting.GRAY));
    tooltip.add(
        Component.literal("Right-click: toggle interaction").withStyle(ChatFormatting.DARK_GRAY));
    tooltip.add(
        Component.literal("Shift+Right-click air: cycle mode").withStyle(ChatFormatting.DARK_GRAY));
  }

  @Override
  public boolean isFoil(ItemStack stack) {
    return true;
  }
}
