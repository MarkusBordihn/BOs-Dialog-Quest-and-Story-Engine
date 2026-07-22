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

public record ThemeViewport(
    int logicalWidth,
    int logicalHeight,
    float scale,
    int originX,
    int originY,
    int guiWidth,
    int guiHeight) {

  public static ThemeViewport of(
      int logicalWidth,
      int logicalHeight,
      int guiWidth,
      int guiHeight,
      ThemeScaleLimits limits,
      ThemeAnchor anchor) {
    float scale = selectScale(logicalWidth, logicalHeight, guiWidth, guiHeight, limits);
    int scaledWidth = Math.round(logicalWidth * scale);
    int scaledHeight = Math.round(logicalHeight * scale);
    int originX = anchorX(anchor, guiWidth, scaledWidth);
    int originY = anchorY(anchor, guiHeight, scaledHeight);
    return new ThemeViewport(
        logicalWidth, logicalHeight, scale, originX, originY, guiWidth, guiHeight);
  }

  static float selectScale(
      int logicalWidth, int logicalHeight, int guiWidth, int guiHeight, ThemeScaleLimits limits) {
    float fit = Math.min((float) guiWidth / logicalWidth, (float) guiHeight / logicalHeight);
    float scale;
    if (fit >= 1.0f) {
      scale = (float) Math.floor(fit);
    } else {
      scale = (float) Math.floor(fit * 4.0f) / 4.0f;
    }
    // The minimum limit must never force overflow: it can pull the scale up only to the point that
    // still fits (min(limit.min, fit)), with an absolute floor so extremely small windows stay
    // rendered rather than collapsing to zero.
    float low = Math.max(0.25f, Math.min(limits.min(), fit));
    scale = Math.max(low, Math.min(scale, limits.max()));
    return scale;
  }

  private static int anchorX(ThemeAnchor anchor, int guiWidth, int scaledWidth) {
    return switch (anchor.horizontal()) {
      case LEFT -> 0;
      case RIGHT -> guiWidth - scaledWidth;
      case CENTER -> (guiWidth - scaledWidth) / 2;
    };
  }

  private static int anchorY(ThemeAnchor anchor, int guiHeight, int scaledHeight) {
    return switch (anchor.vertical()) {
      case TOP -> 0;
      case BOTTOM -> guiHeight - scaledHeight;
      case MIDDLE -> (guiHeight - scaledHeight) / 2;
    };
  }

  public float toScreenX(float logicalX) {
    return this.originX + logicalX * this.scale;
  }

  public float toScreenY(float logicalY) {
    return this.originY + logicalY * this.scale;
  }

  public double toLogicalX(double screenX) {
    return (screenX - this.originX) / this.scale;
  }

  public double toLogicalY(double screenY) {
    return (screenY - this.originY) / this.scale;
  }

  public int scaledWidth() {
    return Math.round(this.logicalWidth * this.scale);
  }

  public int scaledHeight() {
    return Math.round(this.logicalHeight * this.scale);
  }

  public ScreenRect toScreenRect(ThemeArea area) {
    int x = Math.round(this.toScreenX(area.x()));
    int y = Math.round(this.toScreenY(area.y()));
    int right = Math.round(this.toScreenX(area.right()));
    int bottom = Math.round(this.toScreenY(area.bottom()));
    return new ScreenRect(x, y, right - x, bottom - y);
  }

  public record ScreenRect(int x, int y, int width, int height) {}
}
