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
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionSavedData;
import de.markusbordihn.dialogqueststoryengine.interaction.InteractionDispatcher;
import de.markusbordihn.dialogqueststoryengine.interaction.InteractionManager;
import de.markusbordihn.dialogqueststoryengine.item.InteractionWandItem;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepEvents;
import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InteractionEvents {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Map<UUID, PlayerBlockLocation> lastPlayerBlockLocations = new HashMap<>();

  private InteractionEvents() {}

  public static void handlePlayerTick(Player player) {
    if (player.level().isClientSide()) {
      return;
    }

    if (!InteractionManager.hasStepOnInteractions(((ServerPlayer) player).server)) {
      return;
    }

    PlayerBlockLocation currentLocation =
        new PlayerBlockLocation(
            player.level().dimension().location(), player.blockPosition().below());
    PlayerBlockLocation previousLocation = lastPlayerBlockLocations.get(player.getUUID());
    if (!currentLocation.equals(previousLocation)) {
      lastPlayerBlockLocations.put(player.getUUID(), currentLocation);
      if (previousLocation != null) {
        handleStepOnBlock(player, currentLocation.blockPos(), player.level());
      }
    }
  }

  public static void clearTrackingData() {
    lastPlayerBlockLocations.clear();
  }

  public static void handleRightClickEntity(Player player, Entity target) {
    if (player.level().isClientSide()
        || player.getMainHandItem().getItem() instanceof InteractionWandItem) {
      return;
    }

    QuestStepEvents.handleEntityInteract(player, target);

    ServerPlayer serverPlayer = (ServerPlayer) player;
    UUID targetId = target.getUUID();

    if (BindManager.isBinding(player)) {
      handleBind(
          serverPlayer, targetId, TargetKind.ENTITY, null, target.level().dimension().location());
      return;
    }
    InteractionDispatcher.dispatchFor(
        serverPlayer.server, targetId, InteractionEventType.ON_ENTITY_INTERACT, serverPlayer);
  }

  public static void handleRightClickBlock(Player player, BlockPos pos, Level level) {
    if (level.isClientSide() || player.getMainHandItem().getItem() instanceof InteractionWandItem) {
      return;
    }

    ServerPlayer serverPlayer = (ServerPlayer) player;
    UUID targetId = BlockUUID.fromBlockPos(level.dimension(), pos);

    if (BindManager.isBinding(player)) {
      TargetKind kind =
          level.getBlockEntity(pos) != null ? TargetKind.BLOCK_ENTITY : TargetKind.BLOCK;
      handleBind(serverPlayer, targetId, kind, pos, level.dimension().location());
      return;
    }

    InteractionDispatcher.dispatchFor(
        serverPlayer.server, targetId, InteractionEventType.ON_BLOCK_INTERACT, serverPlayer);
  }

  public static void handleStepOnBlock(Entity entity, BlockPos pos, Level level) {
    if (level.isClientSide() || !(entity instanceof Player player)) {
      return;
    }

    ServerPlayer serverPlayer = (ServerPlayer) player;
    InteractionDispatcher.dispatchFor(
        serverPlayer.server,
        BlockUUID.fromBlockPos(level.dimension(), pos),
        InteractionEventType.ON_STEP_ON,
        serverPlayer);
  }

  private static void handleBind(
      ServerPlayer player,
      UUID targetId,
      TargetKind targetKind,
      BlockPos blockPos,
      ResourceLocation dimension) {
    BindContext bindContext = BindManager.consumeBind(player);
    InteractionSavedData data = InteractionSavedData.get(player.server);

    if (bindContext.unbind()) {
      InteractionEventType eventType = resolveEventType(targetKind, bindContext.type());
      if (eventType != null) {
        player.sendSystemMessage(
            data.unregister(targetId, eventType)
                ? Component.literal(
                        "\u2716 Removed "
                            + eventType.resourceLocation().getPath()
                            + " trigger from "
                            + targetKind
                            + locationStr(blockPos)
                            + ".")
                    .withStyle(ChatFormatting.YELLOW)
                : Component.literal(
                        "No "
                            + eventType.resourceLocation().getPath()
                            + " trigger found on "
                            + targetKind
                            + locationStr(blockPos)
                            + ".")
                    .withStyle(ChatFormatting.RED));
      } else {
        int count = data.unregisterAll(targetId);
        player.sendSystemMessage(
            count > 0
                ? Component.literal(
                        "\u2716 Removed "
                            + count
                            + " trigger(s) from "
                            + targetKind
                            + locationStr(blockPos)
                            + ".")
                    .withStyle(ChatFormatting.YELLOW)
                : Component.literal(
                        "No INTERACTIONS found on " + targetKind + locationStr(blockPos) + ".")
                    .withStyle(ChatFormatting.RED));
      }
    } else {
      InteractionEntry entry =
          targetKind == TargetKind.ENTITY
              ? InteractionEntry.forEntityInteract(
                  targetId, bindContext.type(), bindContext.label(), dimension)
              : InteractionEntry.forBlockInteract(
                  targetId,
                  blockPos,
                  targetKind,
                  bindContext.type(),
                  bindContext.label(),
                  dimension);
      data.register(entry);
      player.sendSystemMessage(
          Component.literal(
                  "\u2714 Registered "
                      + entry.eventType().resourceLocation().getPath()
                      + " trigger '"
                      + bindContext.label()
                      + "' on "
                      + targetKind
                      + locationStr(blockPos)
                      + ".")
              .withStyle(ChatFormatting.GREEN));
      log.info("Player {} registered trigger: {}", player.getName().getString(), entry);
    }
  }

  private static InteractionEventType resolveEventType(
      TargetKind targetKind, InteractionType interactionType) {
    if (interactionType == null) {
      return null;
    }

    if (targetKind == TargetKind.ENTITY) {
      return InteractionEventType.ON_ENTITY_INTERACT;
    }

    return interactionType == InteractionType.STEP_ON
        ? InteractionEventType.ON_STEP_ON
        : InteractionEventType.ON_BLOCK_INTERACT;
  }

  private static String locationStr(BlockPos blockPos) {
    return blockPos != null ? " at " + blockPos.toShortString() : "";
  }

  private record PlayerBlockLocation(ResourceLocation dimension, BlockPos blockPos) {}
}
