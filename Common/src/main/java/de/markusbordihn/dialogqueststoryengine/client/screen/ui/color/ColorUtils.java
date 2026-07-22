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

package de.markusbordihn.dialogqueststoryengine.client.screen.ui.color;

public final class ColorUtils {

  private ColorUtils() {}

  public static int lighten(int argb, float factor) {
    int a = (argb >> 24) & 0xFF;
    int r = Math.min(255, (int) (((argb >> 16) & 0xFF) * (1f + factor)));
    int g = Math.min(255, (int) (((argb >> 8) & 0xFF) * (1f + factor)));
    int b = Math.min(255, (int) ((argb & 0xFF) * (1f + factor)));
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  public static int darken(int argb, float factor) {
    int a = (argb >> 24) & 0xFF;
    int r = Math.max(0, (int) (((argb >> 16) & 0xFF) * (1f - factor)));
    int g = Math.max(0, (int) (((argb >> 8) & 0xFF) * (1f - factor)));
    int b = Math.max(0, (int) ((argb & 0xFF) * (1f - factor)));
    return (a << 24) | (r << 16) | (g << 8) | b;
  }

  public static int blend(int background, int foreground) {
    int foregroundAlpha = (foreground >> 24) & 0xFF;
    if (foregroundAlpha == 0xFF) {
      return foreground;
    }

    if (foregroundAlpha == 0x00) {
      return background;
    }

    float alpha = foregroundAlpha / 255f;
    int r =
        (int) (((foreground >> 16) & 0xFF) * alpha + ((background >> 16) & 0xFF) * (1f - alpha));
    int g = (int) (((foreground >> 8) & 0xFF) * alpha + ((background >> 8) & 0xFF) * (1f - alpha));
    int b = (int) ((foreground & 0xFF) * alpha + (background & 0xFF) * (1f - alpha));

    return 0xFF000000 | (r << 16) | (g << 8) | b;
  }

  public static int withAlpha(int argb, int alpha) {
    return (argb & 0x00FFFFFF) | ((alpha & 0xFF) << 24);
  }
}
