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

import de.markusbordihn.dialogqueststoryengine.client.screen.MainScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Widget;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class BreadcrumbBar extends Widget {

  private static final float SCALE = ScaledText.SCALE_SMALL;
  private static final String SEPARATOR = " > ";
  private final List<Segment> ancestors;
  private final String currentLabel;
  private final List<int[]> segmentBounds = new ArrayList<>();
  private final HomeButton homeBtn;
  private final CloseButton closeBtn;

  public BreadcrumbBar(
      int posX,
      int posY,
      int width,
      List<Segment> ancestors,
      String currentLabel,
      Runnable closeAllAction) {
    super(posX, posY, width, 16);
    this.ancestors = ancestors != null ? ancestors : List.of();
    this.currentLabel = currentLabel;
    this.homeBtn = new HomeButton(0, 0, MainScreen::open);
    this.closeBtn = new CloseButton(0, 0, closeAllAction);
  }

  @Override
  public int getX() {
    return posX;
  }

  @Override
  public int getY() {
    return posY;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }

    ColorPalette palette = ColorPalette.current();
    Font font = Minecraft.getInstance().font;
    int x = getX();
    int y = getY();

    graphics.fill(x, y, x + width, y + height, (palette.outline() & 0x00FFFFFF) | 0x30000000);
    graphics.fill(x, y + height - 1, x + width, y + height, palette.outline());

    segmentBounds.clear();
    int cursorX = x + 22;
    int textY = y + 4;

    homeBtn.setPosition(x + 2, y + 2);
    homeBtn.render(graphics, mouseX, mouseY, partialTick);

    for (Segment segment : ancestors) {
      int segmentWidth = ScaledText.getScaledWidth(font, segment.label, SCALE);
      boolean hovered =
          mouseX >= cursorX
              && mouseX < cursorX + segmentWidth
              && mouseY >= y
              && mouseY < y + height;

      int color = hovered ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.draw(graphics, font, segment.label, cursorX, textY, color, SCALE);

      if (hovered) {
        int lineY = textY + ScaledText.getScaledHeight(font, SCALE);
        graphics.fill(cursorX, lineY, cursorX + segmentWidth, lineY + 1, color);
      }

      segmentBounds.add(new int[] {cursorX, cursorX + segmentWidth});
      cursorX += segmentWidth;

      int separatorWidth = ScaledText.getScaledWidth(font, SEPARATOR, SCALE);
      ScaledText.draw(graphics, font, SEPARATOR, cursorX, textY, palette.onSurfaceLow(), SCALE);
      cursorX += separatorWidth;
    }

    ScaledText.draw(graphics, font, currentLabel, cursorX, textY, palette.onSurface(), SCALE);

    closeBtn.setPosition(x + width - 16, y + 2);
    closeBtn.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!active || !visible || button != 0 || !isMouseOver(mouseX, mouseY)) {
      return false;
    }

    if (homeBtn.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    if (closeBtn.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    int y = getY();
    for (int i = 0; i < segmentBounds.size() && i < ancestors.size(); i++) {
      int[] bounds = segmentBounds.get(i);
      if (mouseX >= bounds[0] && mouseX < bounds[1] && mouseY >= y && mouseY < y + height) {
        Runnable action = ancestors.get(i).action;
        if (action != null) {
          action.run();
        }
        return true;
      }
    }

    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (homeBtn.mouseReleased(mouseX, mouseY, button)) {
      return true;
    }

    return closeBtn.mouseReleased(mouseX, mouseY, button);
  }

  public record Segment(String label, Runnable action) {}
}
