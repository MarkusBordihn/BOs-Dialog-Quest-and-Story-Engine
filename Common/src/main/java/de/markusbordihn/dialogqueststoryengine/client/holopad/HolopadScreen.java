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

import de.markusbordihn.dialogqueststoryengine.client.story.StoryScreen;
import de.markusbordihn.dialogqueststoryengine.client.story.TypewriterAnimator;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryPage;
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
import org.lwjgl.glfw.GLFW;

public class HolopadScreen extends StoryScreen {

  private static final int COLOR_TITLE = 0x00FFFF;
  private static final int COLOR_PAGE_NUMBER = 0xAAAAAA;
  private final StoryEntry entry;
  private final Theme theme;
  private final TypewriterAnimator animator = new TypewriterAnimator();
  private final SessionData sessionData;
  private ScreenLayout layout;
  private List<List<String>> pages;
  private int currentPage;
  private Button prevButton;
  private Button nextButton;
  private Button closeButton;
  private Button replayButton;

  private HolopadScreen(StoryEntry entry, Theme theme, SessionData sessionData) {
    super(Component.translatable(entry.titleKey()));
    this.entry = entry;
    this.theme = theme;
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
  protected void init() {
    this.layout = ScreenLayout.from(this.theme, this.width, this.height);

    ArrayList<List<String>> allPages = new ArrayList<>();
    for (StoryPage storyPage : this.entry.pages()) {
      allPages.addAll(
          HolopadPageRenderer.paginate(
              Component.translatable(storyPage.textKey()),
              this.font,
              this.layout.textArea().width(),
              this.layout.textArea().height()));
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
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    renderDimBackground(graphics);
    renderScreenTexture(graphics, this.layout.backgroundTexture(), this.layout);
    renderScreenTexture(graphics, this.layout.frameTexture(), this.layout);
    renderTitle(graphics);
    renderRevealedText(graphics, this.layout, this.animator);

    if (this.layout.showPageNumbers() && this.animator.isComplete()) {
      renderPageNumber(graphics);
    }

    super.render(graphics, mouseX, mouseY, partialTick);
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
    int titleX = this.layout.titleX(this.font.width(this.title));
    int titleY = this.layout.titleY(this.font.lineHeight);
    graphics.drawString(this.font, this.title, titleX, titleY, COLOR_TITLE, false);
  }

  private void renderPageNumber(GuiGraphics graphics) {
    String pageText = (this.currentPage + 1) + " / " + this.pages.size();
    int pageX =
        this.layout.leftPos()
            + this.layout.textArea().x()
            + this.layout.textArea().width()
            - this.font.width(pageText);
    int pageY =
        this.layout.topPos() + this.layout.textArea().y() + this.layout.textArea().height() + 2;
    graphics.drawString(this.font, pageText, pageX, pageY, COLOR_PAGE_NUMBER, false);
  }

  private void addReplayButton() {
    int buttonWidth = 80;
    int buttonHeight = 20;
    int buttonX =
        this.layout.leftPos()
            + this.layout.displayArea().x()
            + (this.layout.displayArea().width() - buttonWidth) / 2;
    int buttonY =
        this.layout.topPos()
            + this.layout.displayArea().y()
            + (this.layout.displayArea().height() - buttonHeight) / 2;

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
    int buttonY =
        this.layout.topPos() + this.layout.textArea().y() + this.layout.textArea().height() + 2;
    int buttonWidth = 40;
    int buttonHeight = 14;

    this.prevButton =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.prev"),
                button -> navigateTo(this.currentPage - 1))
            .bounds(
                this.layout.leftPos() + this.layout.textArea().x(),
                buttonY,
                buttonWidth,
                buttonHeight)
            .build();
    this.addRenderableWidget(this.prevButton);

    this.nextButton =
        Button.builder(
                Component.translatable("gui.dialog_quest_and_story_engine.button.next"),
                button -> navigateTo(this.currentPage + 1))
            .bounds(
                this.layout.leftPos()
                    + this.layout.textArea().x()
                    + this.layout.textArea().width()
                    - buttonWidth,
                buttonY,
                buttonWidth,
                buttonHeight)
            .build();
    this.addRenderableWidget(this.nextButton);

    updateNavigationButtons();
  }

  private void addCloseButtonWidget() {
    int buttonWidth = 40;
    int buttonHeight = 14;
    int buttonY =
        this.layout.topPos() + this.layout.textArea().y() + this.layout.textArea().height() + 2;
    int buttonX =
        this.layout.leftPos()
            + this.layout.textArea().x()
            + (this.layout.textArea().width() - buttonWidth) / 2;

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
    if (this.prevButton != null) {
      this.prevButton.active = this.currentPage > 0;
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
    int buttonWidth = this.layout.choiceButtonWidth();
    int buttonHeight = CHOICE_BUTTON_HEIGHT;
    int choiceCount = this.sessionData.allowedChoiceIds().size();

    int index = 0;
    for (String choiceId : this.sessionData.allowedChoiceIds()) {
      String labelKey = this.sessionData.choiceLabels().getOrDefault(choiceId, choiceId);
      int buttonX = this.layout.choiceButtonX(buttonWidth);
      int buttonY =
          this.layout.choiceButtonY(index, choiceCount, buttonHeight, CHOICE_BUTTON_SPACING);
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
