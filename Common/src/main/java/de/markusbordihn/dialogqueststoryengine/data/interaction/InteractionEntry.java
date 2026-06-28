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

package de.markusbordihn.dialogqueststoryengine.data.interaction;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record InteractionEntry(
    InteractionSource source,
    InteractionEventType eventType,
    UUID targetId,
    TargetKind targetKind,
    InteractionType interactionType,
    String label,
    ResourceLocation dimension,
    BlockPos blockPos,
    ActionDataSet actionDataSet) {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String TAG_SOURCE = "Source";
  private static final String TAG_EVENT_TYPE = "eventType";
  private static final String TAG_TARGET_ID = "TargetId";
  private static final String TAG_TARGET_KIND = "TargetKind";
  private static final String TAG_INTERACTION_TYPE = "InteractionType";
  private static final String TAG_LABEL = "Label";
  private static final String TAG_DIMENSION = "Dimension";
  private static final String TAG_BLOCK_X = "BlockX";
  private static final String TAG_ACTION_DATA = "ActionData";
  private static final String TAG_BLOCK_Y = "BlockY";
  private static final String TAG_BLOCK_Z = "BlockZ";
  private static final String TAG_HAS_BLOCK_POS = "HasBlockPos";

  public static InteractionEntry forEntityInteract(
      UUID targetId, InteractionType interactionType, String label, ResourceLocation dimension) {
    return new InteractionEntry(
        InteractionSource.WAND,
        InteractionEventType.ON_ENTITY_INTERACT,
        targetId,
        TargetKind.ENTITY,
        interactionType,
        label,
        dimension,
        null,
        new ActionDataSet());
  }

  public static InteractionEntry createTemplate(
      UUID targetId, TargetKind targetKind, BlockPos blockPos, ResourceLocation dimension) {
    InteractionEventType eventType =
        targetKind == TargetKind.ENTITY
            ? InteractionEventType.ON_ENTITY_INTERACT
            : InteractionEventType.ON_BLOCK_INTERACT;
    return new InteractionEntry(
        InteractionSource.WAND,
        eventType,
        targetId,
        targetKind,
        InteractionType.RIGHT_CLICK,
        "",
        dimension,
        blockPos,
        new ActionDataSet());
  }

  public static InteractionEntry forBlockInteract(
      UUID targetId,
      BlockPos blockPos,
      TargetKind targetKind,
      InteractionType interactionType,
      String label,
      ResourceLocation dimension) {
    InteractionEventType eventType = resolveEventType(targetKind, interactionType);
    return new InteractionEntry(
        InteractionSource.WAND,
        eventType,
        targetId,
        targetKind,
        interactionType,
        label,
        dimension,
        blockPos,
        new ActionDataSet());
  }

  private static InteractionEventType resolveEventType(
      TargetKind targetKind, InteractionType interactionType) {
    if (targetKind == TargetKind.ENTITY) {
      return InteractionEventType.ON_ENTITY_INTERACT;
    }
    return switch (interactionType) {
      case STEP_ON -> InteractionEventType.ON_STEP_ON;
      case OPEN_HOLOPAD -> InteractionEventType.ON_HOLOPAD_USE;
      default -> InteractionEventType.ON_BLOCK_INTERACT;
    };
  }

  public static InteractionEntry load(CompoundTag tag) {
    try {
      UUID targetId = tag.getUUID(TAG_TARGET_ID);
      String label = tag.getString(TAG_LABEL);
      ResourceLocation dimension = ResourceLocation.tryParse(tag.getString(TAG_DIMENSION));
      if (dimension == null) {
        log.warn(
            "Skipping interaction entry with invalid dimension: {}", tag.getString(TAG_DIMENSION));
        return null;
      }

      BlockPos blockPos = null;
      if (tag.getBoolean(TAG_HAS_BLOCK_POS)) {
        blockPos =
            new BlockPos(tag.getInt(TAG_BLOCK_X), tag.getInt(TAG_BLOCK_Y), tag.getInt(TAG_BLOCK_Z));
      }

      InteractionSource source = InteractionSource.fromName(tag.getString(TAG_SOURCE));
      InteractionEventType eventType = InteractionEventType.fromName(tag.getString(TAG_EVENT_TYPE));
      TargetKind targetKind = TargetKind.fromName(tag.getString(TAG_TARGET_KIND));
      InteractionType interactionType =
          InteractionType.fromName(tag.getString(TAG_INTERACTION_TYPE));
      if (source == null || eventType == null || targetKind == null || interactionType == null) {
        log.warn(
            "Skipping interaction entry with invalid fields: source={}, eventType={}, targetKind={}, interactionType={}",
            tag.getString(TAG_SOURCE),
            tag.getString(TAG_EVENT_TYPE),
            tag.getString(TAG_TARGET_KIND),
            tag.getString(TAG_INTERACTION_TYPE));
        return null;
      }

      ActionDataSet actionDataSet =
          tag.contains(TAG_ACTION_DATA)
              ? ActionDataSet.load(tag.getCompound(TAG_ACTION_DATA))
              : new ActionDataSet();
      return new InteractionEntry(
          source,
          eventType,
          targetId,
          targetKind,
          interactionType,
          label,
          dimension,
          blockPos,
          actionDataSet);
    } catch (Exception e) {
      log.warn("Failed to load interaction entry: {}", e.getMessage());
      return null;
    }
  }

  public static InteractionEntry readFromBuf(FriendlyByteBuf buf) {
    InteractionSource source = buf.readEnum(InteractionSource.class);
    InteractionEventType eventType = buf.readEnum(InteractionEventType.class);
    UUID targetId = buf.readUUID();
    TargetKind targetKind = buf.readEnum(TargetKind.class);
    InteractionType interactionType = buf.readEnum(InteractionType.class);
    String label = buf.readUtf();
    ResourceLocation dimension = buf.readResourceLocation();
    BlockPos blockPos = buf.readBoolean() ? buf.readBlockPos() : null;
    ActionDataSet actionDataSet = ActionDataSet.readFromBuf(buf);
    return new InteractionEntry(
        source,
        eventType,
        targetId,
        targetKind,
        interactionType,
        label,
        dimension,
        blockPos,
        actionDataSet);
  }

  public InteractionEntry withEdits(InteractionType updatedInteractionType, String updatedLabel) {
    return withEdits(updatedInteractionType, updatedLabel, this.actionDataSet);
  }

  public InteractionEntry withEdits(
      InteractionType updatedInteractionType,
      String updatedLabel,
      ActionDataSet updatedActionDataSet) {
    InteractionEventType updatedEventType =
        resolveEventType(this.targetKind, updatedInteractionType);
    return new InteractionEntry(
        this.source,
        updatedEventType,
        this.targetId,
        this.targetKind,
        updatedInteractionType,
        updatedLabel,
        this.dimension,
        this.blockPos,
        updatedActionDataSet);
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putString(TAG_SOURCE, source.name());
    tag.putString(TAG_EVENT_TYPE, this.eventType.name());
    tag.putUUID(TAG_TARGET_ID, targetId);
    tag.putString(TAG_TARGET_KIND, targetKind.name());
    tag.putString(TAG_INTERACTION_TYPE, interactionType.name());
    tag.putString(TAG_LABEL, label);
    tag.putString(TAG_DIMENSION, dimension.toString());
    if (blockPos != null) {
      tag.putBoolean(TAG_HAS_BLOCK_POS, true);
      tag.putInt(TAG_BLOCK_X, blockPos.getX());
      tag.putInt(TAG_BLOCK_Y, blockPos.getY());
      tag.putInt(TAG_BLOCK_Z, blockPos.getZ());
    } else {
      tag.putBoolean(TAG_HAS_BLOCK_POS, false);
    }
    if (!actionDataSet.isEmpty()) {
      tag.put(TAG_ACTION_DATA, actionDataSet.save());
    }
    return tag;
  }

  public void writeToBuf(FriendlyByteBuf buf) {
    buf.writeEnum(source);
    buf.writeEnum(eventType);
    buf.writeUUID(targetId);
    buf.writeEnum(targetKind);
    buf.writeEnum(interactionType);
    buf.writeUtf(label);
    buf.writeResourceLocation(dimension);
    boolean hasPos = blockPos != null;
    buf.writeBoolean(hasPos);
    if (hasPos) {
      buf.writeBlockPos(blockPos);
    }
    actionDataSet.writeToBuf(buf);
  }

  @Override
  public String toString() {
    String shortId = targetId.toString().substring(0, 8);
    if (blockPos != null) {
      return String.format(
          "[%s/%s] %s '%s' at %s (ID: %s) in %s",
          source,
          this.eventType.resourceLocation().getPath(),
          targetKind,
          label,
          blockPos.toShortString(),
          shortId,
          dimension);
    }
    return String.format(
        "[%s/%s] %s '%s' (ID: %s) in %s",
        source, this.eventType.resourceLocation().getPath(), targetKind, label, shortId, dimension);
  }
}
