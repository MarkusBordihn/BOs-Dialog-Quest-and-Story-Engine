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

import de.markusbordihn.dialogqueststoryengine.client.InteractionClientData;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ColumnListPanel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Label;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextInput;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class InteractionOverviewScreen extends BaseScreen {

  private ColumnListPanel<InteractionEntry> table;
  private TextInput searchInput;
  private String searchFilter = "";

  public InteractionOverviewScreen() {
    setBreadcrumb(List.of(new BreadcrumbBar.Segment("Home", MainScreen::open)), "Interactions");
  }

  public InteractionOverviewScreen(List<BreadcrumbBar.Segment> ancestors) {
    setBreadcrumb(ancestors, "Interactions");
  }

  public static void open() {
    Minecraft.getInstance()
        .execute(
            () -> {
              InteractionOverviewScreen screen = new InteractionOverviewScreen();
              screen.openScreen();
            });
  }

  public static void open(List<BreadcrumbBar.Segment> ancestors) {
    Minecraft.getInstance()
        .execute(
            () -> {
              InteractionOverviewScreen screen = new InteractionOverviewScreen(ancestors);
              screen.openScreen();
            });
  }

  @Override
  protected Component getTitle() {
    return TextComponent.ofKey("screen.dialog_quest_and_story_engine.interaction_overview");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    setSizeCentered(400, 300);
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = getInnerWidth();
    int innerHeight = getInnerHeight();
    int searchHeight = 16;
    int searchBarY = innerHeight - searchHeight;

    this.table = new ColumnListPanel<>(0, 0, innerWidth, searchBarY - 4);
    this.table.addColumn("column.label", 0.25f);
    this.table.addColumn("column.type", 0.25f);
    this.table.addColumn("column.target", 0.25f);
    this.table.addColumn("column.position", 0.25f);
    this.table.setEntryHeight(20);
    this.table.setEntryRenderer(this::renderEntry);
    this.table.setOnSelect(
        entry -> {
          List<BreadcrumbBar.Segment> childAncestors = buildChildAncestors("Interactions");
          String targetLabel = InteractionConfigScreen.targetContextLabel(entry);
          InteractionConfigScreen configScreen =
              new InteractionConfigScreen(entry, false, childAncestors, targetLabel);
          configScreen.openScreen();
        });
    addWidget(this.table);
    updateListItems();

    addWidget(
        new Label(0, searchBarY + 4, "search", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    this.searchInput =
        new TextInput(
            50,
            searchBarY,
            innerWidth - 50,
            searchHeight,
            value -> {
              this.searchFilter = value;
              updateListItems();
            });
    this.searchInput.setValue(this.searchFilter);
    addWidget(this.searchInput);
  }

  private void updateListItems() {
    List<InteractionEntry> all = InteractionClientData.getEntries();
    if (this.searchFilter.isEmpty()) {
      this.table.setItems(all);
      return;
    }

    String filter = this.searchFilter.toLowerCase(Locale.ROOT);
    this.table.setItems(
        all.stream()
            .filter(
                entry ->
                    entry.label().toLowerCase(Locale.ROOT).contains(filter)
                        || entry.interactionType().name().toLowerCase(Locale.ROOT).contains(filter)
                        || entry.targetKind().name().toLowerCase(Locale.ROOT).contains(filter))
            .toList());
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
      ColorPalette palette,
      int[] columnOffsets) {
    float scale = ScaledText.SCALE_SMALL;
    String posStr = entry.blockPos() != null ? entry.blockPos().toShortString() : "N/A";

    ScaledText.draw(
        graphics, font, entry.label(), x + columnOffsets[0], y + 3, palette.onSurface(), scale);
    ScaledText.draw(
        graphics,
        font,
        entry.interactionType().name(),
        x + columnOffsets[1],
        y + 3,
        palette.onSurfaceLow(),
        scale);
    ScaledText.draw(
        graphics,
        font,
        entry.targetKind().name(),
        x + columnOffsets[2],
        y + 3,
        palette.onSurfaceLow(),
        scale);
    ScaledText.draw(
        graphics, font, posStr, x + columnOffsets[3], y + 3, palette.onSurfaceLow(), scale);
  }
}
