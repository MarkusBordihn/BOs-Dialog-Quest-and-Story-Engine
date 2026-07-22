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
    ActionDataSet actionDataSet,
    boolean cancelDefaultAction) {

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
  private static final String TAG_CANCEL_DEFAULT = "CancelDefaultAction";

  public InteractionEntry(
      InteractionSource source,
      InteractionEventType eventType,
      UUID targetId,
      TargetKind targetKind,
      InteractionType interactionType,
      String label,
      ResourceLocation dimension,
      BlockPos blockPos,
      ActionDataSet actionDataSet) {
    this(
        source,
        eventType,
        targetId,
        targetKind,
        interactionType,
        label,
        dimension,
        blockPos,
        actionDataSet,
        false);
  }

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
          actionDataSet,
          tag.getBoolean(TAG_CANCEL_DEFAULT));
    } catch (Exception e) {
      log.warn("Failed to load interaction entry: {}", e.getMessage());
      return null;
    }
  }

  public static InteractionEntry readFromBuffer(FriendlyByteBuf buffer) {
    InteractionSource source = buffer.readEnum(InteractionSource.class);
    InteractionEventType eventType = buffer.readEnum(InteractionEventType.class);
    UUID targetId = buffer.readUUID();
    TargetKind targetKind = buffer.readEnum(TargetKind.class);
    InteractionType interactionType = buffer.readEnum(InteractionType.class);
    String label = buffer.readUtf();
    ResourceLocation dimension = buffer.readResourceLocation();
    BlockPos blockPos = buffer.readBoolean() ? buffer.readBlockPos() : null;
    ActionDataSet actionDataSet = ActionDataSet.readFromBuffer(buffer);
    boolean cancelDefaultAction = buffer.readBoolean();
    return new InteractionEntry(
        source,
        eventType,
        targetId,
        targetKind,
        interactionType,
        label,
        dimension,
        blockPos,
        actionDataSet,
        cancelDefaultAction);
  }

  public InteractionEntry withEdits(InteractionType updatedInteractionType, String updatedLabel) {
    return this.withEdits(updatedInteractionType, updatedLabel, this.actionDataSet);
  }

  public InteractionEntry withEdits(
      InteractionType updatedInteractionType,
      String updatedLabel,
      ActionDataSet updatedActionDataSet) {
    return this.withEdits(
        updatedInteractionType, updatedLabel, updatedActionDataSet, this.cancelDefaultAction);
  }

  public InteractionEntry withEdits(
      InteractionType updatedInteractionType,
      String updatedLabel,
      ActionDataSet updatedActionDataSet,
      boolean updatedCancelDefaultAction) {
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
        updatedActionDataSet,
        updatedCancelDefaultAction);
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putString(TAG_SOURCE, this.source.name());
    tag.putString(TAG_EVENT_TYPE, this.eventType.name());
    tag.putUUID(TAG_TARGET_ID, this.targetId);
    tag.putString(TAG_TARGET_KIND, this.targetKind.name());
    tag.putString(TAG_INTERACTION_TYPE, this.interactionType.name());
    tag.putString(TAG_LABEL, this.label);
    tag.putString(TAG_DIMENSION, this.dimension.toString());
    if (this.blockPos != null) {
      tag.putBoolean(TAG_HAS_BLOCK_POS, true);
      tag.putInt(TAG_BLOCK_X, this.blockPos.getX());
      tag.putInt(TAG_BLOCK_Y, this.blockPos.getY());
      tag.putInt(TAG_BLOCK_Z, this.blockPos.getZ());
    } else {
      tag.putBoolean(TAG_HAS_BLOCK_POS, false);
    }
    if (!this.actionDataSet.isEmpty()) {
      tag.put(TAG_ACTION_DATA, this.actionDataSet.save());
    }
    if (this.cancelDefaultAction) {
      tag.putBoolean(TAG_CANCEL_DEFAULT, true);
    }
    return tag;
  }

  public void writeToBuffer(FriendlyByteBuf buffer) {
    buffer.writeEnum(this.source);
    buffer.writeEnum(this.eventType);
    buffer.writeUUID(this.targetId);
    buffer.writeEnum(this.targetKind);
    buffer.writeEnum(this.interactionType);
    buffer.writeUtf(this.label);
    buffer.writeResourceLocation(this.dimension);
    boolean hasPos = this.blockPos != null;
    buffer.writeBoolean(hasPos);
    if (hasPos) {
      buffer.writeBlockPos(this.blockPos);
    }
    this.actionDataSet.writeToBuffer(buffer);
    buffer.writeBoolean(this.cancelDefaultAction);
  }

  @Override
  public String toString() {
    String shortId = this.targetId.toString().substring(0, 8);
    if (this.blockPos != null) {
      return String.format(
          "[%s/%s] %s '%s' at %s (ID: %s) in %s",
          this.source,
          this.eventType.resourceLocation().getPath(),
          this.targetKind,
          this.label,
          this.blockPos.toShortString(),
          shortId,
          this.dimension);
    }
    return String.format(
        "[%s/%s] %s '%s' (ID: %s) in %s",
        this.source,
        this.eventType.resourceLocation().getPath(),
        this.targetKind,
        this.label,
        shortId,
        this.dimension);
  }
}
