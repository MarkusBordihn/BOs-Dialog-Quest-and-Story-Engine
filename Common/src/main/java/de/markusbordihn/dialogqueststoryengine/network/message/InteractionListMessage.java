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

package de.markusbordihn.dialogqueststoryengine.network.message;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionConfigScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionSelectScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.MainScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record InteractionListMessage(
    List<InteractionDataEntry> entries,
    UUID targetId,
    TargetKind targetKind,
    ResourceLocation dimension,
    BlockPos blockPos)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":interaction_list");

  public InteractionListMessage(
      List<InteractionDataEntry> entries,
      UUID targetId,
      TargetKind targetKind,
      ResourceLocation dimension,
      BlockPos blockPos) {
    this.entries = entries != null ? entries : Collections.emptyList();
    this.targetId = targetId;
    this.targetKind = targetKind;
    this.dimension = dimension;
    this.blockPos = blockPos;
  }

  public static InteractionListMessage create(FriendlyByteBuf buffer) {
    int count = buffer.readInt();
    List<InteractionDataEntry> entries = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      entries.add(InteractionDataEntry.readFromBuf(buffer));
    }
    UUID targetId = buffer.readUUID();
    TargetKind targetKind = buffer.readEnum(TargetKind.class);
    ResourceLocation dimension = buffer.readResourceLocation();
    BlockPos blockPos = buffer.readBoolean() ? buffer.readBlockPos() : null;
    return new InteractionListMessage(entries, targetId, targetKind, dimension, blockPos);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(entries.size());
    for (InteractionDataEntry entry : entries) {
      entry.writeToBuf(buffer);
    }
    buffer.writeUUID(targetId);
    buffer.writeEnum(targetKind);
    buffer.writeResourceLocation(dimension);
    buffer.writeBoolean(blockPos != null);
    if (blockPos != null) {
      buffer.writeBlockPos(blockPos);
    }
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleClient() {
    // Wand always navigates: Home > Interactions > TARGET
    String targetLabel =
        InteractionConfigScreen.targetContextLabel(
            new InteractionDataEntry(
                targetId, InteractionType.RIGHT_CLICK, targetKind, "", dimension, blockPos));

    List<BreadcrumbBar.Segment> baseAncestors =
        List.of(
            new BreadcrumbBar.Segment("Home", MainScreen::open),
            new BreadcrumbBar.Segment("Interactions", InteractionOverviewScreen::open));

    Minecraft minecraft = Minecraft.getInstance();
    if (entries.isEmpty()) {
      InteractionDataEntry template =
          new InteractionDataEntry(
              targetId, InteractionType.RIGHT_CLICK, targetKind, "", dimension, blockPos);
      minecraft.execute(
          () -> {
            if (minecraft.screen instanceof BaseScreen.ScreenWrapper) {
              return;
            }
            new InteractionConfigScreen(template, true, baseAncestors, targetLabel).openScreen();
          });
    } else if (entries.size() == 1) {
      minecraft.execute(
          () -> {
            if (minecraft.screen instanceof BaseScreen.ScreenWrapper) {
              return;
            }
            new InteractionConfigScreen(entries.get(0), false, baseAncestors, targetLabel)
                .openScreen();
          });
    } else {
      InteractionSelectScreen.openWithEntries(
          entries, targetId, targetKind, dimension, blockPos, baseAncestors);
    }
  }
}
