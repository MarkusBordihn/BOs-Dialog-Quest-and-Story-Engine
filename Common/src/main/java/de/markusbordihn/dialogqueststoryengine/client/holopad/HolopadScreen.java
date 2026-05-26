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

import com.mojang.blaze3d.systems.RenderSystem;
import de.markusbordihn.dialogqueststoryengine.client.story.StoryScreen;
import de.markusbordihn.dialogqueststoryengine.client.story.TypewriterAnimator;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.ClientCloseSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.SubmitChoicePacket;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryPage;
import de.markusbordihn.dialogqueststoryengine.theme.Theme;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class HolopadScreen extends StoryScreen {

  public record SessionData(
      UUID sessionId,
      List<String> allowedChoiceIds,
      Map<String, String> choiceLabels,
      int revision) {}

  private static final int COLOR_TITLE = 0x00FFFF;
  private static final int COLOR_TEXT = 0xFFFFFF;
  private static final int COLOR_PAGE_NUMBER = 0xAAAAAA;
  private static final int TEXTURE_WIDTH = 512;
  private static final int TEXTURE_HEIGHT = 256;
  private static final int HOLOGRAM_AREA_X = 16;
  private static final int HOLOGRAM_AREA_Y = 16;
  private static final int HOLOGRAM_AREA_WIDTH = 338;
  private static final int HOLOGRAM_AREA_HEIGHT = 100;

  private final StoryEntry entry;
  private final Theme theme;
  private final TypewriterAnimator animator = new TypewriterAnimator();
  private final SessionData sessionData;

  private HolopadLayout layout;
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
    this.layout = HolopadLayout.from(this.theme, this.width, this.height);

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
    renderHolopadBackground(graphics);
    renderHolopadFrame(graphics);
    renderTitle(graphics);
    renderPageText(graphics);

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

  private void renderHolopadBackground(GuiGraphics graphics) {
    RenderSystem.setShaderTexture(0, this.layout.backgroundTexture());
    graphics.blit(
        this.layout.backgroundTexture(),
        this.layout.leftPos(),
        this.layout.topPos(),
        0,
        0,
        this.layout.screenWidth(),
        this.layout.screenHeight(),
        TEXTURE_WIDTH,
        TEXTURE_HEIGHT);
  }

  private void renderHolopadFrame(GuiGraphics graphics) {
    RenderSystem.setShaderTexture(0, this.layout.frameTexture());
    graphics.blit(
        this.layout.frameTexture(),
        this.layout.leftPos(),
        this.layout.topPos(),
        0,
        0,
        this.layout.screenWidth(),
        this.layout.screenHeight(),
        TEXTURE_WIDTH,
        TEXTURE_HEIGHT);
  }

  private void renderTitle(GuiGraphics graphics) {
    int titleX = this.layout.leftPos() + this.layout.textArea().x();
    int titleY = this.layout.topPos() + this.layout.textArea().y() - this.font.lineHeight - 4;
    graphics.drawString(this.font, this.title, titleX, titleY, COLOR_TITLE, false);
  }

  private void renderPageText(GuiGraphics graphics) {
    int textX = this.layout.leftPos() + this.layout.textArea().x();
    int textY = this.layout.topPos() + this.layout.textArea().y();
    int lineHeight = this.font.lineHeight + HolopadPageRenderer.LINE_SPACING;

    graphics.enableScissor(
        this.layout.leftPos() + this.layout.textArea().x(),
        this.layout.topPos() + this.layout.textArea().y(),
        this.layout.leftPos() + this.layout.textArea().x() + this.layout.textArea().width(),
        this.layout.topPos() + this.layout.textArea().y() + this.layout.textArea().height());

    List<String> revealedLines = this.animator.getRevealedLines();
    for (int i = 0; i < revealedLines.size(); i++) {
      graphics.drawString(
          this.font, revealedLines.get(i), textX, textY + i * lineHeight, COLOR_TEXT, false);
    }

    String partialLine = this.animator.getPartialLine();
    if (!partialLine.isEmpty()) {
      graphics.drawString(
          this.font,
          partialLine,
          textX,
          textY + revealedLines.size() * lineHeight,
          COLOR_TEXT,
          false);
    }

    graphics.disableScissor();
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
    int buttonX = this.layout.leftPos() + HOLOGRAM_AREA_X + (HOLOGRAM_AREA_WIDTH - buttonWidth) / 2;
    int buttonY =
        this.layout.topPos() + HOLOGRAM_AREA_Y + (HOLOGRAM_AREA_HEIGHT - buttonHeight) / 2;

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

  private void addChoiceButtons() {
    int buttonWidth = 120;
    int buttonHeight = 16;
    int startY =
        this.layout.topPos() + this.layout.textArea().y() + this.layout.textArea().height() + 22;

    int index = 0;
    for (String choiceId : this.sessionData.allowedChoiceIds()) {
      String labelKey = this.sessionData.choiceLabels().getOrDefault(choiceId, choiceId);
      int buttonX =
          this.layout.leftPos()
              + this.layout.textArea().x()
              + (this.layout.textArea().width() - buttonWidth) / 2;
      int buttonY = startY + index * (buttonHeight + 4);
      int revision = this.sessionData.revision();
      this.addRenderableWidget(
          Button.builder(
                  Component.translatable(labelKey),
                  button ->
                      NetworkHandlerManager.sendToServer(
                          new SubmitChoicePacket(
                              this.sessionData.sessionId(), choiceId, revision)))
              .bounds(buttonX, buttonY, buttonWidth, buttonHeight)
              .build());
      index++;
    }
  }
}
