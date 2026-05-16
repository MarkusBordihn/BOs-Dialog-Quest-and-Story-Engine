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

package de.markusbordihn.dialogqueststoryengine.client.screen;

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ListPanel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextButton;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import java.util.List;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public class InteractionSelectScreen extends BaseScreen {

  private final List<InteractionEntry> entries;
  private final UUID targetId;
  private final TargetKind targetKind;
  private final ResourceLocation dimension;
  private final BlockPos blockPos;
  private final String targetLabel;

  public InteractionSelectScreen(
      List<InteractionEntry> entries,
      UUID targetId,
      TargetKind targetKind,
      ResourceLocation dimension,
      BlockPos blockPos,
      List<BreadcrumbBar.Segment> ancestors) {
    this.entries = entries;
    this.targetId = targetId;
    this.targetKind = targetKind;
    this.dimension = dimension;
    this.blockPos = blockPos;
    this.targetLabel =
        blockPos != null
            ? targetKind.name() + " (" + blockPos.toShortString() + ")"
            : targetKind.name();
    setBreadcrumb(ancestors, this.targetLabel);
  }

  public static void openWithEntries(
      List<InteractionEntry> entries,
      UUID targetId,
      TargetKind targetKind,
      ResourceLocation dimension,
      BlockPos blockPos,
      List<BreadcrumbBar.Segment> ancestors) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.execute(
        () -> {
          if (minecraft.screen instanceof BaseScreen.ScreenWrapper) {
            return;
          }
          InteractionSelectScreen screen =
              new InteractionSelectScreen(
                  entries, targetId, targetKind, dimension, blockPos, ancestors);
          screen.openScreen();
        });
  }

  @Override
  protected Component getTitle() {
    return TextComponent.ofKey("screen.dialog_quest_and_story_engine.interaction_select");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    setSizeCentered(260, Math.min(entries.size() * 22 + 70, 220));
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = getInnerWidth();
    int row = 0;

    int listHeight = getInnerHeight() - 28;
    ListPanel<InteractionEntry> listPanel = new ListPanel<>(0, row, innerWidth, listHeight);
    listPanel.setEntryHeight(20);
    listPanel.setEntryRenderer(this::renderEntry);
    listPanel.setOnSelect(
        entry -> {
          List<BreadcrumbBar.Segment> childAncestors = buildChildAncestors(targetLabel);
          InteractionConfigScreen configScreen =
              new InteractionConfigScreen(entry, false, childAncestors);
          configScreen.openScreen();
        });
    listPanel.setItems(entries);
    addWidget(listPanel);
    row += listHeight + 4;

    int buttonWidth = 120;
    addWidget(
        new TextButton(
            (innerWidth - buttonWidth) / 2,
            row,
            buttonWidth,
            20,
            "button.new_interaction",
            btn -> {
              InteractionEntry template =
                  InteractionEntry.createTemplate(targetId, targetKind, blockPos, dimension);
              List<BreadcrumbBar.Segment> childAncestors = buildChildAncestors(targetLabel);
              InteractionConfigScreen configScreen =
                  new InteractionConfigScreen(template, true, childAncestors);
              configScreen.openScreen();
            }));
  }

  private void renderEntry(
      GuiGraphics graphics,
      Font font,
      InteractionEntry entry,
      int index,
      int x,
      int y,
      int width,
      int height,
      ColorPalette palette) {
    float scale = ScaledText.SCALE_SMALL;
    String typeStr = entry.interactionType().name();
    String labelStr = entry.label().isEmpty() ? "(no label)" : entry.label();

    ScaledText.draw(graphics, font, typeStr, x, y + 3, palette.onSurface(), scale);
    ScaledText.draw(
        graphics,
        font,
        " - " + labelStr,
        x + ScaledText.getScaledWidth(font, typeStr, scale),
        y + 3,
        palette.onSurface(),
        scale);
  }
}
