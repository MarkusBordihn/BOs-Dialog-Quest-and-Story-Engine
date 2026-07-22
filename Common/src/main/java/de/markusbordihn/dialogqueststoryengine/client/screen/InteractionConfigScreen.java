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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.SelectBox;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.SelectOption;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Separator;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextButton;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextInput;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.RemoveInteractionMessage;
import de.markusbordihn.dialogqueststoryengine.network.message.SaveInteractionMessage;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractionConfigScreen extends BaseScreen {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private final boolean isNew;
  private InteractionEntry entry;
  private String editLabel;
  private InteractionType editType;

  private TextInput labelInput;
  private SelectBox<InteractionType> typeSelect;
  private TextButton saveButton;
  private TextButton cancelButton;

  public InteractionConfigScreen(
      InteractionEntry entry,
      boolean isNew,
      List<BreadcrumbBar.Segment> ancestors,
      String currentLabel) {
    this.entry = entry;
    this.isNew = isNew;
    this.editLabel = entry.label();
    this.editType = entry.interactionType();
    this.setBreadcrumb(ancestors, currentLabel);
  }

  public InteractionConfigScreen(
      InteractionEntry entry, boolean isNew, List<BreadcrumbBar.Segment> ancestors) {
    this(entry, isNew, ancestors, targetContextLabel(entry));
  }

  public InteractionConfigScreen(InteractionEntry entry, boolean isNew) {
    this(entry, isNew, List.of());
  }

  public static String targetContextLabel(InteractionEntry entry) {
    if (entry.targetKind() == TargetKind.ENTITY) {
      String shortId = entry.targetId() != null ? entry.targetId().toString().substring(0, 8) : "?";
      return "Entity (" + shortId + ")";
    }

    String kindName = entry.targetKind() == TargetKind.BLOCK_ENTITY ? "Block Entity" : "Block";
    return entry.blockPos() != null
        ? kindName + " (" + entry.blockPos().toShortString() + ")"
        : kindName;
  }

  public static void openWithData(InteractionEntry entry, boolean isNew) {
    openWithData(entry, isNew, List.of());
  }

  public static void openWithData(
      InteractionEntry entry, boolean isNew, List<BreadcrumbBar.Segment> ancestors) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.execute(
        () -> {
          if (minecraft.screen instanceof BaseScreen.ScreenWrapper) {
            return;
          }
          InteractionConfigScreen screen = new InteractionConfigScreen(entry, isNew, ancestors);
          screen.openScreen();
        });
  }

  @Override
  protected Component getTitle() {
    return TextComponent.ofKey("screen.dialog_quest_and_story_engine.interaction_config");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    this.setSizeCentered(320, 220);
    this.refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = this.getInnerWidth();
    int labelColumnX = 0;
    int fieldColumnX = 70;
    int fieldWidth = innerWidth - fieldColumnX - 10;
    int row = 0;
    int rowHeight = 22;

    this.addWidget(
        new Label(
            labelColumnX, row + 4, "field.label", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    this.labelInput =
        new TextInput(
            fieldColumnX,
            row,
            fieldWidth,
            16,
            value -> {
              this.editLabel = value;
              this.updateButtonStates();
            });
    this.labelInput.setValue(this.editLabel);
    this.labelInput.setMaxLength(128);
    this.addWidget(this.labelInput);
    row += rowHeight;

    List<SelectOption<InteractionType>> typeOptions =
        Arrays.stream(InteractionType.values())
            .map(interactionType -> SelectOption.of(interactionType.name(), interactionType))
            .collect(Collectors.toList());
    this.addWidget(
        new Label(
            labelColumnX, row + 4, "field.type", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    this.typeSelect =
        new SelectBox<>(
            fieldColumnX,
            row,
            Math.min(fieldWidth, 120),
            16,
            typeOptions,
            type -> {
              this.editType = type;
              this.updateButtonStates();
            },
            this::openOverlay,
            this::closeOverlay);
    this.typeSelect.selectByValue(this.editType);
    this.addWidget(this.typeSelect);
    row += rowHeight;

    this.addWidget(
        new Label(
            labelColumnX,
            row + 4,
            "field.target",
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    this.addWidget(
        new Label(
            fieldColumnX,
            row + 4,
            this.entry.targetKind().name(),
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    row += rowHeight;

    if (this.entry.blockPos() != null) {
      this.addWidget(
          new Label(
              labelColumnX,
              row + 4,
              "field.position",
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
      this.addWidget(
          new Label(
              fieldColumnX,
              row + 4,
              this.entry.blockPos().toShortString(),
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
      row += rowHeight;
    }

    this.addWidget(
        new Label(
            labelColumnX,
            row + 4,
            "field.dimension",
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    this.addWidget(
        new Label(
            fieldColumnX,
            row + 4,
            this.entry.dimension().toString(),
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    row += rowHeight;

    this.addWidget(new Separator(0, row, innerWidth, true));
    row += 6;

    TextButton actionsButton =
        new TextButton(
            labelColumnX,
            row,
            Math.min(innerWidth, 160),
            20,
            "button.edit_actions",
            button -> {
              List<BreadcrumbBar.Segment> childAncestors = this.buildChildAncestors();
              ActionEditorScreen editorScreen =
                  new ActionEditorScreen(
                      this.entry, childAncestors, updatedEntry -> this.entry = updatedEntry);
              editorScreen.openScreen();
            });
    this.addWidget(actionsButton);
    row += 28;

    int buttonWidth = 70;
    int buttonSpacing = 8;
    int buttonCount = this.isNew ? 2 : 3;
    int totalButtonWidth = buttonWidth * buttonCount + buttonSpacing * (buttonCount - 1);
    int buttonStartX = (innerWidth - totalButtonWidth) / 2;

    this.saveButton =
        new TextButton(
            buttonStartX, row, buttonWidth, 20, "button.save", button -> this.saveAndClose());
    this.addWidget(this.saveButton);
    this.cancelButton =
        new TextButton(
            buttonStartX + buttonWidth + buttonSpacing,
            row,
            buttonWidth,
            20,
            "button.cancel",
            button -> this.closeScreen());
    this.addWidget(this.cancelButton);

    if (!this.isNew) {
      TextButton removeButton =
          new TextButton(
              buttonStartX + (buttonWidth + buttonSpacing) * 2,
              row,
              buttonWidth,
              20,
              "button.remove",
              button -> {
                NetworkHandlerManager.sendToServer(new RemoveInteractionMessage(this.entry));
                this.closeScreen();
              });
      this.addWidget(removeButton);
    }
    this.updateButtonStates();
  }

  private void saveAndClose() {
    if (!this.isNew && !this.hasUnsavedChanges()) {
      return;
    }

    String label = this.labelInput.getValue();
    if (label.isEmpty()) {
      label = this.editLabel;
    }
    InteractionEntry updatedEntry = this.entry.withEdits(this.editType, label);
    NetworkHandlerManager.sendToServer(new SaveInteractionMessage(updatedEntry));
    this.closeScreen();
  }

  private boolean hasUnsavedChanges() {
    String label = this.labelInput != null ? this.labelInput.getValue() : this.editLabel;
    return this.isNew
        || !this.entry.label().equals(label)
        || this.entry.interactionType() != this.editType;
  }

  private void updateButtonStates() {
    boolean hasUnsavedChanges = this.hasUnsavedChanges();
    if (this.saveButton != null) {
      this.saveButton.setActive(hasUnsavedChanges);
    }
    if (this.cancelButton != null) {
      this.cancelButton.setActive(hasUnsavedChanges);
    }
  }

  @Override
  public void tick() {
    super.tick();
    this.updateButtonStates();
  }
}
