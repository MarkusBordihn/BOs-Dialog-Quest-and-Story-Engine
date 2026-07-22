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

package de.markusbordihn.dialogqueststoryengine.client.screen.ui;

import de.markusbordihn.dialogqueststoryengine.client.screen.ScreenType;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BottomBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TitleBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Tooltip;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class BaseScreen extends Panel {

  protected static final int BREADCRUMB_HEIGHT = 16;
  protected static final int TITLE_BAR_HEIGHT = 16;
  protected static final int BOTTOM_BAR_HEIGHT = 18;
  private static final int OVERLAY_Z = 300;

  protected final Deque<Panel> modalPanels = new ArrayDeque<>();
  protected Screen previousScreen;
  protected int screenWidth;
  protected int screenHeight;
  private ScreenWrapper wrapper;
  private Panel overlayPanel;
  private int tooltipHoverTicks;
  private Widget tooltipCandidate;
  private ScreenType screenType;
  private List<BreadcrumbBar.Segment> breadcrumbAncestors = List.of();
  private String breadcrumbLabel = "";

  private BreadcrumbBar breadcrumbBar;
  private TitleBar titleBar;
  private BottomBar bottomBar;

  protected BaseScreen() {
    super(0, 0, 0, 0);
    this.clipChildren = false;
  }

  public static void closeAll() {
    Minecraft.getInstance().setScreen(null);
  }

  private static Widget findDeepestHoveredWithTooltip(Panel panel, double mouseX, double mouseY) {
    List<Widget> list = panel.getChildren();
    for (int i = list.size() - 1; i >= 0; i--) {
      Widget child = list.get(i);
      if (!child.isVisible() || !child.isMouseOver(mouseX, mouseY)) {
        continue;
      }

      if (child instanceof Panel childPanel) {
        Widget deeper = findDeepestHoveredWithTooltip(childPanel, mouseX, mouseY);
        if (deeper != null) {
          return deeper;
        }
      }

      if (child.getTooltipText() != null) {
        return child;
      }
    }

    return null;
  }

  public void setBreadcrumb(List<BreadcrumbBar.Segment> ancestors, String currentLabel) {
    this.breadcrumbAncestors = ancestors != null ? ancestors : List.of();
    this.breadcrumbLabel = currentLabel != null ? currentLabel : "";
  }

  public List<BreadcrumbBar.Segment> buildChildAncestors(String myLabel) {
    List<BreadcrumbBar.Segment> child = new ArrayList<>(this.breadcrumbAncestors);
    child.add(
        new BreadcrumbBar.Segment(
            myLabel, () -> Minecraft.getInstance().setScreen(this.getScreenWrapper())));

    return child;
  }

  public List<BreadcrumbBar.Segment> buildChildAncestors() {
    return this.buildChildAncestors(this.breadcrumbLabel);
  }

  public void openScreen() {
    Minecraft minecraft = Minecraft.getInstance();
    this.previousScreen = minecraft.screen;
    this.wrapper = new ScreenWrapper(this);
    minecraft.setScreen(this.wrapper);
  }

  public void closeScreen() {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.setScreen(this.previousScreen);
  }

  public Screen getScreenWrapper() {
    return this.wrapper;
  }

  public void openOverlay(Panel overlay) {
    this.overlayPanel = overlay;
  }

  public void closeOverlay() {
    if (this.overlayPanel != null) {
      this.overlayPanel.setParent(null);
      this.overlayPanel = null;
    }
  }

  public boolean hasOverlay() {
    return this.overlayPanel != null;
  }

  public void openModal(Panel modal) {
    this.modalPanels.push(modal);
    modal.setParent(this);
  }

  public void closeModal() {
    Panel modal = this.modalPanels.poll();
    if (modal != null) {
      modal.setParent(null);
    }
  }

  public boolean hasModal() {
    return !this.modalPanels.isEmpty();
  }

  public ScreenType getScreenType() {
    return this.screenType;
  }

  public void setScreenType(ScreenType type) {
    this.screenType = type;
  }

  public void setSizeProportional(float widthFraction, float heightFraction) {
    Minecraft minecraft = Minecraft.getInstance();
    int screenWidth = minecraft.getWindow().getGuiScaledWidth();
    int screenHeight = minecraft.getWindow().getGuiScaledHeight();
    this.width = (int) (screenWidth * widthFraction);
    this.height = (int) (screenHeight * heightFraction);
    this.posX = (screenWidth - this.width) / 2;
    this.posY = (screenHeight - this.height) / 2;
  }

  public void setSizeCentered(int fixedWidth, int fixedHeight) {
    Minecraft minecraft = Minecraft.getInstance();
    int screenWidth = minecraft.getWindow().getGuiScaledWidth();
    int screenHeight = minecraft.getWindow().getGuiScaledHeight();
    int availableHeight = screenHeight - BREADCRUMB_HEIGHT - BOTTOM_BAR_HEIGHT - 4;
    this.width = Math.min(fixedWidth, screenWidth - 10);
    this.height = Math.min(fixedHeight, availableHeight);
    this.posX = (screenWidth - this.width) / 2;
    this.posY = BREADCRUMB_HEIGHT + 2 + (availableHeight - this.height) / 2;
  }

  public void onScreenInit(int screenWidth, int screenHeight) {
    this.screenWidth = screenWidth;
    this.screenHeight = screenHeight;
    this.setSizeProportional(0.85f, 0.85f);
    this.refreshWidgets();
  }

  private void initOverlays(int screenWidth, int screenHeight) {
    this.breadcrumbBar =
        new BreadcrumbBar(
            0,
            0,
            screenWidth,
            this.breadcrumbAncestors,
            this.breadcrumbLabel,
            BaseScreen::closeAll);
    this.titleBar = new TitleBar(0, 0, 0, this::getPanelTitle, this::closeScreen);
    this.bottomBar =
        new BottomBar(
            0,
            screenHeight - BOTTOM_BAR_HEIGHT,
            screenWidth,
            BOTTOM_BAR_HEIGHT,
            this::onThemeChange);
  }

  private void onThemeChange() {
    ColorPalette.toggle();
    Minecraft minecraft = Minecraft.getInstance();
    this.initOverlays(
        minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    this.refreshWidgets();
  }

  protected abstract Component getTitle();

  protected String getPanelTitle() {
    return this.getTitle().getString();
  }

  @Override
  public int getContentY() {
    return this.getY() + TITLE_BAR_HEIGHT + 2 + this.padding - this.scrollY;
  }

  @Override
  public int getInnerHeight() {
    return this.height - TITLE_BAR_HEIGHT - 2 - this.padding * 2;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);

    for (Panel modal : this.modalPanels) {
      if (modal.isVisible()) {
        graphics.fill(0, 0, this.screenWidth, this.screenHeight, 0x80000000);
        modal.render(graphics, mouseX, mouseY, partialTick);
      }
    }
  }

  protected void renderOverlays(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (this.breadcrumbBar != null) {
      this.breadcrumbBar.render(graphics, mouseX, mouseY, partialTick);
    }
    if (this.bottomBar != null) {
      this.bottomBar.render(graphics, mouseX, mouseY, partialTick);
    }
    if (this.overlayPanel != null && this.overlayPanel.isVisible()) {
      graphics.flush();
      graphics.pose().pushPose();
      graphics.pose().translate(0, 0, OVERLAY_Z);
      this.overlayPanel.render(graphics, mouseX, mouseY, partialTick);
      graphics.pose().popPose();
      graphics.flush();
    }
    this.renderTooltip(graphics, mouseX, mouseY);
  }

  private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    Widget candidate = findDeepestHoveredWithTooltip(this, mouseX, mouseY);
    if (candidate == null) {
      this.tooltipCandidate = null;
      this.tooltipHoverTicks = 0;
      return;
    }

    if (candidate != this.tooltipCandidate) {
      this.tooltipCandidate = candidate;
      this.tooltipHoverTicks = 0;
    }
    this.tooltipHoverTicks++;
    if (this.tooltipHoverTicks >= 10) {
      Tooltip.render(
          graphics,
          Minecraft.getInstance().font,
          candidate.getTooltipText(),
          mouseX,
          mouseY,
          this.screenWidth,
          this.screenHeight);
    }
  }

  @Override
  protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    ColorPalette palette = ColorPalette.current();
    int x = this.getX();
    int y = this.getY();
    graphics.fill(x + 1, y + 1, x + this.width + 1, y + this.height + 1, 0x40000000);
    graphics.fill(x, y, x + this.width, y + this.height, palette.surface());
    drawBorderBevel(graphics, x, y, this.width, this.height, palette.outline());
    if (this.titleBar != null) {
      this.titleBar.setPosition(x, y);
      this.titleBar.setWidth(this.width);
      this.titleBar.render(graphics, mouseX, mouseY, 0f);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.overlayPanel != null && this.overlayPanel.isVisible()) {
      if (this.overlayPanel.isMouseOver(mouseX, mouseY)) {
        return this.overlayPanel.mouseClicked(mouseX, mouseY, button);
      }
      this.closeOverlay();
      return true;
    }

    if (this.breadcrumbBar != null && this.breadcrumbBar.isMouseOver(mouseX, mouseY)) {
      return this.breadcrumbBar.mouseClicked(mouseX, mouseY, button);
    }

    if (this.bottomBar != null
        && this.bottomBar.isMouseOver(mouseX, mouseY)
        && this.bottomBar.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    if (this.titleBar != null && this.titleBar.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    if (this.hasModal()) {
      Panel top = this.modalPanels.peek();
      if (top != null && top.isVisible()) {
        return top.isMouseOver(mouseX, mouseY)
            ? top.mouseClicked(mouseX, mouseY, button)
            : this.closeModalAndReturn();
      }
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (this.overlayPanel != null && this.overlayPanel.isVisible()) {
      if (this.overlayPanel.mouseReleased(mouseX, mouseY, button)) {
        return true;
      }
    }

    if (this.breadcrumbBar != null) {
      this.breadcrumbBar.mouseReleased(mouseX, mouseY, button);
    }
    if (this.titleBar != null) {
      this.titleBar.mouseReleased(mouseX, mouseY, button);
    }
    if (this.bottomBar != null) {
      this.bottomBar.mouseReleased(mouseX, mouseY, button);
    }
    if (this.hasModal()) {
      Panel top = this.modalPanels.peek();
      if (top != null && top.isVisible()) {
        top.mouseReleased(mouseX, mouseY, button);
      }
    }

    return super.mouseReleased(mouseX, mouseY, button);
  }

  private boolean closeModalAndReturn() {
    this.closeModal();
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      if (this.overlayPanel != null) {
        this.closeOverlay();
        return true;
      }
      if (this.hasModal()) {
        this.closeModal();
        return true;
      }
      this.closeScreen();
      return true;
    }

    if (this.overlayPanel != null
        && this.overlayPanel.isVisible()
        && this.overlayPanel.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    }

    Panel top = this.activeModal();
    if (top != null) {
      return top.keyPressed(keyCode, scanCode, modifiers);
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (this.overlayPanel != null
        && this.overlayPanel.isVisible()
        && this.overlayPanel.charTyped(codePoint, modifiers)) {
      return true;
    }
    Panel top = this.activeModal();
    return top != null
        ? top.charTyped(codePoint, modifiers)
        : super.charTyped(codePoint, modifiers);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (this.overlayPanel != null
        && this.overlayPanel.isVisible()
        && this.overlayPanel.isMouseOver(mouseX, mouseY)) {
      return this.overlayPanel.mouseScrolled(mouseX, mouseY, delta);
    }
    Panel top = this.activeModal();
    return top != null && top.isVisible()
        ? top.mouseScrolled(mouseX, mouseY, delta)
        : super.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public void tick() {
    super.tick();
    if (this.overlayPanel != null && this.overlayPanel.isVisible()) {
      this.overlayPanel.tick();
    }
  }

  private Panel activeModal() {
    return this.hasModal() ? this.modalPanels.peek() : null;
  }

  public static class ScreenWrapper extends Screen {

    private final BaseScreen baseScreen;

    protected ScreenWrapper(BaseScreen baseScreen) {
      super(baseScreen.getTitle());
      this.baseScreen = baseScreen;
    }

    @Override
    protected void init() {
      this.baseScreen.screenWidth = this.width;
      this.baseScreen.screenHeight = this.height;
      this.baseScreen.initOverlays(this.width, this.height);
      this.baseScreen.onScreenInit(this.width, this.height);
    }

    @Override
    public void resize(Minecraft minecraft, int width, int height) {
      this.width = width;
      this.height = height;
      this.init();
    }

    @Override
    public void onClose() {
      this.baseScreen.closeScreen();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(graphics);
      this.baseScreen.render(graphics, mouseX, mouseY, partialTick);
      this.baseScreen.renderOverlays(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return this.baseScreen.mouseClicked(mouseX, mouseY, button)
          || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return this.baseScreen.mouseReleased(mouseX, mouseY, button)
          || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
        double mouseX, double mouseY, int button, double dragX, double dragY) {
      return this.baseScreen.mouseDragged(mouseX, mouseY, button, dragX, dragY)
          || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      return this.baseScreen.mouseScrolled(mouseX, mouseY, delta)
          || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.baseScreen.keyPressed(keyCode, scanCode, modifiers)
          || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
      return this.baseScreen.charTyped(codePoint, modifiers)
          || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick() {
      this.baseScreen.tick();
    }

    @Override
    public boolean isPauseScreen() {
      return false;
    }
  }
}
