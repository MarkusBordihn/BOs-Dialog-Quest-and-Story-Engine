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

public record ColorPalette(
    int primary,
    int primaryVariant,
    int surface,
    int surfaceContainer,
    int background,
    int outline,
    int onPrimary,
    int onSurface,
    int onSurfaceLow,
    int onBackground,
    int error,
    int onError,
    int titleBar,
    int onTitleBar,
    boolean dark) {

  public static final ColorPalette DARK =
      new ColorPalette(
          /* primary          */ 0xFF4FC3F7,
          /* primaryVariant   */ 0xFF0288D1,
          /* surface          */ 0xCC1E1E22,
          /* surfaceContainer */ 0xFF2A2A2E,
          /* background       */ 0xFF121214,
          /* outline          */ 0xFF3D3D42,
          /* onPrimary        */ 0xFF000000,
          /* onSurface        */ 0xFFE8E8EC,
          /* onSurfaceLow     */ 0xFF8A8A94,
          /* onBackground     */ 0xFFD0D0D8,
          /* error            */ 0xFFE81123,
          /* onError          */ 0xFFFFFFFF,
          /* titleBar         */ 0xFF1E1E2E,
          /* onTitleBar       */ 0xFFE8E8EC,
          /* dark             */ true);

  public static final ColorPalette LIGHT =
      new ColorPalette(
          /* primary          */ 0xFF1565C0,
          /* primaryVariant   */ 0xFF0D47A1,
          /* surface          */ 0xCCF5F5F5,
          /* surfaceContainer */ 0xFFE8E8EC,
          /* background       */ 0xFFEEEEF2,
          /* outline          */ 0xFFBBBBBF,
          /* onPrimary        */ 0xFFFFFFFF,
          /* onSurface        */ 0xFF1A1A1E,
          /* onSurfaceLow     */ 0xFF5A5A64,
          /* onBackground     */ 0xFF2A2A32,
          /* error            */ 0xFFE81123,
          /* onError          */ 0xFFFFFFFF,
          /* titleBar         */ 0xFFB0BBBF,
          /* onTitleBar       */ 0xFF1A1A1E,
          /* dark             */ false);

  private static ColorPalette active = DARK;

  public static ColorPalette current() {
    return active;
  }

  public static void set(ColorPalette palette) {
    active = palette;
  }

  public static void toggle() {
    active = (active == DARK) ? LIGHT : DARK;
  }

  public static boolean isDark() {
    return active == DARK;
  }

  static int lighten(int argb, float factor) {
    return ColorUtils.lighten(argb, factor);
  }

  static int darken(int argb, float factor) {
    return ColorUtils.darken(argb, factor);
  }

  public int surfaceContainerHigh() {
    return this.dark ? lighten(this.surfaceContainer, 0.30f) : darken(this.surfaceContainer, 0.12f);
  }

  public int surfaceContainerLow() {
    return darken(this.surfaceContainer, 0.35f);
  }

  public int scrollTrack() {
    return (this.outline & 0x00FFFFFF) | 0x60000000;
  }

  public int scrollThumb() {
    return this.outline;
  }

  public int listHighlight() {
    return (this.primaryVariant & 0x00FFFFFF) | 0x60000000;
  }

  public int listHover() {
    return (this.primary & 0x00FFFFFF) | 0x20000000;
  }

  public int listStripe() {
    return (this.primary & 0x00FFFFFF) | 0x10000000;
  }
}
