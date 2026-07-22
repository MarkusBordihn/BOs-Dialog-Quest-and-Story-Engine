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

  private static final float BEVEL_LIGHTEN = 0.30f;
  private static final float BEVEL_DARKEN = 0.20f;

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

  protected static void fillRect(
      GuiGraphics graphics, int x, int y, int width, int height, int color) {
    graphics.fill(x, y, x + width, y + height, color);
  }

  protected static void fillRoundedRect(
      GuiGraphics graphics, int x, int y, int width, int height, int color) {
    graphics.fill(x + 2, y + 1, x + width - 2, y + height - 1, color);
    graphics.fill(x + 1, y + 2, x + width - 1, y + height - 2, color);
  }

  protected static void drawBorder(
      GuiGraphics graphics, int x, int y, int width, int height, int color) {
    graphics.fill(x, y, x + width, y + 1, color);
    graphics.fill(x, y + height - 1, x + width, y + height, color);
    graphics.fill(x, y, x + 1, y + height, color);
    graphics.fill(x + width - 1, y, x + width, y + height, color);
  }

  protected static void drawBorderBevel(
      GuiGraphics graphics, int x, int y, int width, int height, int color) {
    int light = lighten(color, BEVEL_LIGHTEN);
    int dark = darken(color, BEVEL_DARKEN);
    graphics.fill(x, y, x + width, y + 1, light);
    graphics.fill(x, y, x + 1, y + height, light);
    graphics.fill(x, y + height - 1, x + width, y + height, dark);
    graphics.fill(x + width - 1, y, x + width, y + height, dark);
  }

  protected static void drawBorderRounded(
      GuiGraphics graphics, int x, int y, int width, int height, int color) {
    graphics.fill(x + 2, y, x + width - 2, y + 1, color);
    graphics.fill(x + 2, y + height - 1, x + width - 2, y + height, color);
    graphics.fill(x, y + 2, x + 1, y + height - 2, color);
    graphics.fill(x + width - 1, y + 2, x + width, y + height - 2, color);
    graphics.fill(x + 1, y + 1, x + 2, y + 2, color);
    graphics.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
    graphics.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
    graphics.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, color);
  }

  protected static void drawBorderRoundedBevel(
      GuiGraphics graphics, int x, int y, int width, int height, int color) {
    int light = lighten(color, BEVEL_LIGHTEN);
    int dark = darken(color, BEVEL_DARKEN);
    graphics.fill(x + 2, y, x + width - 2, y + 1, light);
    graphics.fill(x, y + 2, x + 1, y + height - 2, light);
    graphics.fill(x + 2, y + height - 1, x + width - 2, y + height, dark);
    graphics.fill(x + width - 1, y + 2, x + width, y + height - 2, dark);
    graphics.fill(x + 1, y + 1, x + 2, y + 2, light);
    graphics.fill(x + width - 2, y + 1, x + width - 1, y + 2, color);
    graphics.fill(x + 1, y + height - 2, x + 2, y + height - 1, color);
    graphics.fill(x + width - 2, y + height - 2, x + width - 1, y + height - 1, dark);
  }

  protected static int lighten(int argb, float factor) {
    return ColorUtils.lighten(argb, factor);
  }

  protected static int darken(int argb, float factor) {
    return ColorUtils.darken(argb, factor);
  }

  public int getX() {
    return this.parent != null ? this.parent.getContentX() + this.posX : this.posX;
  }

  public int getY() {
    return this.parent != null ? this.parent.getContentY() + this.posY : this.posY;
  }

  public int getWidth() {
    return this.width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return this.height;
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
    return this.visible;
  }

  public void setVisible(boolean visible) {
    this.visible = visible;
  }

  public boolean isActive() {
    return this.active;
  }

  public void setActive(boolean active) {
    this.active = active;
  }

  public String getTooltipText() {
    return this.tooltipText;
  }

  public void setTooltip(String text) {
    this.tooltipText = text;
  }

  public Panel getParent() {
    return this.parent;
  }

  public void setParent(Panel parent) {
    this.parent = parent;
  }

  public boolean isMouseOver(double mouseX, double mouseY) {
    int x = this.getX();
    int y = this.getY();
    return mouseX >= x && mouseX < x + this.width && mouseY >= y && mouseY < y + this.height;
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
