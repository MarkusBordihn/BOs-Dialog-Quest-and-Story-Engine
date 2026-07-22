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

package de.markusbordihn.dialogqueststoryengine.client.holopad;

import de.markusbordihn.dialogqueststoryengine.client.screen.theme.ChoiceListLayout;
import de.markusbordihn.dialogqueststoryengine.client.story.StoryScreen;
import de.markusbordihn.dialogqueststoryengine.client.story.TypewriterAnimator;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryPage;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.layout.HolopadLayout;
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
import org.lwjgl.glfw.GLFW;

public class HolopadScreen extends StoryScreen {

  private final StoryEntry entry;
  private final TypewriterAnimator animator = new TypewriterAnimator();
  private final SessionData sessionData;
  private HolopadLayout layout;
  private List<List<String>> pages;
  private int currentPage;
  private Button previousButton;
  private Button nextButton;
  private Button closeButton;
  private Button replayButton;

  private HolopadScreen(StoryEntry entry, Theme theme, SessionData sessionData) {
    super(Component.translatable(entry.titleKey()), theme);
    this.entry = entry;
    this.sessionData = sessionData;
  }

  public static void open(StoryEntry entry, Theme theme) {
    scheduleOpen(new HolopadScreen(entry, theme, null));
  }

  public static void openInteractive(StoryEntry entry, Theme theme, SessionData sessionData) {
    scheduleOpen(new HolopadScreen(entry, theme, sessionData));
  }

  public UUID sessionId() {
    return this.sessionData != null ? this.sessionData.sessionId() : null;
  }

  @Override
  protected void initThemed() {
    this.layout = new HolopadLayout(this.resolvedLayout);
    ThemeArea textArea = this.layout.text();

    ArrayList<List<String>> allPages = new ArrayList<>();
    for (StoryPage storyPage : this.entry.pages()) {
      allPages.addAll(
          HolopadPageRenderer.paginate(
              Component.translatable(storyPage.textKey()),
              this.font,
              textArea.width(),
              textArea.height()));
    }
    this.pages = List.copyOf(allPages);

    this.currentPage = 0;
    this.animator.start(this.pages.isEmpty() ? List.of() : this.pages.get(0));

    addNavigationButtons();
    addReplayButton();

    if (this.sessionData != null) {
      addChoiceButtons();
    }

    if (this.layout.showCloseButton()) {
      addCloseButtonWidget();
    }
  }

  @Override
  public void tick() {
    this.animator.tick();
    if (this.replayButton != null
        && !this.replayButton.visible
        && this.animator.isComplete()
        && this.currentPage == this.pages.size() - 1) {
      this.replayButton.visible = true;
    }
  }

  @Override
  protected void renderThemed(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderViewportSprite(graphics, this.layout.background());
    renderViewportSprite(graphics, this.layout.frame());
    renderTitle(graphics);
    renderRevealedText(graphics, this.layout.text(), this.animator, this.layout.textColor());

    if (this.layout.showPageNumbers() && this.animator.isComplete()) {
      renderPageNumber(graphics);
    }
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_LEFT) {
      navigateTo(this.currentPage - 1);
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_RIGHT) {
      navigateTo(this.currentPage + 1);
      return true;
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  protected void onSpacePressed() {
    if (!this.animator.isComplete()) {
      this.animator.skip();
    }
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (delta > 0) {
      navigateTo(this.currentPage - 1);
    } else if (delta < 0) {
      navigateTo(this.currentPage + 1);
    }
    return true;
  }

  private void renderTitle(GuiGraphics graphics) {
    ThemeArea titleArea = this.layout.title();
    int titleX = alignedX(titleArea, this.layout.titleAlignment(), this.font.width(this.title));
    int titleY = centeredY(titleArea, this.font.lineHeight);
    graphics.drawString(this.font, this.title, titleX, titleY, this.layout.accentColor(), false);
  }

  private void renderPageNumber(GuiGraphics graphics) {
    String pageText = (this.currentPage + 1) + " / " + this.pages.size();
    ThemeArea pageArea = this.layout.page().orElse(null);
    int pageX;
    int pageY;
    if (pageArea != null) {
      pageX = pageArea.x() + pageArea.width() - this.font.width(pageText);
      pageY = centeredY(pageArea, this.font.lineHeight);
    } else {
      ThemeArea textArea = this.layout.text();
      pageX = textArea.x() + textArea.width() - this.font.width(pageText);
      pageY = textArea.y() + textArea.height() + 2;
    }
    graphics.drawString(this.font, pageText, pageX, pageY, this.layout.mutedColor(), false);
  }

  private void addReplayButton() {
    ThemeArea display = this.layout.display();
    int buttonWidth = 80;
    int buttonHeight = 20;
    int buttonX = display.x() + (display.width() - buttonWidth) / 2;
    int buttonY = display.y() + (display.height() - buttonHeight) / 2;

    this.replayButton =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.replay"),
                button -> replay())
            .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
            .build();
    this.replayButton.visible = false;
    this.addRenderableWidget(this.replayButton);
  }

