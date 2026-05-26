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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Separator;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextButton;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextInput;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.SaveInteractionMessage;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionEditorScreen extends BaseScreen {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private final InteractionEntry entry;
  private final ActionDataSet editableActionDataSet;
  private final Consumer<InteractionEntry> onSaveCallback;
  private ActionType selectedNewType = ActionType.OPEN_STORY;
  private Panel configPanel;

  private TextInput field1Input;
  private TextInput field2Input;
  private String editField1Prefill = null;
  private String editField2Prefill = null;

  public ActionEditorScreen(
      InteractionEntry entry,
      List<BreadcrumbBar.Segment> ancestors,
      Consumer<InteractionEntry> onSaveCallback) {
    this.entry = entry;
    this.editableActionDataSet = entry.actionDataSet().copy();
    this.onSaveCallback = onSaveCallback;
    setBreadcrumb(ancestors, "Actions");
    setScreenType(ScreenType.ACTIONS);
  }

  public ActionEditorScreen(InteractionEntry entry, List<BreadcrumbBar.Segment> ancestors) {
    this(entry, ancestors, null);
  }

  @Override
  protected Component getTitle() {
    String eventTypeName = entry.eventType().name();
    String shortType = eventTypeName.startsWith("ON_") ? eventTypeName.substring(3) : eventTypeName;
    return TextComponent.of("Action Editor - " + entry.label() + " (" + shortType + ")");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    setSizeCentered(360, 260);
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = getInnerWidth();
    int row = 0;
    int rowHeight = 22;

    for (ActionDataEntry existing : editableActionDataSet.entries()) {
      int capturedRow = row;
      String summary = formatActionSummary(existing);
      addWidget(
          new Label(
              0,
              capturedRow + 4,
              summary,
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
      addWidget(
          new TextButton(
              innerWidth - 104,
              capturedRow,
              52,
              16,
              "button.edit",
              btn -> {
                prefillFromEntry(existing);
                editableActionDataSet.remove(existing.id());
                refreshWidgets();
              }));
      addWidget(
          new TextButton(
              innerWidth - 50,
              capturedRow,
              48,
              16,
              "button.remove",
              btn -> {
                editableActionDataSet.remove(existing.id());
                refreshWidgets();
              }));
      row += rowHeight;
    }

    if (!editableActionDataSet.entries().isEmpty()) {
      addWidget(new Separator(0, row, innerWidth, true));
      row += 8;
    }

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
              selectedNewType = actionType;
              field1Input = null;
              field2Input = null;
              refreshConfigPanel();
              refreshWidgets();
            },
            this::openOverlay,
            this::closeOverlay);
    actionSelect.selectByValue(selectedNewType);
    addWidget(actionSelect);
    row += rowHeight + 4;

    configPanel =
        new Panel(0, row, innerWidth, 46) {
          @Override
          protected void renderBackground(
              GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ColorPalette palette = ColorPalette.current();
            int panelX = getX();
            int panelY = getY();
            graphics.fill(
                panelX, panelY, panelX + getWidth(), panelY + getHeight(), palette.background());
          }

          @Override
          protected void addWidgets() {
            field1Input = null;
            field2Input = null;
            populateConfigPanel(this);
            applyPrefills();
          }
        };
    configPanel.setParent(this);
    populateConfigPanel(configPanel);
    addWidget(configPanel);
    row += 50;

    int addBtnWidth = 60;
    addWidget(
        new TextButton(
            (innerWidth - addBtnWidth) / 2,
            row,
            addBtnWidth,
            16,
            "button.add",
            btn -> addCurrentAction()));
    row += 24;

    addWidget(new Separator(0, row, innerWidth, true));
    row += 8;

    int buttonWidth = 70;
    int btnSpacing = 8;
    int buttonStartX = (innerWidth - (buttonWidth * 2 + btnSpacing)) / 2;
    addWidget(
        new TextButton(buttonStartX, row, buttonWidth, 20, "button.save", btn -> saveAndClose()));
    addWidget(
        new TextButton(
            buttonStartX + buttonWidth + btnSpacing,
            row,
            buttonWidth,
            20,
            "button.cancel",
            btn -> closeScreen()));
  }

  private void refreshConfigPanel() {
    if (configPanel != null) {
      configPanel.clearWidgets();
      field1Input = null;
      field2Input = null;
      populateConfigPanel(configPanel);
    }
  }

  private void populateConfigPanel(Panel panel) {
    int y = 4;
    int panelWidth = panel.getWidth();
    int fieldWidth = panelWidth - 90;
    switch (selectedNewType) {
      case OPEN_STORY -> {
        panel.addWidget(
            new Label(4, y + 3, "field.story_id", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        field1Input = new TextInput(86, y, fieldWidth, 14, val -> {});
        field1Input.setSuggestion("dqse:my_story");
        field1Input.setMaxLength(200);
        panel.addWidget(field1Input);
        y += 18;
        panel.addWidget(
            new Label(
                4, y + 3, "field.theme_override", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        field2Input = new TextInput(86, y, fieldWidth, 14, val -> {});
        field2Input.setSuggestion("(optional)");
        field2Input.setMaxLength(200);
        panel.addWidget(field2Input);
      }
      case OPEN_INTERACTIVE_STORY -> {
        panel.addWidget(
            new Label(4, y + 3, "field.story_id", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        field1Input = new TextInput(86, y, fieldWidth, 14, val -> {});
        field1Input.setSuggestion("dqse:my_interactive_story");
        field1Input.setMaxLength(200);
        panel.addWidget(field1Input);
      }
      case RUN_COMMAND -> {
        panel.addWidget(
            new Label(4, y + 3, "field.command", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        field1Input = new TextInput(86, y, fieldWidth, 14, val -> {});
        field1Input.setSuggestion("/say hello");
        field1Input.setMaxLength(256);
        panel.addWidget(field1Input);
      }
      case SET_FACT -> {
        panel.addWidget(
            new Label(4, y + 3, "field.fact_id", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        field1Input = new TextInput(86, y, fieldWidth, 14, val -> {});
        field1Input.setSuggestion("dqse:my_fact");
        field1Input.setMaxLength(200);
        panel.addWidget(field1Input);
        y += 18;
        panel.addWidget(
            new Label(
                4, y + 3, "field.fact_value", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
        field2Input = new TextInput(86, y, fieldWidth, 14, val -> {});
        field2Input.setSuggestion("true");
        field2Input.setMaxLength(128);
        panel.addWidget(field2Input);
      }
      default -> {
        panel.addWidget(
            new Label(4, y, "action.default.hint", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
      }
    }
  }

  private void addCurrentAction() {
    String value1 = field1Input != null ? field1Input.getValue().trim() : "";
    String value2 = field2Input != null ? field2Input.getValue().trim() : "";
    switch (selectedNewType) {
      case OPEN_STORY -> {
        ResourceLocation storyId = ResourceLocation.tryParse(value1);
        if (storyId == null) {
          log.warn("{} ActionEditorScreen: invalid storyId '{}'", Constants.LOG_PREFIX, value1);
          return;
        }
        ResourceLocation themeOverrideId =
            value2.isEmpty() ? null : ResourceLocation.tryParse(value2);
        editableActionDataSet.add(ActionDataEntry.openStory(storyId, themeOverrideId));
      }
      case OPEN_INTERACTIVE_STORY -> {
        ResourceLocation storyId = ResourceLocation.tryParse(value1);
        if (storyId == null) {
          return;
        }
        editableActionDataSet.add(ActionDataEntry.openInteractiveStory(storyId));
      }
      case RUN_COMMAND -> {
        if (!value1.isEmpty()) {
          editableActionDataSet.add(ActionDataEntry.runCommand(value1));
        }
      }
      case SET_FACT -> {
        ResourceLocation factId = ResourceLocation.tryParse(value1);
        if (factId == null || value2.isEmpty()) {
          return;
        }
        editableActionDataSet.add(ActionDataEntry.setFact(factId, value2));
      }
      default -> log.warn(
          "{} ActionEditorScreen: unsupported action type {}", Constants.LOG_PREFIX, selectedNewType);
    }
    refreshWidgets();
  }

  private void saveAndClose() {
    InteractionEntry updatedEntry =
        entry.withEdits(entry.interactionType(), entry.label(), editableActionDataSet);
    NetworkHandlerManager.sendToServer(new SaveInteractionMessage(updatedEntry));
    if (this.onSaveCallback != null) {
      this.onSaveCallback.accept(updatedEntry);
    }
    closeScreen();
  }

  private String formatActionSummary(ActionDataEntry action) {
    String typeName = formatActionName(action.type());
    return switch (action.type()) {
      case OPEN_STORY -> typeName + ": " + action.storyId();
      case OPEN_INTERACTIVE_STORY -> typeName + ": " + action.storyId();
      case RUN_COMMAND -> typeName + ": " + action.command();
      case SET_FACT -> typeName + ": " + action.factId() + " = " + action.factValue();
      default -> typeName;
    };
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

  private void prefillFromEntry(ActionDataEntry action) {
    selectedNewType = action.type();
    switch (action.type()) {
      case OPEN_STORY -> {
        editField1Prefill = action.storyId() != null ? action.storyId().toString() : "";
        editField2Prefill =
            action.themeOverrideId() != null ? action.themeOverrideId().toString() : "";
      }
      case OPEN_INTERACTIVE_STORY ->
          editField1Prefill = action.storyId() != null ? action.storyId().toString() : "";
      case RUN_COMMAND -> editField1Prefill = action.command();
      case SET_FACT -> {
        editField1Prefill = action.factId() != null ? action.factId().toString() : "";
        editField2Prefill = action.factValue();
      }
      default -> {}
    }
  }

  private void applyPrefills() {
    if (editField1Prefill != null && field1Input != null) {
      field1Input.setValue(editField1Prefill);
      editField1Prefill = null;
    }
    if (editField2Prefill != null && field2Input != null) {
      field2Input.setValue(editField2Prefill);
      editField2Prefill = null;
    }
  }
}
