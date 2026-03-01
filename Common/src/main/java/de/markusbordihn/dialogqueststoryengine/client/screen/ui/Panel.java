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

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;

public class Panel extends Widget {

  protected final List<Widget> children = new ArrayList<>();
  protected int scrollX;
  protected int scrollY;
  protected int contentHeight;
  protected int contentWidth;
  protected int padding = 4;
  protected boolean clipChildren = true;
  private Widget focusedChild;

  public Panel(int posX, int posY, int width, int height) {
    super(posX, posY, width, height);
  }

  public int getContentX() {
    return getX() + padding - scrollX;
  }

  public int getContentY() {
    return getY() + padding - scrollY;
  }

  public int getInnerWidth() {
    return width - padding * 2;
  }

  public int getInnerHeight() {
    return height - padding * 2;
  }

  public void setPadding(int padding) {
    this.padding = padding;
  }

  public void addWidget(Widget widget) {
    widget.setParent(this);
    children.add(widget);
  }

  public void removeWidget(Widget widget) {
    children.remove(widget);
    widget.setParent(null);
    if (focusedChild == widget) {
      focusedChild = null;
    }
  }

  public void clearWidgets() {
    for (Widget child : children) {
      child.setParent(null);
    }
    children.clear();
    focusedChild = null;
  }

  public List<Widget> getChildren() {
    return children;
  }

  public void refreshWidgets() {
    clearWidgets();
    addWidgets();
    for (Widget child : children) {
      if (child instanceof Panel panel) {
        panel.refreshWidgets();
      }
    }
    alignWidgets();
  }

  protected void addWidgets() {
    // Override in subclasses to populate children
  }

  protected void alignWidgets() {
    // Override in subclasses to layout children
  }

  public void alignVertical(int startY, int spacing) {
    int y = startY;
    for (Widget child : children) {
      if (child.isVisible()) {
        child.posY = y;
        y += child.getHeight() + spacing;
      }
    }
    contentHeight = y;
  }

  public void alignHorizontal(int startX, int spacing) {
    int x = startX;
    for (Widget child : children) {
      if (child.isVisible()) {
        child.posX = x;
        x += child.getWidth() + spacing;
      }
    }
    contentWidth = x;
  }

  public int getScrollX() {
    return scrollX;
  }

  public void setScrollX(int scrollX) {
    this.scrollX = Math.max(0, scrollX);
  }

  public int getScrollY() {
    return scrollY;
  }

  public void setScrollY(int scrollY) {
    this.scrollY = Math.max(0, Math.min(scrollY, getMaxScrollY()));
  }

  public int getMaxScrollY() {
    return Math.max(0, contentHeight - getInnerHeight());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }
    renderBackground(graphics, mouseX, mouseY, partialTick);
    if (clipChildren) {
      enableScissor(graphics);
    }
    for (Widget child : children) {
      if (child.isVisible()) {
        child.render(graphics, mouseX, mouseY, partialTick);
      }
    }
    if (clipChildren) {
      disableScissor(graphics);
    }
    renderForeground(graphics, mouseX, mouseY, partialTick);
  }

  protected void renderBackground(
      GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

  protected void renderForeground(
      GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {}

  protected void enableScissor(GuiGraphics graphics) {
    int x = getX();
    int y = getY();
    graphics.enableScissor(x, y, x + width, y + height);
  }

  protected void disableScissor(GuiGraphics graphics) {
    graphics.disableScissor();
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!visible || !active) {
      return false;
    }
    for (int i = children.size() - 1; i >= 0; i--) {
      Widget child = children.get(i);
      if (child.isVisible() && child.isActive() && child.isMouseOver(mouseX, mouseY)) {
        if (child.mouseClicked(mouseX, mouseY, button)) {
          focusedChild = child;
          return true;
        }
      }
    }
    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (focusedChild != null && focusedChild.mouseReleased(mouseX, mouseY, button)) {
      return true;
    }
    for (int i = children.size() - 1; i >= 0; i--) {
      Widget child = children.get(i);
      if (child.isVisible() && child.isActive() && child.mouseReleased(mouseX, mouseY, button)) {
        return true;
      }
    }
    return false;
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (focusedChild != null) {
      return focusedChild.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }
    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (!visible || !active || !isMouseOver(mouseX, mouseY)) {
      return false;
    }
    for (int i = children.size() - 1; i >= 0; i--) {
      Widget child = children.get(i);
      if (child.isVisible()
          && child.isActive()
          && child.isMouseOver(mouseX, mouseY)
          && child.mouseScrolled(mouseX, mouseY, delta)) {
        return true;
      }
    }
    if (getMaxScrollY() > 0) {
      setScrollY(scrollY - (int) (delta * 10));
      return true;
    }
    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (focusedChild != null && focusedChild.keyPressed(keyCode, scanCode, modifiers)) {
      return true;
    }
    return false;
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (focusedChild != null && focusedChild.charTyped(codePoint, modifiers)) {
      return true;
    }
    return false;
  }

  @Override
  public void tick() {
    for (Widget child : children) {
      child.tick();
    }
  }
}
