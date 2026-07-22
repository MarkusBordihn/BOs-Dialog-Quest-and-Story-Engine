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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class HomeButton extends AbstractButton {

  private static final String HOME_ICON = "\u2302";

  private final Runnable onHomeAction;

  public HomeButton(int posX, int posY, Runnable onHomeAction) {
    super(posX, posY, 14, 12);
    this.onHomeAction = onHomeAction;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }

    ColorPalette palette = ColorPalette.current();
    int x = this.getX();
    int y = this.getY();
    this.hovered = this.isMouseOver(mouseX, mouseY);

    int backgroundColor = this.hovered ? palette.primaryVariant() : palette.surfaceContainer();
    fillRoundedRect(graphics, x, y, this.width, this.height, backgroundColor);
    drawBorderRounded(
        graphics,
        x,
        y,
        this.width,
        this.height,
        this.hovered ? palette.primary() : palette.outline());

    Font font = Minecraft.getInstance().font;
    int textColor = this.hovered ? palette.onPrimary() : palette.onSurface();
    ScaledText.drawCentered(
        graphics,
        font,
        HOME_ICON,
        x + this.width / 2 + 1,
        y + 3,
        textColor,
        ScaledText.SCALE_NORMAL);
  }

  @Override
  protected void onPress() {
    if (this.onHomeAction != null) {
      this.onHomeAction.run();
    }
  }
}
