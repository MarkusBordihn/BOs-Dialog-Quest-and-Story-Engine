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

public abstract class AbstractButton extends Widget {

  protected boolean hovered;
  protected boolean pressed;

  protected AbstractButton(int posX, int posY, int width, int height) {
    super(posX, posY, width, height);
  }

  protected int resolveBackgroundColor(ColorPalette palette) {
    if (!this.active) {
      return palette.surfaceContainerLow();
    }

    if (this.pressed || this.hovered) {
      return palette.surfaceContainerHigh();
    }

    return palette.surfaceContainer();
  }

  protected int resolveTextColor(ColorPalette palette) {
    return this.active ? palette.onSurface() : palette.onSurfaceLow();
  }

  protected void onPress() {}

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.active && this.visible && button == 0 && this.isMouseOver(mouseX, mouseY)) {
      this.pressed = true;
      return true;
    }

    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (this.pressed && button == 0) {
      this.pressed = false;
      if (this.isMouseOver(mouseX, mouseY)) {
        this.onPress();
      }
      return true;
    }

    return false;
  }
}
