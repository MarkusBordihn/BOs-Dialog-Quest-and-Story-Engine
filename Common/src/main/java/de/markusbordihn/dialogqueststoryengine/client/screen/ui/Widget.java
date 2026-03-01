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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorUtils;
import net.minecraft.client.gui.GuiGraphics;

public class Widget {

  protected int posX;
  protected int posY;
  protected int width;
  protected int height;
  protected boolean visible = true;
  protected boolean active = true;
  protected Panel parent;
  protected String tooltipText;

  public Widget(int posX, int posY, int width, int height) {
    this.posX = posX;
    this.posY = posY;
    this.width = width;
    this.height = height;
  }

  protected static void fillRect(GuiGraphics g, int x, int y, int w, int h, int color) {
    g.fill(x, y, x + w, y + h, color);
  }

  protected static void fillRoundedRect(GuiGraphics g, int x, int y, int w, int h, int color) {
    g.fill(x + 2, y + 1, x + w - 2, y + h - 1, color);
    g.fill(x + 1, y + 2, x + w - 1, y + h - 2, color);
  }

  protected static void drawBorder(GuiGraphics g, int x, int y, int w, int h, int color) {
    g.fill(x, y, x + w, y + 1, color);
    g.fill(x, y + h - 1, x + w, y + h, color);
    g.fill(x, y, x + 1, y + h, color);
    g.fill(x + w - 1, y, x + w, y + h, color);
  }

  protected static void drawBorderBevel(GuiGraphics g, int x, int y, int w, int h, int color) {
    int light = lighten(color, 0.30f);
    int dark = darken(color, 0.20f);
    g.fill(x, y, x + w, y + 1, light);
    g.fill(x, y, x + 1, y + h, light);
    g.fill(x, y + h - 1, x + w, y + h, dark);
    g.fill(x + w - 1, y, x + w, y + h, dark);
  }

  protected static void drawBorderRounded(GuiGraphics g, int x, int y, int w, int h, int color) {
    g.fill(x + 2, y, x + w - 2, y + 1, color);
    g.fill(x + 2, y + h - 1, x + w - 2, y + h, color);
    g.fill(x, y + 2, x + 1, y + h - 2, color);
    g.fill(x + w - 1, y + 2, x + w, y + h - 2, color);
    g.fill(x + 1, y + 1, x + 2, y + 2, color);
    g.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
    g.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
    g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, color);
  }

  protected static void drawBorderRoundedBevel(
      GuiGraphics g, int x, int y, int w, int h, int color) {
    int light = lighten(color, 0.30f);
    int dark = darken(color, 0.20f);
    g.fill(x + 2, y, x + w - 2, y + 1, light);
    g.fill(x, y + 2, x + 1, y + h - 2, light);
    g.fill(x + 2, y + h - 1, x + w - 2, y + h, dark);
    g.fill(x + w - 1, y + 2, x + w, y + h - 2, dark);
    g.fill(x + 1, y + 1, x + 2, y + 2, light);
    g.fill(x + w - 2, y + 1, x + w - 1, y + 2, color);
    g.fill(x + 1, y + h - 2, x + 2, y + h - 1, color);
    g.fill(x + w - 2, y + h - 2, x + w - 1, y + h - 1, dark);
  }

  protected static int lighten(int argb, float factor) {
    return ColorUtils.lighten(argb, factor);
  }

  protected static int darken(int argb, float factor) {
    return ColorUtils.darken(argb, factor);
  }

  public int getX() {
    return parent != null ? parent.getContentX() + posX : posX;
  }

  public int getY() {
    return parent != null ? parent.getContentY() + posY : posY;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }

  public void setPosition(int posX, int posY) {
    this.posX = posX;
    this.posY = posY;
  }

  public void setSize(int width, int height) {
    this.width = width;
    this.height = height;
  }

  public boolean isVisible() {
    return visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isActive() {
    return active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getTooltipText() {
    return tooltipText;
  }

  public void setTooltip(String text) {
    this.tooltipText = text;
  }

  public Panel getParent() {
    return parent;
  }

  public void setParent(Panel parent) {
    this.parent = parent;
  }

  public boolean isMouseOver(double mouseX, double mouseY) {
    int x = getX();
    int y = getY();
    return mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
  }

  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return false;
  }

  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    return false;
  }

  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    return false;
  }

  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    return false;
  }

  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    return false;
  }

  public boolean charTyped(char codePoint, int modifiers) {
    return false;
  }

  public void tick() {}
}
