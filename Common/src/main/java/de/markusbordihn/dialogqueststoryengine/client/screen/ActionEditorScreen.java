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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Panel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Checkbox;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ConfirmModal;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Label;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.MessageListModal;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.SelectBox;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.SelectOption;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Separator;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextButton;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextInput;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogClientRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestClientRegistry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.SaveInteractionMessage;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntryClientRegistry;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import java.util.Arrays;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ActionEditorScreen extends BaseScreen {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private final InteractionEntry entry;
  private final ActionDataSet editableActionDataSet;
  private final Consumer<InteractionEntry> onSaveCallback;
  private CompoundTag originalActionDataTag;
  private ActionType selectedNewType = ActionType.OPEN_STORY;
  private Panel configPanel;
  private TextButton addActionButton;
  private TextButton saveButton;
  private TextButton cancelButton;

  private FieldControl field1Control;
  private FieldControl field2Control;
  private ActionDataEntry editingAction;
  private String editField1Prefill = null;
  private String editField2Prefill = null;
  private boolean cancelDefaultAction;
  private boolean originalCancelDefaultAction;

  private TextInput priorityInput;
  private int draftPriority;
  private ConditionEditorSpecs.Kind conditionKind = ConditionEditorSpecs.Kind.NONE;
  private Panel conditionPanel;
  private FieldControl conditionField1Control;
  private FieldControl conditionField2Control;
  private String conditionField1Prefill = null;
  private String conditionField2Prefill = null;
  private String rawUnrecognizedCondition = "";

  public ActionEditorScreen(
      InteractionEntry entry,
      List<BreadcrumbBar.Segment> ancestors,
      Consumer<InteractionEntry> onSaveCallback) {
    this.entry = entry;
    this.editableActionDataSet = entry.actionDataSet().copy();
    this.originalActionDataTag = entry.actionDataSet().save();
    this.cancelDefaultAction = entry.cancelDefaultAction();
    this.originalCancelDefaultAction = entry.cancelDefaultAction();
    this.onSaveCallback = onSaveCallback;
    this.setBreadcrumb(ancestors, "Actions");
    this.setScreenType(ScreenType.ACTIONS);
  }

  public ActionEditorScreen(InteractionEntry entry, List<BreadcrumbBar.Segment> ancestors) {
    this(entry, ancestors, null);
  }

  private static List<String> idsFor(ActionEditorSpecs.RefSource ref) {
    return switch (ref) {
      case STORY ->
          StoryEntryClientRegistry.ids().stream()
              .map(ResourceLocation::toString)
              .sorted()
              .collect(Collectors.toList());
      case DIALOG ->
          DialogClientRegistry.ids().stream()
              .map(ResourceLocation::toString)
              .sorted()
              .collect(Collectors.toList());
      case QUEST ->
          QuestClientRegistry.ids().stream()
              .map(ResourceLocation::toString)
              .sorted()
              .collect(Collectors.toList());
      case THEME ->
          ThemeClientRegistry.ids().stream()
              .map(ResourceLocation::toString)
              .sorted()
              .collect(Collectors.toList());
      case ITEM ->
          BuiltInRegistries.ITEM.keySet().stream()
              .map(ResourceLocation::toString)
              .sorted()
              .collect(Collectors.toList());
      case NONE -> List.of();
    };
  }

  @Override
  protected Component getTitle() {
    String eventTypeName = this.entry.eventType().name();
    String shortType = eventTypeName.startsWith("ON_") ? eventTypeName.substring(3) : eventTypeName;
    return TextComponent.of("Action Editor - " + this.entry.label() + " (" + shortType + ")");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    this.setSizeCentered(380, 340);
    this.refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    int innerWidth = this.getInnerWidth();
    int row = 0;
    int rowHeight = 22;

    if (ClientActionDiagnostics.has(this.entry.targetId())) {
      int count = ClientActionDiagnostics.get(this.entry.targetId()).size();
      TextButton warnButton =
          new TextButton(
              innerWidth - 110,
              row,
              110,
              16,
              "⚠ " + count + " issues",
              button -> this.openDiagnosticsModal());
      warnButton.setTooltip(TextComponent.of("diagnostics.tooltip").getString());
      this.addWidget(warnButton);
      row += 20;
    }

    for (ActionDataEntry existing : this.editableActionDataSet.entries()) {
      int capturedRow = row;
      String summary = this.formatActionSummary(existing);
      this.addWidget(
          new Label(0, capturedRow + 4, summary, 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
      this.addWidget(
          new TextButton(
              innerWidth - 104,
              capturedRow,
              52,
              16,
              "button.edit",
              button -> {
                this.prefillFromEntry(existing);
                this.editingAction = existing;
                this.refreshWidgets();
              }));
      this.addWidget(
          new TextButton(
              innerWidth - 50,
              capturedRow,
              48,
              16,
              "button.remove",
              button -> this.confirmRemoveAction(existing)));
      row += rowHeight;
    }

    if (!this.editableActionDataSet.entries().isEmpty()) {
      this.addWidget(new Separator(0, row, innerWidth, true));
      row += 8;
    }

    this.addWidget(
        new Label(
            0, row + 4, "field.action_type", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    List<SelectOption<ActionType>> actionOptions =
        Arrays.stream(ActionType.values())
            .filter(actionType -> actionType != ActionType.NONE)
            .map(actionType -> SelectOption.of(this.formatActionName(actionType), actionType))
            .collect(Collectors.toList());
    SelectBox<ActionType> actionSelect =
        new SelectBox<>(
            80,
            row,
            Math.min(innerWidth - 80, 160),
            16,
            actionOptions,
            actionType -> {
              this.captureDraftInputs();
              this.selectedNewType = actionType;
              this.editingAction = null;
              this.field1Control = null;
              this.field2Control = null;
              this.refreshWidgets();
            },
            this::openOverlay,
            this::closeOverlay);
    actionSelect.selectByValue(this.selectedNewType);
    this.addWidget(actionSelect);
    row += rowHeight + 4;

    this.configPanel =
        new Panel(0, row, innerWidth, 46) {
          @Override
          protected void renderBackground(
              GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ColorPalette palette = ColorPalette.current();
            int panelX = this.getX();
            int panelY = this.getY();
            graphics.fill(
                panelX,
                panelY,
                panelX + this.getWidth(),
                panelY + this.getHeight(),
                palette.background());
          }

          @Override
          protected void addWidgets() {
            ActionEditorScreen.this.field1Control = null;
            ActionEditorScreen.this.field2Control = null;
            ActionEditorScreen.this.populateConfigPanel(this);
            ActionEditorScreen.this.applyPrefills();
          }
        };
    this.configPanel.setParent(this);
    this.populateConfigPanel(this.configPanel);
    this.addWidget(this.configPanel);
    row += 50;

    this.addWidget(
        new Label(0, row + 4, "field.priority", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    this.priorityInput = new TextInput(86, row, 50, 14, value -> this.updateButtonStates());
    this.priorityInput.setSuggestion("0");
    this.priorityInput.setMaxLength(6);
    this.priorityInput.setValue(String.valueOf(this.draftPriority));
    this.addWidget(this.priorityInput);
    row += 20;

    this.addWidget(
        new Label(0, row + 4, "field.condition", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
    List<SelectOption<ConditionEditorSpecs.Kind>> conditionOptions =
        Arrays.stream(ConditionEditorSpecs.Kind.values())
            .map(kind -> SelectOption.of(TextComponent.of(kind.labelKey()).getString(), kind))
            .collect(Collectors.toList());
    SelectBox<ConditionEditorSpecs.Kind> conditionSelect =
        new SelectBox<>(
            86,
            row,
            Math.min(innerWidth - 86, 180),
            16,
            conditionOptions,
            kind -> {
              this.captureDraftInputs();
              this.conditionKind = kind;
              this.conditionField1Prefill = null;
              this.conditionField2Prefill = null;
              this.rawUnrecognizedCondition = "";
              this.refreshConditionPanel();
              this.updateButtonStates();
            },
            this::openOverlay,
            this::closeOverlay);
    conditionSelect.selectByValue(this.conditionKind);
    this.addWidget(conditionSelect);
    row += 20;

    this.conditionPanel =
        new Panel(0, row, innerWidth, 40) {
          @Override
          protected void renderBackground(
              GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            ColorPalette palette = ColorPalette.current();
            graphics.fill(
                this.getX(),
                this.getY(),
                this.getX() + this.getWidth(),
                this.getY() + this.getHeight(),
                palette.background());
          }

          @Override
          protected void addWidgets() {
            ActionEditorScreen.this.conditionField1Control = null;
            ActionEditorScreen.this.conditionField2Control = null;
            ActionEditorScreen.this.populateConditionPanel(this);
            ActionEditorScreen.this.applyConditionPrefills();
          }
        };
    this.conditionPanel.setParent(this);
    this.populateConditionPanel(this.conditionPanel);
    this.addWidget(this.conditionPanel);
    row += 44;

    int addButtonWidth = 60;
    this.addActionButton =
        new TextButton(
            (innerWidth - addButtonWidth) / 2,
            row,
            addButtonWidth,
            16,
            this.editingAction != null ? "button.update" : "button.add",
            button -> this.addCurrentAction());
    this.addWidget(this.addActionButton);
    row += 24;

    if (this.supportsCancelDefault()) {
      this.addWidget(new Separator(0, row, innerWidth, true));
      row += 8;
      this.addWidget(
          new Checkbox(
              0,
              row,
              "field.cancel_default_action",
              this.cancelDefaultAction,
              checked -> {
                this.cancelDefaultAction = checked;
                this.updateButtonStates();
              }));
      row += 20;
    }

    this.addWidget(new Separator(0, row, innerWidth, true));
    row += 8;

    int buttonWidth = 70;
    int buttonSpacing = 8;
    int buttonStartX = (innerWidth - (buttonWidth * 2 + buttonSpacing)) / 2;
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
    this.updateButtonStates();
  }

  private void populateConfigPanel(Panel panel) {
    int y = 4;
    int fieldWidth = panel.getWidth() - 90;
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(this.selectedNewType);
    if (spec == null || (spec.field1() == null && spec.field2() == null)) {
      panel.addWidget(
          new Label(4, y, "action.default.hint", 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));
      return;
    }

    if (spec.field1() != null) {
      this.field1Control = this.addField(panel, spec.field1(), y, fieldWidth);
      y += 18;
    }
    if (spec.field2() != null) {
      this.field2Control = this.addField(panel, spec.field2(), y, fieldWidth);
    }
  }

  private FieldControl addField(Panel panel, ActionEditorSpecs.Field field, int y, int fieldWidth) {
    return this.addField(
        panel, field.labelKey(), field.suggestion(), field.maxLength(), field.ref(), y, fieldWidth);
  }

  private FieldControl addField(
      Panel panel,
      String labelKey,
      String suggestion,
      int maxLength,
      ActionEditorSpecs.RefSource ref,
      int y,
      int fieldWidth) {
    panel.addWidget(new Label(4, y + 3, labelKey, 0, ScaledText.SCALE_SMALL, Label.Alignment.LEFT));

    List<String> options = idsFor(ref);
    if (ref != ActionEditorSpecs.RefSource.NONE && !options.isEmpty()) {
      List<SelectOption<String>> selectOptions =
          options.stream().map(id -> SelectOption.of(id, id)).collect(Collectors.toList());
      SelectBox<String> select =
          new SelectBox<>(
              86,
              y,
              fieldWidth,
              14,
              selectOptions,
              value -> this.updateButtonStates(),
              this::openOverlay,
              this::closeOverlay);
      select.setSearchable(true);
      panel.addWidget(select);
      return new SelectFieldControl(select);
    }

    TextInput input = new TextInput(86, y, fieldWidth, 14, value -> this.updateButtonStates());
    input.setSuggestion(suggestion);
    input.setMaxLength(maxLength);
    panel.addWidget(input);
    return new TextFieldControl(input);
  }

  private void refreshConditionPanel() {
    if (this.conditionPanel != null) {
      this.conditionPanel.clearWidgets();
      this.conditionField1Control = null;
      this.conditionField2Control = null;
      this.populateConditionPanel(this.conditionPanel);
    }
  }

  private void populateConditionPanel(Panel panel) {
    int y = 4;
    int fieldWidth = panel.getWidth() - 90;
    ConditionEditorSpecs.Spec spec = ConditionEditorSpecs.get(this.conditionKind);
    this.conditionField1Control = null;
    this.conditionField2Control = null;
    if (spec == null || (spec.field1() == null && spec.field2() == null)) {
      return;
    }
    if (spec.field1() != null) {
      ConditionEditorSpecs.Field field = spec.field1();
      this.conditionField1Control =
          this.addField(
              panel,
              field.labelKey(),
              field.suggestion(),
              field.maxLength(),
              field.ref(),
              y,
              fieldWidth);
      y += 18;
    }
    if (spec.field2() != null) {
      ConditionEditorSpecs.Field field = spec.field2();
      this.conditionField2Control =
          this.addField(
              panel,
              field.labelKey(),
              field.suggestion(),
              field.maxLength(),
              field.ref(),
              y,
              fieldWidth);
    }
  }

  private void applyConditionPrefills() {
    if (this.conditionField1Prefill != null && this.conditionField1Control != null) {
      this.conditionField1Control.setValue(this.conditionField1Prefill);
      this.conditionField1Prefill = null;
    }
    if (this.conditionField2Prefill != null && this.conditionField2Control != null) {
      this.conditionField2Control.setValue(this.conditionField2Prefill);
      this.conditionField2Prefill = null;
    }
  }

  private void captureDraftInputs() {
    if (this.priorityInput != null) {
      this.draftPriority = this.parsePriority(this.priorityInput.getValue());
    }
    if (this.conditionField1Control != null) {
      this.conditionField1Prefill = this.conditionField1Control.getValue();
    }
    if (this.conditionField2Control != null) {
      this.conditionField2Prefill = this.conditionField2Control.getValue();
    }
  }

  private int parsePriority(String value) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
      return 0;
    }
  }

  private String buildConditionJson() {
    if (this.conditionKind == ConditionEditorSpecs.Kind.NONE) {
      return this.rawUnrecognizedCondition;
    }
    ConditionEditorSpecs.Spec spec = ConditionEditorSpecs.get(this.conditionKind);
    if (spec == null) {
      return "";
    }
    String value1 =
        this.conditionField1Control != null ? this.conditionField1Control.getValue().trim() : "";
    String value2 =
        this.conditionField2Control != null ? this.conditionField2Control.getValue().trim() : "";
    return spec.build().apply(value1, value2);
  }

  private void addCurrentAction() {
    ActionDataEntry action = this.createCurrentAction(true);
    if (action == null) {
      this.updateButtonStates();
      return;
    }

    if (this.editingAction != null) {
      this.editableActionDataSet.replace(action);
    } else {
      this.editableActionDataSet.add(action);
    }
    this.clearCurrentActionDraft();
    this.refreshWidgets();
  }

  private ActionDataEntry createCurrentAction(boolean logInvalid) {
    String value1 = this.field1Control != null ? this.field1Control.getValue().trim() : "";
    String value2 = this.field2Control != null ? this.field2Control.getValue().trim() : "";
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(this.selectedNewType);
    ActionDataEntry action = spec != null ? spec.build().apply(value1, value2) : null;

    if (action == null) {
      if (logInvalid && !value1.isEmpty()) {
        log.warn(
            "{} ActionEditorScreen: invalid input for action {}",
            Constants.LOG_PREFIX,
            this.selectedNewType);
      }
      return null;
    }

    int priority =
        this.priorityInput != null
            ? this.parsePriority(this.priorityInput.getValue())
            : this.draftPriority;
    String condition = this.buildConditionJson();
    ActionDataEntry base =
        this.editingAction != null
            ? new ActionDataEntry(this.editingAction.id(), action.type(), action.data().copy())
            : action;
    return base.withConditionAndPriority(condition, priority);
  }

  private void saveAndClose() {
    if (!this.hasUnsavedChanges()) {
      return;
    }

    this.commitPendingCurrentAction();
    this.persistInteraction();
    this.closeScreen();
  }

  private void persistInteraction() {
    InteractionEntry updatedEntry =
        this.entry.withEdits(
            this.entry.interactionType(),
            this.entry.label(),
            this.editableActionDataSet,
            this.cancelDefaultAction);
    NetworkHandlerManager.sendToServer(new SaveInteractionMessage(updatedEntry));
    if (this.onSaveCallback != null) {
      this.onSaveCallback.accept(updatedEntry);
    }
    this.originalActionDataTag = this.editableActionDataSet.save();
    this.originalCancelDefaultAction = this.cancelDefaultAction;
  }

  private void openDiagnosticsModal() {
    MessageListModal modal =
        new MessageListModal(
            this.screenWidth,
            this.screenHeight,
            "diagnostics.title",
            ClientActionDiagnostics.get(this.entry.targetId()),
            this::closeModal);
    this.openModal(modal);
  }

  private void confirmRemoveAction(ActionDataEntry action) {
    ConfirmModal modal =
        new ConfirmModal(
            this.screenWidth,
            this.screenHeight,
            "confirm.delete_action.title",
            "confirm.delete_action.message",
            this.formatActionSummary(action),
            "button.delete",
            () -> {
              this.editableActionDataSet.remove(action.id());
              if (this.editingAction != null && this.editingAction.id().equals(action.id())) {
                this.clearCurrentActionDraft();
              }
              this.persistInteraction();
              this.closeModal();
              this.refreshWidgets();
            },
            this::closeModal);
    this.openModal(modal);
  }

  private void commitPendingCurrentAction() {
    ActionDataEntry action = this.createCurrentAction(false);
    if (action == null
        || (this.editingAction != null && this.actionsEqual(action, this.editingAction))) {
      return;
    }

    if (this.editingAction != null) {
      this.editableActionDataSet.replace(action);
    } else {
      this.editableActionDataSet.add(action);
    }
    this.clearCurrentActionDraft();
  }

  private boolean hasUnsavedChanges() {
    return this.hasActionDataChanges()
        || this.hasPendingCurrentActionChange()
        || this.cancelDefaultAction != this.originalCancelDefaultAction;
  }

  private boolean supportsCancelDefault() {
    return this.entry.eventType() == InteractionEventType.ON_BLOCK_INTERACT
        || this.entry.eventType() == InteractionEventType.ON_ENTITY_INTERACT;
  }

  private boolean hasActionDataChanges() {
    return !this.editableActionDataSet.save().equals(this.originalActionDataTag);
  }

  private boolean hasPendingCurrentActionChange() {
    ActionDataEntry action = this.createCurrentAction(false);
    if (action == null) {
      return false;
    }

    return this.editingAction == null || !this.actionsEqual(action, this.editingAction);
  }

  private boolean actionsEqual(ActionDataEntry first, ActionDataEntry second) {
    return first != null && second != null && first.save().equals(second.save());
  }

  private void clearCurrentActionDraft() {
    this.editingAction = null;
    this.editField1Prefill = null;
    this.editField2Prefill = null;
    this.field1Control = null;
    this.field2Control = null;
    this.draftPriority = 0;
    this.conditionKind = ConditionEditorSpecs.Kind.NONE;
    this.conditionField1Prefill = null;
    this.conditionField2Prefill = null;
    this.conditionField1Control = null;
    this.conditionField2Control = null;
    this.rawUnrecognizedCondition = "";
    if (this.priorityInput != null) {
      this.priorityInput.setValue("0");
    }
  }

  private void updateButtonStates() {
    if (this.addActionButton != null) {
      this.addActionButton.setActive(this.createCurrentAction(false) != null);
    }
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

  private String formatActionSummary(ActionDataEntry action) {
    String typeName = this.formatActionName(action.type());
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(action.type());
    String detail = spec != null ? spec.summary().apply(action) : null;
    String base = detail == null || detail.isBlank() ? typeName : typeName + ": " + detail;
    return base + this.formatGating(action);
  }

  private String formatGating(ActionDataEntry action) {
    StringBuilder extra = new StringBuilder();
    if (action.priority() != 0) {
      extra.append("  [P").append(action.priority()).append("]");
    }
    if (!action.condition().isBlank()) {
      extra.append("  [").append(this.conditionSummary(action.condition())).append("]");
    }
    return extra.toString();
  }

  private String conditionSummary(String conditionJson) {
    ConditionEditorSpecs.Kind kind = ConditionEditorSpecs.kindOf(conditionJson);
    JsonObject json = ConditionEditorSpecs.parse(conditionJson);
    ConditionEditorSpecs.Spec spec = ConditionEditorSpecs.get(kind);
    if (kind == ConditionEditorSpecs.Kind.NONE || spec == null || json == null) {
      return "if custom";
    }
    return "if " + spec.summary().apply(json);
  }

  private String formatActionName(ActionType type) {
    StringBuilder builder = new StringBuilder();
    boolean capitalize = true;
    for (char character : type.name().replace('_', ' ').toCharArray()) {
      if (character == ' ') {
        builder.append(' ');
        capitalize = true;
      } else if (capitalize) {
        builder.append(Character.toUpperCase(character));
        capitalize = false;
      } else {
        builder.append(Character.toLowerCase(character));
      }
    }
    return builder.toString();
  }

  private void prefillFromEntry(ActionDataEntry action) {
    this.selectedNewType = action.type();
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(action.type());
    if (spec != null) {
      this.editField1Prefill = spec.field1() != null ? spec.read1().apply(action) : null;
      this.editField2Prefill = spec.field2() != null ? spec.read2().apply(action) : null;
    }

    this.draftPriority = action.priority();
    String condition = action.condition();
    this.conditionKind = ConditionEditorSpecs.kindOf(condition);
    this.rawUnrecognizedCondition =
        this.conditionKind == ConditionEditorSpecs.Kind.NONE
                && condition != null
                && !condition.isBlank()
            ? condition
            : "";
    JsonObject conditionObject = ConditionEditorSpecs.parse(condition);
    ConditionEditorSpecs.Spec conditionSpec = ConditionEditorSpecs.get(this.conditionKind);
    this.conditionField1Prefill =
        conditionSpec != null && conditionSpec.field1() != null && conditionObject != null
            ? conditionSpec.read1().apply(conditionObject)
            : null;
    this.conditionField2Prefill =
        conditionSpec != null && conditionSpec.field2() != null && conditionObject != null
            ? conditionSpec.read2().apply(conditionObject)
            : null;
  }

  private void applyPrefills() {
    if (this.editField1Prefill != null && this.field1Control != null) {
      this.field1Control.setValue(this.editField1Prefill);
      this.editField1Prefill = null;
    }
    if (this.editField2Prefill != null && this.field2Control != null) {
      this.field2Control.setValue(this.editField2Prefill);
      this.editField2Prefill = null;
    }
  }

  private interface FieldControl {
    String getValue();

    void setValue(String value);
  }

  private record TextFieldControl(TextInput input) implements FieldControl {
    @Override
    public String getValue() {
      return this.input.getValue().trim();
    }

    @Override
    public void setValue(String value) {
      this.input.setValue(value);
    }
  }

  private record SelectFieldControl(SelectBox<String> select) implements FieldControl {
    @Override
    public String getValue() {
      String value = this.select.getSelectedValue();
      return value != null ? value : "";
    }

    @Override
    public void setValue(String value) {
      this.select.selectByValue(value);
    }
  }
}
