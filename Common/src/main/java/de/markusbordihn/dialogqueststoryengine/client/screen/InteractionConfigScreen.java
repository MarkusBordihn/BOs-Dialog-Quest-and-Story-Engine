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
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
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
  private InteractionDataEntry entry;
  private String editLabel;
  private InteractionType editType;

  private TextInput labelInput;
  private SelectBox<InteractionType> typeSelect;

  public InteractionConfigScreen(
      InteractionDataEntry entry,
      boolean isNew,
      List<BreadcrumbBar.Segment> ancestors,
      String currentLabel) {
    this.entry = entry;
    this.isNew = isNew;
    this.editLabel = entry.label();
    this.editType = entry.type();
    setBreadcrumb(ancestors, currentLabel);
  }

  public InteractionConfigScreen(
      InteractionDataEntry entry, boolean isNew, List<BreadcrumbBar.Segment> ancestors) {
    this(entry, isNew, ancestors, targetContextLabel(entry));
  }

  public InteractionConfigScreen(InteractionDataEntry entry, boolean isNew) {
    this(entry, isNew, List.of());
  }

  public static String targetContextLabel(InteractionDataEntry entry) {
    if (entry.kind() == TargetKind.ENTITY) {
      String shortId = entry.targetId() != null ? entry.targetId().toString().substring(0, 8) : "?";
      return "Entity (" + shortId + ")";
    }

    String kindName = entry.kind() == TargetKind.BLOCK_ENTITY ? "Block Entity" : "Block";
    return entry.blockPos() != null
        ? kindName + " (" + entry.blockPos().toShortString() + ")"
        : kindName;
  }

  public static void openWithData(InteractionDataEntry entry, boolean isNew) {
    openWithData(entry, isNew, List.of());
  }

  public static void openWithData(
      InteractionDataEntry entry, boolean isNew, List<BreadcrumbBar.Segment> ancestors) {
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
    setSizeCentered(320, 220);
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = getInnerWidth();
    int labelColumnX = 0;
    int fieldColumnX = 70;
    int fieldWidth = innerWidth - fieldColumnX - 10;
    int row = 0;
    int rowHeight = 22;

    addWidget(
        new Label(
            labelColumnX, row + 4, "field.label", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    labelInput = new TextInput(fieldColumnX, row, fieldWidth, 16, value -> editLabel = value);
    labelInput.setValue(editLabel);
    labelInput.setMaxLength(128);
    addWidget(labelInput);
    row += rowHeight;

    List<SelectOption<InteractionType>> typeOptions =
        Arrays.stream(InteractionType.values())
            .map(t -> SelectOption.of(t.name(), t))
            .collect(Collectors.toList());
    addWidget(
        new Label(
            labelColumnX, row + 4, "field.type", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    typeSelect =
        new SelectBox<>(
            fieldColumnX,
            row,
            Math.min(fieldWidth, 120),
            16,
            typeOptions,
            type -> editType = type,
            this::openOverlay,
            this::closeOverlay);
    typeSelect.selectByValue(editType);
    addWidget(typeSelect);
    row += rowHeight;

    addWidget(
        new Label(
            labelColumnX,
            row + 4,
            "field.target",
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    addWidget(
        new Label(
            fieldColumnX,
            row + 4,
            entry.kind().name(),
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    row += rowHeight;

    if (entry.blockPos() != null) {
      addWidget(
          new Label(
              labelColumnX,
              row + 4,
              "field.position",
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
      addWidget(
          new Label(
              fieldColumnX,
              row + 4,
              entry.blockPos().toShortString(),
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
      row += rowHeight;
    }

    addWidget(
        new Label(
            labelColumnX,
            row + 4,
            "field.dimension",
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    addWidget(
        new Label(
            fieldColumnX,
            row + 4,
            entry.dimension().toString(),
            0,
            ScaledText.SCALE_SMALL,
            Label.Alignment.LEFT));
    row += rowHeight;

    addWidget(new Separator(0, row, innerWidth, true));
    row += 6;

    TextButton actionsButton =
        new TextButton(
            labelColumnX,
            row,
            Math.min(innerWidth, 160),
            20,
            "button.edit_actions",
            btn -> {
              List<BreadcrumbBar.Segment> childAncestors = buildChildAncestors();
              ActionEditorScreen editorScreen = new ActionEditorScreen(entry, childAncestors);
              editorScreen.openScreen();
            });
    addWidget(actionsButton);
    row += 28;

    int buttonWidth = 70;
    int btnSpacing = 8;
    int buttonCount = isNew ? 2 : 3;
    int totalButtonWidth = buttonWidth * buttonCount + btnSpacing * (buttonCount - 1);
    int buttonStartX = (innerWidth - totalButtonWidth) / 2;

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

    if (!isNew) {
      TextButton removeBtn =
          new TextButton(
              buttonStartX + (buttonWidth + btnSpacing) * 2,
              row,
              buttonWidth,
              20,
              "button.remove",
              btn -> {
                NetworkHandlerManager.sendToServer(new RemoveInteractionMessage(entry));
                closeScreen();
              });
      addWidget(removeBtn);
    }
  }

  private void saveAndClose() {
    String label = labelInput.getValue();
    if (label.isEmpty()) {
      label = editLabel;
    }
    InteractionDataEntry updatedEntry =
        new InteractionDataEntry(
            entry.targetId(), editType, entry.kind(), label, entry.dimension(), entry.blockPos());
    NetworkHandlerManager.sendToServer(new SaveInteractionMessage(updatedEntry));
    closeScreen();
  }
}
