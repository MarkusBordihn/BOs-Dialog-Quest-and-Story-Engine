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

package de.markusbordihn.dialogqueststoryengine.client.screen.ui.components;

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Panel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import net.minecraft.client.gui.GuiGraphics;

public class ScrollPanel extends Panel {

  private int scrollBarWidth = 6;
  private boolean draggingScrollBar;
  private double dragStartY;
  private int dragStartScroll;

  public ScrollPanel(int posX, int posY, int width, int height) {
    super(posX, posY, width, height);
  }

  @Override
  protected void renderForeground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    int maxScroll = getMaxScrollY();
    if (maxScroll <= 0) {
      return;
    }
    ColorPalette palette = ColorPalette.current();
    int x = getX() + width - scrollBarWidth;
    int y = getY();

    graphics.fill(x, y, x + scrollBarWidth, y + height, palette.scrollTrack());

    int totalTrack = height;
    int handleHeight = Math.max(15, (int) ((float) height / (height + maxScroll) * totalTrack));
    float scrollFraction = (float) scrollY / maxScroll;
    int handleY = y + (int) (scrollFraction * (totalTrack - handleHeight));

    graphics.fill(
        x + 1, handleY, x + scrollBarWidth - 1, handleY + handleHeight, palette.scrollThumb());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (button == 0 && isMouseOverScrollBar(mouseX, mouseY)) {
      draggingScrollBar = true;
      dragStartY = mouseY;
      dragStartScroll = scrollY;
      return true;
    }
    return super.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (draggingScrollBar) {
      int maxScroll = getMaxScrollY();
      if (maxScroll > 0) {
        double deltaY = mouseY - dragStartY;
        int totalTrack = height;
        int handleHeight = Math.max(15, (int) ((float) height / (height + maxScroll) * totalTrack));
        float scrollPerPixel = (float) maxScroll / (totalTrack - handleHeight);
        setScrollY(dragStartScroll + (int) (deltaY * scrollPerPixel));
      }
      return true;
    }
    return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (draggingScrollBar) {
      draggingScrollBar = false;
      return true;
    }
    return super.mouseReleased(mouseX, mouseY, button);
  }

  private boolean isMouseOverScrollBar(double mouseX, double mouseY) {
    int barX = getX() + width - scrollBarWidth;
    int barY = getY();
    return mouseX >= barX
        && mouseX < barX + scrollBarWidth
        && mouseY >= barY
        && mouseY < barY + height;
  }
}
