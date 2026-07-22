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

package de.markusbordihn.dialogqueststoryengine.client.screen.dialog;

import de.markusbordihn.dialogqueststoryengine.client.screen.theme.ChoiceListLayout;
import de.markusbordihn.dialogqueststoryengine.client.story.StoryScreen;
import de.markusbordihn.dialogqueststoryengine.client.story.TypewriterAnimator;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.layout.DialogLayout;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.ClientCloseSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.SubmitChoicePacket;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;

public class DialogScreen extends StoryScreen {

  private final TypewriterAnimator animator = new TypewriterAnimator();
  private DialogSessionData sessionData;

  private DialogLayout layout;
  private List<Button> choiceButtons = new ArrayList<>();
  private boolean awaitingResponse;
  private boolean sessionActive = true;

  private DialogScreen(DialogSessionData sessionData, Theme theme) {
    super(Component.empty(), theme);
    this.sessionData = sessionData;
  }

  public static void open(DialogSessionData sessionData, Theme theme) {
    scheduleOpen(new DialogScreen(sessionData, theme));
  }

  public UUID sessionId() {
    return this.sessionData.sessionId();
  }

  public void navigateToNode(
      String nodeId,
      String speakerKey,
      String textKey,
      List<String> allowedChoiceIds,
      Map<String, String> choiceLabels,
      int revision) {
    this.sessionData =
        new DialogSessionData(
            this.sessionData.sessionId(),
            this.sessionData.dialogId(),
            nodeId,
            speakerKey,
            textKey,
            allowedChoiceIds,
            choiceLabels,
            revision);
    this.awaitingResponse = false;
    this.rebuildChoiceButtons();
    this.startAnimator();
  }

  public void onServerRejection() {
    this.awaitingResponse = false;
    this.choiceButtons.forEach(button -> button.active = true);
  }

  @Override
  protected void initThemed() {
    this.layout = new DialogLayout(this.resolvedLayout);
    this.rebuildChoiceButtons();
    this.startAnimator();
  }

  @Override
  public void tick() {
    this.animator.tick();
  }

  @Override
  protected void renderThemed(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    this.renderViewportSprite(graphics, this.layout.background());
    this.renderViewportSprite(graphics, this.layout.frame());
    this.renderSpeakerName(graphics);
    this.renderRevealedText(graphics, this.layout.text(), this.animator, this.layout.textColor());
  }

  @Override
  public void onClose() {
    if (this.sessionActive && !this.awaitingResponse) {
      NetworkHandlerManager.sendToServer(
          new ClientCloseSessionPacket(this.sessionData.sessionId()));
      this.sessionActive = false;
    }

    super.onClose();
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  protected void onSpacePressed() {
    if (!this.animator.isComplete()) {
      this.animator.skip();
    }
  }

  private void startAnimator() {
    if (this.layout == null) {
      return;
    }

    List<String> wrappedLines = new ArrayList<>();
    this.font
        .getSplitter()
        .splitLines(
            Component.translatable(this.sessionData.textKey()),
            this.layout.text().width(),
            Style.EMPTY)
        .forEach(line -> wrappedLines.add(line.getString()));
    this.animator.start(wrappedLines);
  }

  private void rebuildChoiceButtons() {
    this.choiceButtons.forEach(this::removeWidget);
    this.choiceButtons = new ArrayList<>();

    if (this.layout == null) {
      return;
    }

    List<String> allowedChoiceIds = this.sessionData.allowedChoiceIds();

    if (allowedChoiceIds.isEmpty()) {
      if (this.layout.showCloseButton()) {
        this.addCloseButtonWidget();
      }
      return;
    }

    ThemeArea choices = this.layout.choices();
    int buttonWidth = choices.width();

    for (int i = 0; i < allowedChoiceIds.size(); i++) {
      String choiceId = allowedChoiceIds.get(i);
      String labelKey = this.sessionData.choiceLabels().getOrDefault(choiceId, choiceId);
      int buttonY =
          ChoiceListLayout.buttonY(
              choices, i, allowedChoiceIds.size(), CHOICE_BUTTON_HEIGHT, CHOICE_BUTTON_SPACING);

      Button button =
          Button.builder(
                  Component.translatable(labelKey), pressed -> this.onChoiceClicked(choiceId))
              .pos(ChoiceListLayout.buttonX(choices, buttonWidth), buttonY)
              .size(buttonWidth, CHOICE_BUTTON_HEIGHT)
              .build();

      this.choiceButtons.add(button);
      this.addRenderableWidget(button);
    }
  }

  private void addCloseButtonWidget() {
    ThemeArea choices = this.layout.choices();
    int buttonWidth = Math.min(80, choices.width());
    int buttonX = ChoiceListLayout.buttonX(choices, buttonWidth);
    int buttonY =
        ChoiceListLayout.buttonY(choices, 0, 1, CHOICE_BUTTON_HEIGHT, CHOICE_BUTTON_SPACING);

    Button button =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.close"),
                pressed -> this.onClose())
            .pos(buttonX, buttonY)
            .size(buttonWidth, CHOICE_BUTTON_HEIGHT)
            .build();

    this.choiceButtons.add(button);
    this.addRenderableWidget(button);
  }

  private void onChoiceClicked(String choiceId) {
    this.awaitingResponse = true;
    this.choiceButtons.forEach(button -> button.active = false);
    NetworkHandlerManager.sendToServer(
        new SubmitChoicePacket(
            this.sessionData.sessionId(), choiceId, this.sessionData.revision()));
  }

  private void renderSpeakerName(GuiGraphics graphics) {
    if (this.sessionData.speakerKey().isEmpty()) {
      return;
    }

    Component speaker = Component.translatable(this.sessionData.speakerKey());
    ThemeArea speakerArea = this.layout.speaker();
    int speakerX = alignedX(speakerArea, this.layout.speakerAlignment(), this.font.width(speaker));
    int speakerY = centeredY(speakerArea, this.font.lineHeight);
    graphics.drawString(this.font, speaker, speakerX, speakerY, this.layout.accentColor(), false);
  }
}