  private void addNavigationButtons() {
    ThemeArea textArea = this.layout.text();
    int buttonY = textArea.y() + textArea.height() + 2;
    int buttonWidth = 40;
    int buttonHeight = 14;

    this.previousButton =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.prev"),
                button -> navigateTo(this.currentPage - 1))
            .bounds(textArea.x(), buttonY, buttonWidth, buttonHeight)
            .build();
    this.addRenderableWidget(this.previousButton);

    this.nextButton =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.next"),
                button -> navigateTo(this.currentPage + 1))
            .bounds(
                textArea.x() + textArea.width() - buttonWidth, buttonY, buttonWidth, buttonHeight)
            .build();
    this.addRenderableWidget(this.nextButton);

    updateNavigationButtons();
  }

  private void addCloseButtonWidget() {
    ThemeArea textArea = this.layout.text();
    int buttonWidth = 40;
    int buttonHeight = 14;
    int buttonY = textArea.y() + textArea.height() + 2;
    int buttonX = textArea.x() + (textArea.width() - buttonWidth) / 2;

    this.closeButton =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.close"),
                button -> this.onClose())
            .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
            .build();
    this.addRenderableWidget(this.closeButton);
  }

  private void navigateTo(int page) {
    if (page < 0 || page >= this.pages.size()) {
      return;
    }

    this.currentPage = page;
    this.animator.start(this.pages.get(page));
    if (this.replayButton != null) {
      this.replayButton.visible = false;
    }
    updateNavigationButtons();
  }

  private void replay() {
    this.currentPage = 0;
    this.animator.start(this.pages.isEmpty() ? List.of() : this.pages.get(0));
    if (this.replayButton != null) {
      this.replayButton.visible = false;
    }
    updateNavigationButtons();
  }

  private void updateNavigationButtons() {
    if (this.previousButton != null) {
      this.previousButton.active = this.currentPage > 0;
    }

    if (this.nextButton != null) {
      this.nextButton.active = this.currentPage < this.pages.size() - 1;
    }
  }

  @Override
  public boolean isPauseScreen() {
    return false;
  }

  @Override
  public void onClose() {
    if (this.sessionData != null) {
      NetworkHandlerManager.sendToServer(
          new ClientCloseSessionPacket(this.sessionData.sessionId()));
    }
    super.onClose();
  }

  public void onServerRejection() {
    this.clearWidgets();
    this.init();
  }

  private void addChoiceButtons() {
    ThemeArea choices = this.layout.choices();
    int buttonWidth = choices.width();
    int buttonHeight = CHOICE_BUTTON_HEIGHT;
    int choiceCount = this.sessionData.allowedChoiceIds().size();

    int index = 0;
    for (String choiceId : this.sessionData.allowedChoiceIds()) {
      String labelKey = this.sessionData.choiceLabels().getOrDefault(choiceId, choiceId);
      int buttonX = ChoiceListLayout.buttonX(choices, buttonWidth);
      int buttonY =
          ChoiceListLayout.buttonY(
              choices, index, choiceCount, buttonHeight, CHOICE_BUTTON_SPACING);
      int revision = this.sessionData.revision();
      this.addRenderableWidget(
          Button.builder(
                  Component.translatable(labelKey),
                  button ->
                      NetworkHandlerManager.sendToServer(
                          new SubmitChoicePacket(this.sessionData.sessionId(), choiceId, revision)))
              .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
              .build());
      index++;
    }
  }

  public record SessionData(
      UUID sessionId,
      List<String> allowedChoiceIds,
      Map<String, String> choiceLabels,
      int revision) {}
}
