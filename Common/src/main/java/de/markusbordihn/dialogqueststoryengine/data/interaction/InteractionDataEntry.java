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
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record InteractionDataEntry(
    UUID targetId,
    InteractionType type,
    TargetKind kind,
    String label,
    ResourceLocation dimension,
    BlockPos blockPos) {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String TAG_TARGET_ID = "TargetId";
  private static final String TAG_TYPE = "Type";
  private static final String TAG_KIND = "Kind";
  private static final String TAG_LABEL = "Label";
  private static final String TAG_DIMENSION = "Dimension";
  private static final String TAG_BLOCK_X = "BlockX";
  private static final String TAG_BLOCK_Y = "BlockY";
  private static final String TAG_BLOCK_Z = "BlockZ";
  private static final String TAG_HAS_BLOCK_POS = "HasBlockPos";

  public static InteractionDataEntry load(CompoundTag tag) {
    try {
      UUID targetId = tag.getUUID(TAG_TARGET_ID);
      InteractionType type = InteractionType.fromName(tag.getString(TAG_TYPE));
      TargetKind kind = TargetKind.fromName(tag.getString(TAG_KIND));
      if (type == null || kind == null) {
        log.warn(
            "Skipping interaction entry with invalid type/kind: {}/{}",
            tag.getString(TAG_TYPE),
            tag.getString(TAG_KIND));
        return null;
      }
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
      return new InteractionDataEntry(targetId, type, kind, label, dimension, blockPos);
    } catch (Exception e) {
      log.warn("Failed to load interaction entry: {}", e.getMessage());
      return null;
    }
  }

  public static InteractionDataEntry readFromBuf(FriendlyByteBuf buf) {
    UUID targetId = buf.readUUID();
    InteractionType type = buf.readEnum(InteractionType.class);
    TargetKind kind = buf.readEnum(TargetKind.class);
    String label = buf.readUtf();
    ResourceLocation dimension = buf.readResourceLocation();
    BlockPos blockPos = buf.readBoolean() ? buf.readBlockPos() : null;
    return new InteractionDataEntry(targetId, type, kind, label, dimension, blockPos);
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putUUID(TAG_TARGET_ID, targetId);
    tag.putString(TAG_TYPE, type.name());
    tag.putString(TAG_KIND, kind.name());
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
    return tag;
  }

  @Override
  public String toString() {
    String shortId = targetId.toString().substring(0, 8);
    if (blockPos != null) {
      return String.format(
          "[%s] %s '%s' at %s (ID: %s) in %s",
          kind, type, label, blockPos.toShortString(), shortId, dimension);
    }
    return String.format("[%s] %s '%s' (ID: %s) in %s", kind, type, label, shortId, dimension);
  }

  public void writeToBuf(FriendlyByteBuf buf) {
    buf.writeUUID(targetId);
    buf.writeEnum(type);
    buf.writeEnum(kind);
    buf.writeUtf(label);
    buf.writeResourceLocation(dimension);
    boolean hasPos = blockPos != null;
    buf.writeBoolean(hasPos);
    if (hasPos) {
      buf.writeBlockPos(blockPos);
    }
  }
}
