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

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.InteractionClientData;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Label;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ListPanel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Separator;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextInput;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractionOverviewScreen extends BaseScreen {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ListPanel<InteractionDataEntry> listPanel;
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
    int row = 0;

    float headerScale = ScaledText.SCALE_SMALL;
    addWidget(new Label(0, row, "column.label", 0, headerScale, Label.Alignment.LEFT));
    addWidget(new Label(innerWidth / 4, row, "column.type", 0, headerScale, Label.Alignment.LEFT));
    addWidget(
        new Label(innerWidth / 4 * 2, row, "column.target", 0, headerScale, Label.Alignment.LEFT));
    addWidget(
        new Label(
            innerWidth / 4 * 3, row, "column.position", 0, headerScale, Label.Alignment.LEFT));
    row += 12;
    addWidget(new Separator(0, row, innerWidth, true));
    row += 2;

    int searchBarY = innerHeight - searchHeight;
    int listHeight = searchBarY - row - 4;
    listPanel = new ListPanel<>(0, row, innerWidth, listHeight);
    listPanel.setEntryHeight(20);
    listPanel.setEntryRenderer(this::renderEntry);
    listPanel.setOnSelect(
        entry -> {
          List<BreadcrumbBar.Segment> childAncestors = buildChildAncestors("Interactions");
          String targetLabel = InteractionConfigScreen.targetContextLabel(entry);
          InteractionConfigScreen configScreen =
              new InteractionConfigScreen(entry, false, childAncestors, targetLabel);
          configScreen.openScreen();
        });
    addWidget(listPanel);
    updateListItems();

    addWidget(
        new Label(0, searchBarY + 4, "search", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    searchInput =
        new TextInput(
            50,
            searchBarY,
            innerWidth - 50,
            searchHeight,
            value -> {
              searchFilter = value;
              updateListItems();
            });
    searchInput.setValue(searchFilter);
    addWidget(searchInput);
  }

  private void updateListItems() {
    List<InteractionDataEntry> all = InteractionClientData.getEntries();
    if (searchFilter == null || searchFilter.isEmpty()) {
      listPanel.setItems(all);
    } else {
      String filter = searchFilter.toLowerCase(Locale.ROOT);
      List<InteractionDataEntry> filtered = new ArrayList<>();
      for (InteractionDataEntry entry : all) {
        if (entry.label().toLowerCase(Locale.ROOT).contains(filter)
            || entry.type().name().toLowerCase(Locale.ROOT).contains(filter)
            || entry.kind().name().toLowerCase(Locale.ROOT).contains(filter)) {
          filtered.add(entry);
        }
      }
      listPanel.setItems(filtered);
    }
  }

  private void renderEntry(
      GuiGraphics graphics,
      Font font,
      InteractionDataEntry entry,
      int index,
      int x,
      int y,
      int width,
      int height,
      ColorPalette palette) {
    int columnWidth = width / 4;
    float scale = ScaledText.SCALE_SMALL;

    ScaledText.draw(graphics, font, entry.label(), x, y + 3, palette.onSurface(), scale);
    ScaledText.draw(
        graphics, font, entry.type().name(), x + columnWidth, y + 3, palette.onSurfaceLow(), scale);
    ScaledText.draw(
        graphics,
        font,
        entry.kind().name(),
        x + columnWidth * 2,
        y + 3,
        palette.onSurfaceLow(),
        scale);

    String posStr = entry.blockPos() != null ? entry.blockPos().toShortString() : "N/A";
    ScaledText.draw(
        graphics, font, posStr, x + columnWidth * 3, y + 3, palette.onSurfaceLow(), scale);
  }
}
