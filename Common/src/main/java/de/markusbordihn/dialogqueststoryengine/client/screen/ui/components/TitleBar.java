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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Widget;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class TitleBar extends Widget {

  public static final int HEIGHT = 16;

  private final Supplier<String> titleGetter;
  private final CloseButton closeButton;

  public TitleBar(int posX, int posY, int width, Supplier<String> titleGetter, Runnable onClose) {
    super(posX, posY, width, HEIGHT);
    this.titleGetter = titleGetter;
    this.closeButton = new CloseButton(0, 0, onClose);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }

    ColorPalette palette = ColorPalette.current();
    int x = this.getX();
    int y = this.getY();

    graphics.fill(x + 1, y + 1, x + this.width - 1, y + HEIGHT, palette.titleBar());
    graphics.fill(x + 1, y + HEIGHT, x + this.width - 1, y + HEIGHT + 1, palette.outline());

    Font font = Minecraft.getInstance().font;
    ScaledText.draw(
        graphics,
        font,
        this.titleGetter.get(),
        x + 6,
        y + 4,
        palette.onTitleBar(),
        ScaledText.SCALE_SMALL);

    this.closeButton.setPosition(x + this.width - 15, y + 2);
    this.closeButton.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return this.closeButton.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    return this.closeButton.mouseReleased(mouseX, mouseY, button);
  }
}
