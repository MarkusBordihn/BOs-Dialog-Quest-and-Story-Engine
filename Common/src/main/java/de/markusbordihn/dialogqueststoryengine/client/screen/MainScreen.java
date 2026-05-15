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
    {"Actions", "section.actions.desc"},
  };

  public MainScreen() {
    setBreadcrumb(List.of(), "Home");
    setScreenType(ScreenType.MAIN);
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
    setSizeCentered(480, 320);
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = getInnerWidth();
    int row = 0;

    addWidget(new Label(0, row + 2, SUBTITLE, 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    row += 16;

    int tileHeight = 46;
    GridLayout grid = GridLayout.of(0, row, innerWidth, 2, 8, 6);

    for (int i = 0; i < SECTIONS.length; i++) {
      String sectionLabel = SECTIONS[i][0];
      TextButton tile =
          new TextButton(0, 0, 0, tileHeight, sectionLabel, btn -> openSection(sectionLabel));
      grid.fill(tile, i % 2, i / 2, tileHeight);
      addWidget(tile);
      addWidget(
          new Label(
              tile.getX() + 4,
              tile.getY() + tileHeight - 12,
              SECTIONS[i][1],
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
    }
  }

  private void openSection(String section) {
    List<BreadcrumbBar.Segment> ancestors = buildChildAncestors("Home");
    switch (section) {
      case "Interactions" -> {
        InteractionOverviewScreen screen = new InteractionOverviewScreen(ancestors);
        screen.openScreen();
      }
      case "Dialogs" -> {
        DialogOverviewScreen screen = new DialogOverviewScreen(ancestors);
        screen.openScreen();
      }
      case "Quests" -> {
        QuestOverviewScreen screen = new QuestOverviewScreen(ancestors);
        screen.openScreen();
      }
      case "Tasks" -> {
        TaskOverviewScreen screen = new TaskOverviewScreen(ancestors);
        screen.openScreen();
      }
      case "Stories" -> {
        StoryOverviewScreen screen = new StoryOverviewScreen(ancestors);
        screen.openScreen();
      }
      case "Actions" -> {
        ActionOverviewScreen screen = new ActionOverviewScreen(ancestors);
        screen.openScreen();
      }
      default -> {
        /* no-op */
      }
    }
  }
}
