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
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

public class IconButton extends AbstractButton {

  private ResourceLocation texture;
  private int texU;
  private int texV;
  private int texWidth;
  private int texHeight;
  private Consumer<IconButton> onPressCallback;

  public IconButton(
      int posX,
      int posY,
      int width,
      int height,
      ResourceLocation texture,
      int texU,
      int texV,
      int texWidth,
      int texHeight,
      Consumer<IconButton> onPress) {
    super(posX, posY, width, height);
    this.texture = texture;
    this.texU = texU;
    this.texV = texV;
    this.texWidth = texWidth;
    this.texHeight = texHeight;
    this.onPressCallback = onPress;
  }

  public void setTexture(ResourceLocation texture, int u, int v, int w, int h) {
    this.texture = texture;
    this.texU = u;
    this.texV = v;
    this.texWidth = w;
    this.texHeight = h;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    hovered = isMouseOver(mouseX, mouseY);

    fillRoundedRect(graphics, x, y, width, height, resolveBgColor(palette));
    drawBorderRoundedBevel(graphics, x, y, width, height, palette.outline());

    if (texture != null) {
      int iconX = x + (width - texWidth) / 2;
      int iconY = y + (height - texHeight) / 2;
      graphics.blit(texture, iconX, iconY, texU, texV, texWidth, texHeight);
    }
  }

  @Override
  protected void onPress() {
    if (onPressCallback != null) {
      onPressCallback.accept(this);
    }
  }
}
