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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Panel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Label;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.SelectBox;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.SelectOption;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionEditorScreen extends BaseScreen {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private final InteractionEntry entry;
  private ActionType selectedAction = ActionType.NONE;
  private Panel configPanel;

  public ActionEditorScreen(InteractionEntry entry, List<BreadcrumbBar.Segment> ancestors) {
    this.entry = entry;
    setBreadcrumb(ancestors, "Actions");
    setScreenType(ScreenType.ACTIONS);
  }

  @Override
  protected Component getTitle() {
    return TextComponent.of("Action Editor - " + entry.label());
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    setSizeCentered(360, 240);
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = getInnerWidth();
    int row = 0;
    int rowHeight = 22;

    addWidget(
        new Label(
            0, row + 4, "field.action_type", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    List<SelectOption<ActionType>> actionOptions =
        Arrays.stream(ActionType.values())
            .filter(t -> t != ActionType.NONE)
            .map(t -> SelectOption.of(formatActionName(t), t))
            .collect(Collectors.toList());
    SelectBox<ActionType> actionSelect =
        new SelectBox<>(
            80,
            row,
            Math.min(innerWidth - 80, 160),
            16,
            actionOptions,
            actionType -> {
              selectedAction = actionType;
              refreshConfigPanel();
              refreshWidgets();
            },
            this::openOverlay,
            this::closeOverlay);
    actionSelect.selectByValue(selectedAction);
    addWidget(actionSelect);
    row += rowHeight + 4;

    row += 4;

    configPanel =
        new Panel(0, row, innerWidth, getInnerHeight() - row - 4) {
          @Override
          protected void renderBackground(
              GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ColorPalette palette = ColorPalette.current();
            int x = getX();
            int y = getY();
            graphics.fill(x, y, x + getWidth(), y + getHeight(), palette.background());
          }
        };
    configPanel.setParent(this);
    populateConfigPanel(configPanel);
    addWidget(configPanel);
  }

  private void refreshConfigPanel() {
    if (configPanel != null) {
      configPanel.clearWidgets();
      populateConfigPanel(configPanel);
    }
  }

  private void populateConfigPanel(Panel panel) {
    int y = 4;
    switch (selectedAction) {
      case START_DIALOG -> {
        panel.addWidget(
            new Label(
                4, y, "action.start_dialog.hint", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        y += 14;
        panel.addWidget(
            new Label(
                4,
                y,
                "action.start_dialog.hint2",
                0,
                ScaledText.SCALE_SMALL,
                Label.Alignment.LEFT));
      }
      case GIVE_QUEST -> {
        panel.addWidget(
            new Label(
                4, y, "action.give_quest.hint", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        y += 14;
        panel.addWidget(
            new Label(
                4, y, "action.give_quest.hint2", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
      }
      case TRIGGER_EVENT -> {
        panel.addWidget(
            new Label(
                4,
                y,
                "action.trigger_event.hint",
                0,
                ScaledText.SCALE_SMALL,
                Label.Alignment.LEFT));
        y += 14;
        panel.addWidget(
            new Label(
                4,
                y,
                "action.trigger_event.hint2",
                0,
                ScaledText.SCALE_SMALL,
                Label.Alignment.LEFT));
      }
      case RUN_COMMAND -> {
        panel.addWidget(
            new Label(
                4, y, "action.run_command.hint", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        y += 14;
        panel.addWidget(
            new Label(
                4, y, "action.run_command.hint2", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
      }
      default -> {
        panel.addWidget(
            new Label(
                4, y, "action.default.hint", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
      }
    }
  }

  private String formatActionName(ActionType type) {
    StringBuilder builder = new StringBuilder();
    boolean capitalize = true;
    for (char c : type.name().replace('_', ' ').toCharArray()) {
      if (c == ' ') {
        builder.append(' ');
        capitalize = true;
      } else if (capitalize) {
        builder.append(Character.toUpperCase(c));
        capitalize = false;
      } else {
        builder.append(Character.toLowerCase(c));
      }
    }
    return builder.toString();
  }
}
