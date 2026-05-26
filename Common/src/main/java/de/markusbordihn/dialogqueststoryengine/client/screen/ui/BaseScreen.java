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

      if (child instanceof Panel p) {
        Widget deeper = findDeepestHoveredWithTooltip(p, mouseX, mouseY);
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
    List<BreadcrumbBar.Segment> child = new ArrayList<>(breadcrumbAncestors);
    child.add(
        new BreadcrumbBar.Segment(
            myLabel, () -> Minecraft.getInstance().setScreen(getScreenWrapper())));

    return child;
  }

  public List<BreadcrumbBar.Segment> buildChildAncestors() {
    return buildChildAncestors(breadcrumbLabel);
  }

  public void openScreen() {
    Minecraft minecraft = Minecraft.getInstance();
    previousScreen = minecraft.screen;
    wrapper = new ScreenWrapper(this);
    minecraft.setScreen(wrapper);
  }

  public void closeScreen() {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.setScreen(previousScreen);
  }

  public Screen getScreenWrapper() {
    return wrapper;
  }

  public void openOverlay(Panel overlay) {
    overlayPanel = overlay;
  }

  public void closeOverlay() {
    if (overlayPanel != null) {
      overlayPanel.setParent(null);
      overlayPanel = null;
    }
  }

  public boolean hasOverlay() {
    return overlayPanel != null;
  }

  public void openModal(Panel modal) {
    modalPanels.push(modal);
    modal.setParent(this);
  }

  public void closeModal() {
    Panel modal = modalPanels.poll();
    if (modal != null) {
      modal.setParent(null);
    }
  }

  public boolean hasModal() {
    return !modalPanels.isEmpty();
  }

  public ScreenType getScreenType() {
    return screenType;
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
    setSizeProportional(0.85f, 0.85f);
    refreshWidgets();
  }

  private void initOverlays(int screenWidth, int screenHeight) {
    breadcrumbBar =
        new BreadcrumbBar(
            0, 0, screenWidth, breadcrumbAncestors, breadcrumbLabel, BaseScreen::closeAll);
    titleBar = new TitleBar(0, 0, 0, this::getPanelTitle, this::closeScreen);
    bottomBar =
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
    initOverlays(
        minecraft.getWindow().getGuiScaledWidth(), minecraft.getWindow().getGuiScaledHeight());
    refreshWidgets();
  }

  protected abstract Component getTitle();

  protected String getPanelTitle() {
    return getTitle().getString();
  }

  @Override
  public int getContentY() {
    return getY() + TITLE_BAR_HEIGHT + 2 + padding - scrollY;
  }

  @Override
  public int getInnerHeight() {
    return height - TITLE_BAR_HEIGHT - 2 - padding * 2;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    super.render(graphics, mouseX, mouseY, partialTick);

    // Non-dimming overlay (e.g. open dropdown)
    if (overlayPanel != null && overlayPanel.isVisible()) {
      graphics.flush();
      overlayPanel.render(graphics, mouseX, mouseY, partialTick);
    }

    // Dimmed modal panels
    for (Panel modal : modalPanels) {
      if (modal.isVisible()) {
        graphics.fill(0, 0, screenWidth, screenHeight, 0x80000000);
        modal.render(graphics, mouseX, mouseY, partialTick);
      }
    }
  }

  protected void renderOverlays(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (breadcrumbBar != null) {
      breadcrumbBar.render(graphics, mouseX, mouseY, partialTick);
    }
    if (bottomBar != null) {
      bottomBar.render(graphics, mouseX, mouseY, partialTick);
    }
    renderTooltip(graphics, mouseX, mouseY);
  }

  private void renderTooltip(GuiGraphics graphics, int mouseX, int mouseY) {
    Widget candidate = findDeepestHoveredWithTooltip(this, mouseX, mouseY);
    if (candidate == null) {
      tooltipCandidate = null;
      tooltipHoverTicks = 0;
      return;
    }

    if (candidate != tooltipCandidate) {
      tooltipCandidate = candidate;
      tooltipHoverTicks = 0;
    }
    tooltipHoverTicks++;
    if (tooltipHoverTicks >= 10) {
      Tooltip.render(
          graphics,
          Minecraft.getInstance().font,
          candidate.getTooltipText(),
          mouseX,
          mouseY,
          screenWidth,
          screenHeight);
    }
  }

  @Override
  protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    graphics.fill(x + 1, y + 1, x + width + 1, y + height + 1, 0x40000000);
    graphics.fill(x, y, x + width, y + height, palette.surface());
    drawBorderBevel(graphics, x, y, width, height, palette.outline());
    if (titleBar != null) {
      titleBar.setPosition(x, y);
      titleBar.setWidth(width);
      titleBar.render(graphics, mouseX, mouseY, 0f);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (overlayPanel != null && overlayPanel.isVisible()) {
      if (overlayPanel.isMouseOver(mouseX, mouseY)) {
        return overlayPanel.mouseClicked(mouseX, mouseY, button);
      }
      closeOverlay();
      return true;
    }

    if (breadcrumbBar != null && breadcrumbBar.isMouseOver(mouseX, mouseY)) {
      return breadcrumbBar.mouseClicked(mouseX, mouseY, button);
    }

    if (bottomBar != null
        && bottomBar.isMouseOver(mouseX, mouseY)
        && bottomBar.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    if (titleBar != null && titleBar.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    if (hasModal()) {
      Panel top = modalPanels.peek();
      if (top != null && top.isVisible()) {
        return top.isMouseOver(mouseX, mouseY)
            ? top.mouseClicked(mouseX, mouseY, button)
            : closeModalAndReturn();
      }
    }

    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (overlayPanel != null && overlayPanel.isVisible()) {
      if (overlayPanel.mouseReleased(mouseX, mouseY, button)) {
        return true;
      }
    }

    if (breadcrumbBar != null) {
      breadcrumbBar.mouseReleased(mouseX, mouseY, button);
    }
    if (titleBar != null) {
      titleBar.mouseReleased(mouseX, mouseY, button);
    }
    if (bottomBar != null) {
      bottomBar.mouseReleased(mouseX, mouseY, button);
    }
    if (hasModal()) {
      Panel top = modalPanels.peek();
      if (top != null && top.isVisible()) {
        top.mouseReleased(mouseX, mouseY, button);
      }
    }

    return super.mouseReleased(mouseX, mouseY, button);
  }

  private boolean closeModalAndReturn() {
    closeModal();
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      if (hasModal()) {
        closeModal();
        return true;
      }
      closeScreen();
      return true;
    }

    Panel top = activeModal();
    if (top != null) {
      return top.keyPressed(keyCode, scanCode, modifiers);
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    Panel top = activeModal();
    return top != null
        ? top.charTyped(codePoint, modifiers)
        : super.charTyped(codePoint, modifiers);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    Panel top = activeModal();
    return top != null && top.isVisible()
        ? top.mouseScrolled(mouseX, mouseY, delta)
        : super.mouseScrolled(mouseX, mouseY, delta);
  }

  private Panel activeModal() {
    return hasModal() ? modalPanels.peek() : null;
  }

  public static class ScreenWrapper extends Screen {

    private final BaseScreen baseScreen;

    protected ScreenWrapper(BaseScreen baseScreen) {
      super(baseScreen.getTitle());
      this.baseScreen = baseScreen;
    }

    @Override
    protected void init() {
      baseScreen.screenWidth = this.width;
      baseScreen.screenHeight = this.height;
      baseScreen.initOverlays(this.width, this.height);
      baseScreen.onScreenInit(this.width, this.height);
    }

    @Override
    public void resize(net.minecraft.client.Minecraft minecraft, int width, int height) {
      this.width = width;
      this.height = height;
      this.init();
    }

    @Override
    public void onClose() {
      baseScreen.closeScreen();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      super.renderBackground(graphics);
      baseScreen.render(graphics, mouseX, mouseY, partialTick);
      baseScreen.renderOverlays(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
      return baseScreen.mouseClicked(mouseX, mouseY, button)
          || super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
      return baseScreen.mouseReleased(mouseX, mouseY, button)
          || super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(
        double mouseX, double mouseY, int button, double dragX, double dragY) {
      return baseScreen.mouseDragged(mouseX, mouseY, button, dragX, dragY)
          || super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      return baseScreen.mouseScrolled(mouseX, mouseY, delta)
          || super.mouseScrolled(mouseX, mouseY, delta);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return baseScreen.keyPressed(keyCode, scanCode, modifiers)
          || super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
      return baseScreen.charTyped(codePoint, modifiers) || super.charTyped(codePoint, modifiers);
    }

    @Override
    public void tick() {
      baseScreen.tick();
    }

    @Override
    public boolean isPauseScreen() {
      return false;
    }
  }
}
