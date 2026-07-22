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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Label;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextButton;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.layout.GridLayout;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class MainScreen extends BaseScreen {

  private static final String SUBTITLE = "subtitle";

  private static final String[][] SECTIONS = {
    {"Interactions", "section.interactions.desc"},
    {"Dialogs", "section.dialogs.desc"},
    {"Quests", "section.quests.desc"},
    {"Tasks", "section.tasks.desc"},
    {"Stories", "section.stories.desc"},
    {"Themes", "section.themes.desc"},
    {"Actions", "section.actions.desc"},
  };

  public MainScreen() {
    this.setBreadcrumb(List.of(), "Home");
    this.setScreenType(ScreenType.MAIN);
  }

  public static void open() {
    Minecraft.getInstance()
        .execute(
            () -> {
              MainScreen screen = new MainScreen();
              screen.openScreen();
            });
  }

  @Override
  protected Component getTitle() {
    return TextComponent.of(Constants.MOD_NAME);
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    this.setSizeCentered(480, 360);
    this.refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = this.getInnerWidth();
    int row = 0;

    this.addWidget(
        new Label(0, row + 2, SUBTITLE, 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    row += 16;

    int rowGap = 6;
    int maxRows = (int) Math.ceil(SECTIONS.length / 2.0);
    int tileHeight = Math.min(40, (this.getInnerHeight() - row - (maxRows - 1) * rowGap) / maxRows);
    GridLayout grid = GridLayout.of(0, row, innerWidth, 2, 8, rowGap);

    for (int i = 0; i < SECTIONS.length; i++) {
      int column = i % 2;
      int gridRow = i / 2;
      String sectionLabel = SECTIONS[i][0];
      TextButton tile =
          new TextButton(
              0, 0, 0, tileHeight, sectionLabel, button -> this.openSection(sectionLabel));
      grid.fill(tile, column, gridRow, tileHeight);
      this.addWidget(tile);
      this.addWidget(
          new Label(
              grid.getX(column) + 4,
              grid.getY(gridRow, tileHeight) + tileHeight - 12,
              SECTIONS[i][1],
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
    }
  }

  private void openSection(String section) {
    List<BreadcrumbBar.Segment> ancestors = this.buildChildAncestors("Home");
    switch (section) {
      case "Interactions" -> new InteractionOverviewScreen(ancestors).openScreen();
      case "Dialogs" -> new DialogOverviewScreen(ancestors).openScreen();
      case "Quests" -> new QuestOverviewScreen(ancestors).openScreen();
      case "Tasks" -> new TaskOverviewScreen(ancestors).openScreen();
      case "Stories" -> new StoryOverviewScreen(ancestors).openScreen();
      case "Themes" -> new ThemeOverviewScreen(ancestors).openScreen();
      case "Actions" -> new ActionOverviewScreen(ancestors).openScreen();
      default -> {}
    }
  }
}
