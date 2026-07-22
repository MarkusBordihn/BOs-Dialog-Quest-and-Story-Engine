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

package de.markusbordihn.dialogqueststoryengine.data.theme;

import net.minecraft.resources.ResourceLocation;

public record ThemeSprite(
    ResourceLocation texture,
    int textureWidth,
    int textureHeight,
    int u,
    int v,
    int sourceWidth,
    int sourceHeight,
    ThemeSpriteScaling scaling,
    ThemeSpriteBorder border) {

  public ThemeSprite {
    if (texture == null) {
      throw new IllegalArgumentException("ThemeSprite texture must not be null");
    }
    if (scaling == null) {
      throw new IllegalArgumentException("ThemeSprite scaling must not be null");
    }
    if (textureWidth <= 0 || textureHeight <= 0) {
      throw new IllegalArgumentException(
          "ThemeSprite texture size must be > 0, got: " + textureWidth + "x" + textureHeight);
    }
    if (sourceWidth <= 0 || sourceHeight <= 0) {
      throw new IllegalArgumentException(
          "ThemeSprite source size must be > 0, got: " + sourceWidth + "x" + sourceHeight);
    }
    if (u < 0 || v < 0) {
      throw new IllegalArgumentException("ThemeSprite u/v must be >= 0, got: " + u + ", " + v);
    }
    if (u + sourceWidth > textureWidth || v + sourceHeight > textureHeight) {
      throw new IllegalArgumentException(
          "ThemeSprite source region exceeds texture bounds: u="
              + u
              + " v="
              + v
              + " source="
              + sourceWidth
              + "x"
              + sourceHeight
              + " texture="
              + textureWidth
              + "x"
              + textureHeight);
    }

    if (scaling == ThemeSpriteScaling.NINE_SLICE) {
      if (border == null) {
        throw new IllegalArgumentException("Nine-slice sprite requires a border");
      }
      if (border.left() + border.right() >= sourceWidth) {
        throw new IllegalArgumentException(
            "Nine-sliced sprite has no horizontal center slice: left+right="
                + (border.left() + border.right())
                + " >= sourceWidth="
                + sourceWidth);
      }
      if (border.top() + border.bottom() >= sourceHeight) {
        throw new IllegalArgumentException(
            "Nine-sliced sprite has no vertical center slice: top+bottom="
                + (border.top() + border.bottom())
                + " >= sourceHeight="
                + sourceHeight);
      }
    } else if (border == null) {
      border = ThemeSpriteBorder.ZERO;
    }
  }
}
