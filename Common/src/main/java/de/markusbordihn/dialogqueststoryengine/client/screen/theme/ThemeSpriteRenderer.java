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

package de.markusbordihn.dialogqueststoryengine.client.screen.theme;

import de.markusbordihn.dialogqueststoryengine.data.theme.NineSliceGeometry;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSprite;
import net.minecraft.client.gui.GuiGraphics;

public final class ThemeSpriteRenderer {

  private ThemeSpriteRenderer() {}

  public static void render(
      GuiGraphics graphics, ThemeSprite sprite, int x, int y, int width, int height) {
    if (width <= 0 || height <= 0) {
      return;
    }
    switch (sprite.scaling()) {
      case STRETCH -> stretch(graphics, sprite, x, y, width, height);
      case TILE -> tile(graphics, sprite, x, y, width, height);
      case NINE_SLICE -> nineSlice(graphics, sprite, x, y, width, height);
    }
  }

  private static void stretch(
      GuiGraphics graphics, ThemeSprite sprite, int x, int y, int width, int height) {
    graphics.blit(
        sprite.texture(),
        x,
        y,
        width,
        height,
        sprite.u(),
        sprite.v(),
        sprite.sourceWidth(),
        sprite.sourceHeight(),
        sprite.textureWidth(),
        sprite.textureHeight());
  }

  private static void tile(
      GuiGraphics graphics, ThemeSprite sprite, int x, int y, int width, int height) {
    for (int offsetY = 0; offsetY < height; offsetY += sprite.sourceHeight()) {
      int tileHeight = Math.min(sprite.sourceHeight(), height - offsetY);
      for (int offsetX = 0; offsetX < width; offsetX += sprite.sourceWidth()) {
        int tileWidth = Math.min(sprite.sourceWidth(), width - offsetX);
        graphics.blit(
            sprite.texture(),
            x + offsetX,
            y + offsetY,
            tileWidth,
            tileHeight,
            sprite.u(),
            sprite.v(),
            tileWidth,
            tileHeight,
            sprite.textureWidth(),
            sprite.textureHeight());
      }
    }
  }

  private static void nineSlice(
      GuiGraphics graphics, ThemeSprite sprite, int x, int y, int width, int height) {
    for (NineSliceGeometry.Region region :
        NineSliceGeometry.regions(
            sprite.sourceWidth(), sprite.sourceHeight(), sprite.border(), width, height)) {
      blitRegion(graphics, sprite, region, x, y);
    }
  }

  private static void blitRegion(
      GuiGraphics graphics, ThemeSprite sprite, NineSliceGeometry.Region region, int x, int y) {
    int destX = x + region.destinationX();
    int destY = y + region.destinationY();
    int srcU = sprite.u() + region.sourceX();
    int srcV = sprite.v() + region.sourceY();

    if (!region.tileX() && !region.tileY()) {
      graphics.blit(
          sprite.texture(),
          destX,
          destY,
          region.destinationWidth(),
          region.destinationHeight(),
          srcU,
          srcV,
          region.sourceWidth(),
          region.sourceHeight(),
          sprite.textureWidth(),
          sprite.textureHeight());
      return;
    }

    int stepX = region.tileX() ? region.sourceWidth() : region.destinationWidth();
    int stepY = region.tileY() ? region.sourceHeight() : region.destinationHeight();
    for (int offsetY = 0; offsetY < region.destinationHeight(); offsetY += stepY) {
      int tileHeight = Math.min(stepY, region.destinationHeight() - offsetY);
      int sourceHeight = region.tileY() ? tileHeight : region.sourceHeight();
      for (int offsetX = 0; offsetX < region.destinationWidth(); offsetX += stepX) {
        int tileWidth = Math.min(stepX, region.destinationWidth() - offsetX);
        int sourceWidth = region.tileX() ? tileWidth : region.sourceWidth();
        graphics.blit(
            sprite.texture(),
            destX + offsetX,
            destY + offsetY,
            tileWidth,
            tileHeight,
            srcU,
            srcV,
            sourceWidth,
            sourceHeight,
            sprite.textureWidth(),
            sprite.textureHeight());
      }
    }
  }
}
