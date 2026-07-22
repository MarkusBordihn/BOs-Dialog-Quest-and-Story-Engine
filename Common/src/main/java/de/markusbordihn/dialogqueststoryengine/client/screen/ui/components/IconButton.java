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
  private int textureU;
  private int textureV;
  private int textureWidth;
  private int textureHeight;
  private Consumer<IconButton> onPressCallback;

  public IconButton(
      int posX,
      int posY,
      int width,
      int height,
      ResourceLocation texture,
      int textureU,
      int textureV,
      int textureWidth,
      int textureHeight,
      Consumer<IconButton> onPress) {
    super(posX, posY, width, height);
    this.texture = texture;
    this.textureU = textureU;
    this.textureV = textureV;
    this.textureWidth = textureWidth;
    this.textureHeight = textureHeight;
    this.onPressCallback = onPress;
  }

  public void setTexture(ResourceLocation texture, int u, int v, int width, int height) {
    this.texture = texture;
    this.textureU = u;
    this.textureV = v;
    this.textureWidth = width;
    this.textureHeight = height;
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

    fillRoundedRect(graphics, x, y, this.width, this.height, this.resolveBackgroundColor(palette));
    drawBorderRoundedBevel(graphics, x, y, this.width, this.height, palette.outline());

    if (this.texture != null) {
      int iconX = x + (this.width - this.textureWidth) / 2;
      int iconY = y + (this.height - this.textureHeight) / 2;
      graphics.blit(
          this.texture,
          iconX,
          iconY,
          this.textureU,
          this.textureV,
          this.textureWidth,
          this.textureHeight);
    }
  }

  @Override
  protected void onPress() {
    if (this.onPressCallback != null) {
      this.onPressCallback.accept(this);
    }
  }
}
