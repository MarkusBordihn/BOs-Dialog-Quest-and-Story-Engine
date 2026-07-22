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
import de.markusbordihn.dialogqueststoryengine.client.screen.ClientActionDiagnostics;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionConfigScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionSelectScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.MainScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.interaction.ActionDiagnostics;
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
    List<InteractionEntry> entries,
    UUID targetId,
    TargetKind targetKind,
    ResourceLocation dimension,
    BlockPos blockPos,
    List<String> diagnostics)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":interaction_list");

  public InteractionListMessage {
    entries = entries != null ? entries : Collections.emptyList();
    diagnostics = diagnostics != null ? diagnostics : Collections.emptyList();
  }

  public InteractionListMessage(
      List<InteractionEntry> entries,
      UUID targetId,
      TargetKind targetKind,
      ResourceLocation dimension,
      BlockPos blockPos) {
    this(entries, targetId, targetKind, dimension, blockPos, ActionDiagnostics.formatFor(targetId));
  }

  public static InteractionListMessage create(FriendlyByteBuf buffer) {
    int count = buffer.readInt();
    List<InteractionEntry> entries = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      entries.add(InteractionEntry.readFromBuffer(buffer));
    }
    UUID targetId = buffer.readUUID();
    TargetKind targetKind = buffer.readEnum(TargetKind.class);
    ResourceLocation dimension = buffer.readResourceLocation();
    BlockPos blockPos = buffer.readBoolean() ? buffer.readBlockPos() : null;
    int diagnosticCount = buffer.readInt();
    List<String> diagnostics = new ArrayList<>(diagnosticCount);
    for (int i = 0; i < diagnosticCount; i++) {
      diagnostics.add(buffer.readUtf());
    }
    return new InteractionListMessage(
        entries, targetId, targetKind, dimension, blockPos, diagnostics);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(this.entries.size());
    for (InteractionEntry entry : this.entries) {
      entry.writeToBuffer(buffer);
    }
    buffer.writeUUID(this.targetId);
    buffer.writeEnum(this.targetKind);
    buffer.writeResourceLocation(this.dimension);
    buffer.writeBoolean(this.blockPos != null);
    if (this.blockPos != null) {
      buffer.writeBlockPos(this.blockPos);
    }
    buffer.writeInt(this.diagnostics.size());
    for (String diagnostic : this.diagnostics) {
      buffer.writeUtf(diagnostic);
    }
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleClient() {
    ClientActionDiagnostics.set(this.targetId, this.diagnostics);
    InteractionEntry templateEntry =
        InteractionEntry.createTemplate(
            this.targetId, this.targetKind, this.blockPos, this.dimension);
    String targetLabel = InteractionConfigScreen.targetContextLabel(templateEntry);

    List<BreadcrumbBar.Segment> baseAncestors =
        List.of(
            new BreadcrumbBar.Segment("Home", MainScreen::open),
            new BreadcrumbBar.Segment("Interactions", InteractionOverviewScreen::open));

    Minecraft minecraft = Minecraft.getInstance();
    if (this.entries.isEmpty()) {
      minecraft.execute(
          () -> {
            if (minecraft.screen instanceof BaseScreen.ScreenWrapper) {
              return;
            }
            new InteractionConfigScreen(templateEntry, true, baseAncestors, targetLabel)
                .openScreen();
          });
    } else if (this.entries.size() == 1) {
      minecraft.execute(
          () -> {
            if (minecraft.screen instanceof BaseScreen.ScreenWrapper) {
              return;
            }
            new InteractionConfigScreen(this.entries.get(0), false, baseAncestors, targetLabel)
                .openScreen();
          });
    } else {
      InteractionSelectScreen.openWithEntries(
          this.entries,
          this.targetId,
          this.targetKind,
          this.dimension,
          this.blockPos,
          baseAncestors);
    }
  }
}
