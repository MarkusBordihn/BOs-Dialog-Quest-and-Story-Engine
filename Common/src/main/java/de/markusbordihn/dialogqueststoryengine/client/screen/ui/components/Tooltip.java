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
  private static final int PADDING_H = 5;
  private static final int PADDING_V = 4;

  private Tooltip() {}

  public static void render(
      GuiGraphics graphics,
      Font font,
      String text,
      int mouseX,
      int mouseY,
      int screenW,
      int screenH) {
    if (text == null || text.isEmpty()) return;

    ColorPalette palette = ColorPalette.current();
    int boxW = font.width(text) + PADDING_H * 2;
    int boxH = font.lineHeight + PADDING_V * 2;

    int bx = mouseX + HORIZONTAL_OFFSET;
    int by = mouseY + VERTICAL_OFFSET;
    if (bx + boxW > screenW - 4) bx = mouseX - boxW - 4;
    if (by + boxH > screenH - 4) by = mouseY - boxH - 4;
    bx = Math.max(2, bx);
    by = Math.max(2, by);

    graphics.fill(bx + 2, by + 2, bx + boxW + 2, by + boxH + 2, 0x55000000);
    graphics.fill(bx, by, bx + boxW, by + boxH, palette.surfaceContainer());
    graphics.fill(bx, by, bx + boxW, by + 1, palette.outline());
    graphics.fill(bx, by + boxH - 1, bx + boxW, by + boxH, palette.outline());
    graphics.fill(bx, by, bx + 1, by + boxH, palette.outline());
    graphics.fill(bx + boxW - 1, by, bx + boxW, by + boxH, palette.outline());
    graphics.drawString(font, text, bx + PADDING_H, by + PADDING_V, palette.onSurface(), false);
  }
}
