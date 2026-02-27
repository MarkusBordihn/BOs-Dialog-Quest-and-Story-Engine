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
import de.markusbordihn.dialogqueststoryengine.commands.BindManager;
import de.markusbordihn.dialogqueststoryengine.commands.BindManager.BindContext;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionData;
import de.markusbordihn.dialogqueststoryengine.item.InteractionWandItem;
import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InteractionEvents {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Map<UUID, BlockPos> lastPlayerBlockPos = new HashMap<>();

  private InteractionEvents() {}

  public static void handlePlayerTick(Player player) {
    if (player.level().isClientSide()) {
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null || !data.hasStepOnInteractions()) {
      return;
    }
    BlockPos currentPos = player.blockPosition().below();
    BlockPos lastPos = lastPlayerBlockPos.get(player.getUUID());
    if (lastPos == null || !lastPos.equals(currentPos)) {
      lastPlayerBlockPos.put(player.getUUID(), currentPos);
      if (lastPos != null) {
        handleStepOnBlock(player, currentPos, player.level());
      }
    }
  }

  public static void clearTrackingData() {
    lastPlayerBlockPos.clear();
  }

  public static void handleRightClickEntity(Player player, Entity target) {
    if (player.level().isClientSide()) {
      return;
    }
    if (player.getMainHandItem().getItem() instanceof InteractionWandItem) {
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null) {
      return;
    }

    UUID targetId = target.getUUID();

    if (BindManager.isBinding(player)) {
      BindContext ctx = BindManager.consumeBind(player);
      if (ctx.unbind()) {
        if (ctx.type() != null) {
          boolean removed = data.unregister(targetId, ctx.type());
          if (removed) {
            player.sendSystemMessage(
                Component.literal(
                        "\u2716 Removed "
                            + ctx.type()
                            + " interaction from "
                            + TargetKind.ENTITY
                            + ".")
                    .withStyle(ChatFormatting.YELLOW));
          } else {
            player.sendSystemMessage(
                Component.literal(
                        "No "
                            + ctx.type()
                            + " interaction found on this "
                            + TargetKind.ENTITY
                            + ".")
                    .withStyle(ChatFormatting.RED));
          }
        } else {
          int removed = data.unregisterAll(targetId);
          if (removed > 0) {
            player.sendSystemMessage(
                Component.literal(
                        "\u2716 Removed "
                            + removed
                            + " interaction(s) from "
                            + TargetKind.ENTITY
                            + ".")
                    .withStyle(ChatFormatting.YELLOW));
          } else {
            player.sendSystemMessage(
                Component.literal("No interactions found on this " + TargetKind.ENTITY + ".")
                    .withStyle(ChatFormatting.RED));
          }
        }
      } else {
        InteractionDataEntry entry =
            new InteractionDataEntry(
                targetId,
                ctx.type(),
                TargetKind.ENTITY,
                ctx.label(),
                target.level().dimension().location(),
                null);
        data.register(entry);
        player.sendSystemMessage(
            Component.literal(
                    "âœ” Registered "
                        + ctx.type()
                        + " interaction '"
                        + ctx.label()
                        + "' on "
                        + TargetKind.ENTITY
                        + ".")
                .withStyle(ChatFormatting.GREEN));
      }
      return;
    }

    if (data.hasInteraction(targetId, InteractionType.RIGHT_CLICK)) {
      InteractionDataEntry entry = data.getInteraction(targetId, InteractionType.RIGHT_CLICK);
      triggerInteraction(player, entry);
    }
  }

  public static void handleRightClickBlock(Player player, BlockPos pos, Level level) {
    if (level.isClientSide()) {
      return;
    }
    if (player.getMainHandItem().getItem() instanceof InteractionWandItem) {
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null) {
      return;
    }

    UUID targetId = BlockUUID.fromBlockPos(level.dimension(), pos);

    if (BindManager.isBinding(player)) {
      BindContext ctx = BindManager.consumeBind(player);
      if (ctx.unbind()) {
        if (ctx.type() != null) {
          boolean removed = data.unregister(targetId, ctx.type());
          if (removed) {
            player.sendSystemMessage(
                Component.literal(
                        "\u2716 Removed "
                            + ctx.type()
                            + " interaction from block at "
                            + pos.toShortString()
                            + ".")
                    .withStyle(ChatFormatting.YELLOW));
          } else {
            player.sendSystemMessage(
                Component.literal(
                        "No "
                            + ctx.type()
                            + " interaction found on block at "
                            + pos.toShortString()
                            + ".")
                    .withStyle(ChatFormatting.RED));
          }
        } else {
          int removed = data.unregisterAll(targetId);
          if (removed > 0) {
            player.sendSystemMessage(
                Component.literal(
                        "\u2716 Removed "
                            + removed
                            + " interaction(s) from block at "
                            + pos.toShortString()
                            + ".")
                    .withStyle(ChatFormatting.YELLOW));
          } else {
            player.sendSystemMessage(
                Component.literal("No interactions found on block at " + pos.toShortString() + ".")
                    .withStyle(ChatFormatting.RED));
          }
        }
      } else {
        TargetKind kind =
            level.getBlockEntity(pos) != null ? TargetKind.BLOCK_ENTITY : TargetKind.BLOCK;
        InteractionDataEntry entry =
            new InteractionDataEntry(
                targetId, ctx.type(), kind, ctx.label(), level.dimension().location(), pos);
        data.register(entry);
        player.sendSystemMessage(
            Component.literal(
                    "âœ” Registered "
                        + ctx.type()
                        + " interaction '"
                        + ctx.label()
                        + "' on "
                        + kind
                        + " at "
                        + pos.toShortString()
                        + ".")
                .withStyle(ChatFormatting.GREEN));
      }
      return;
    }

    if (data.hasInteraction(targetId, InteractionType.RIGHT_CLICK)) {
      InteractionDataEntry entry = data.getInteraction(targetId, InteractionType.RIGHT_CLICK);
      triggerInteraction(player, entry);
    }
  }

  public static void handleStepOnBlock(Entity entity, BlockPos pos, Level level) {
    if (level.isClientSide() || !(entity instanceof Player player)) {
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null) {
      return;
    }

    UUID targetId = BlockUUID.fromBlockPos(level.dimension(), pos);

    if (data.hasInteraction(targetId, InteractionType.STEP_ON)) {
      InteractionDataEntry entry = data.getInteraction(targetId, InteractionType.STEP_ON);
      triggerInteraction(player, entry);
    }
  }

  private static void triggerInteraction(Player player, InteractionDataEntry entry) {
    log.info(
        "Interaction {} on {} '{}' (UUID: {}) triggered by player {}",
        entry.type(),
        entry.kind(),
        entry.label(),
        entry.targetId(),
        player.getName().getString());
    String posInfo = entry.blockPos() != null ? " at " + entry.blockPos().toShortString() : "";
    player.sendSystemMessage(
        Component.literal(
                "\u25B6 "
                    + entry.type()
                    + " | "
                    + entry.kind()
                    + " '"
                    + entry.label()
                    + "'"
                    + posInfo)
            .withStyle(ChatFormatting.GOLD));
    player.sendSystemMessage(
        Component.literal("  UUID: " + entry.targetId()).withStyle(ChatFormatting.DARK_GRAY));
  }
}
