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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public final class Tooltip {

  private static final int HORIZONTAL_OFFSET = 12;
  private static final int VERTICAL_OFFSET = 16;
  private static final int PADDING_HORIZONTAL = 5;
  private static final int PADDING_VERTICAL = 4;

  private Tooltip() {}

  public static void render(
      GuiGraphics graphics,
      Font font,
      String text,
      int mouseX,
      int mouseY,
      int screenWidth,
      int screenHeight) {
    if (text == null || text.isEmpty()) {
      return;
    }

    ColorPalette palette = ColorPalette.current();
    int boxWidth = font.width(text) + PADDING_HORIZONTAL * 2;
    int boxHeight = font.lineHeight + PADDING_VERTICAL * 2;

    int boxX = mouseX + HORIZONTAL_OFFSET;
    int boxY = mouseY + VERTICAL_OFFSET;
    if (boxX + boxWidth > screenWidth - 4) {
      boxX = mouseX - boxWidth - 4;
    }
    if (boxY + boxHeight > screenHeight - 4) {
      boxY = mouseY - boxHeight - 4;
    }
    boxX = Math.max(2, boxX);
    boxY = Math.max(2, boxY);

    graphics.fill(boxX + 2, boxY + 2, boxX + boxWidth + 2, boxY + boxHeight + 2, 0x55000000);
    graphics.fill(boxX, boxY, boxX + boxWidth, boxY + boxHeight, palette.surfaceContainer());
    graphics.fill(boxX, boxY, boxX + boxWidth, boxY + 1, palette.outline());
    graphics.fill(boxX, boxY + boxHeight - 1, boxX + boxWidth, boxY + boxHeight, palette.outline());
    graphics.fill(boxX, boxY, boxX + 1, boxY + boxHeight, palette.outline());
    graphics.fill(boxX + boxWidth - 1, boxY, boxX + boxWidth, boxY + boxHeight, palette.outline());
    graphics.drawString(
        font, text, boxX + PADDING_HORIZONTAL, boxY + PADDING_VERTICAL, palette.onSurface(), false);
  }
}
