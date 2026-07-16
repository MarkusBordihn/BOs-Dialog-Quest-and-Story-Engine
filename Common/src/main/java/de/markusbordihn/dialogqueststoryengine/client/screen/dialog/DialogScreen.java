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

import de.markusbordihn.dialogqueststoryengine.client.story.StoryScreen;
import de.markusbordihn.dialogqueststoryengine.client.story.TypewriterAnimator;
import de.markusbordihn.dialogqueststoryengine.data.theme.ScreenLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
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

  private static final int COLOR_SPEAKER = 0x00FFFF;

  private final Theme theme;
  private final TypewriterAnimator animator = new TypewriterAnimator();
  private DialogSessionData sessionData;

  private ScreenLayout layout;
  private List<Button> choiceButtons = new ArrayList<>();
  private boolean awaitingResponse;
  private boolean sessionActive = true;

  private DialogScreen(DialogSessionData sessionData, Theme theme) {
    super(Component.empty());
    this.sessionData = sessionData;
    this.theme = theme;
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
    rebuildChoiceButtons();
    startAnimator();
  }

  public void onServerRejection() {
    this.awaitingResponse = false;
    this.choiceButtons.forEach(button -> button.active = true);
  }

  @Override
  protected void init() {
    this.layout = ScreenLayout.from(this.theme, this.width, this.height);
    rebuildChoiceButtons();
    startAnimator();
  }

  @Override
  public void tick() {
    this.animator.tick();
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderDimBackground(graphics);
    renderScreenTexture(graphics, this.layout.backgroundTexture(), this.layout);
    renderScreenTexture(graphics, this.layout.frameTexture(), this.layout);
    renderSpeakerName(graphics);
    renderRevealedText(graphics, this.layout, this.animator);
    super.render(graphics, mouseX, mouseY, partialTick);
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
            this.layout.textArea().width(),
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
        addCloseButtonWidget();
      }
      return;
    }

    int buttonWidth = this.layout.choiceButtonWidth();

    for (int i = 0; i < allowedChoiceIds.size(); i++) {
      String choiceId = allowedChoiceIds.get(i);
      String labelKey = this.sessionData.choiceLabels().getOrDefault(choiceId, choiceId);
      int buttonY =
          this.layout.choiceButtonY(
              i, allowedChoiceIds.size(), CHOICE_BUTTON_HEIGHT, CHOICE_BUTTON_SPACING);

      Button button =
          Button.builder(Component.translatable(labelKey), pressed -> onChoiceClicked(choiceId))
              .pos(this.layout.choiceButtonX(buttonWidth), buttonY)
              .size(buttonWidth, CHOICE_BUTTON_HEIGHT)
              .build();

      this.choiceButtons.add(button);
      addRenderableWidget(button);
    }
  }

  private void addCloseButtonWidget() {
    int buttonWidth = Math.min(80, this.layout.choiceButtonWidth());
    int buttonX = this.layout.choiceButtonX(buttonWidth);
    int buttonY = this.layout.choiceButtonY(0, 1, CHOICE_BUTTON_HEIGHT, CHOICE_BUTTON_SPACING);

    Button button =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.close"),
                pressed -> this.onClose())
            .pos(buttonX, buttonY)
            .size(buttonWidth, CHOICE_BUTTON_HEIGHT)
            .build();

    this.choiceButtons.add(button);
    addRenderableWidget(button);
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
    int speakerX = this.layout.titleX(this.font.width(speaker));
    int speakerY = this.layout.titleY(this.font.lineHeight);
    graphics.drawString(this.font, speaker, speakerX, speakerY, COLOR_SPEAKER, false);
  }
}
