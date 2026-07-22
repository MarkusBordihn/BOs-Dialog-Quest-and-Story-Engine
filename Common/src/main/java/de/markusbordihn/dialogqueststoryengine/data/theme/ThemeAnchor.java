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

import de.markusbordihn.dialogqueststoryengine.data.json.EnumKeys;
import java.util.Optional;

public enum ThemeAnchor {
  CENTER(Horizontal.CENTER, Vertical.MIDDLE),
  TOP(Horizontal.CENTER, Vertical.TOP),
  BOTTOM(Horizontal.CENTER, Vertical.BOTTOM),
  LEFT(Horizontal.LEFT, Vertical.MIDDLE),
  RIGHT(Horizontal.RIGHT, Vertical.MIDDLE),
  TOP_LEFT(Horizontal.LEFT, Vertical.TOP),
  TOP_RIGHT(Horizontal.RIGHT, Vertical.TOP),
  BOTTOM_LEFT(Horizontal.LEFT, Vertical.BOTTOM),
  BOTTOM_RIGHT(Horizontal.RIGHT, Vertical.BOTTOM);

  private final Horizontal horizontal;
  private final Vertical vertical;

  ThemeAnchor(Horizontal horizontal, Vertical vertical) {
    this.horizontal = horizontal;
    this.vertical = vertical;
  }

  public static Optional<ThemeAnchor> fromKey(String key) {
    return EnumKeys.byName(ThemeAnchor.class, key);
  }

  public Horizontal horizontal() {
    return this.horizontal;
  }

  public Vertical vertical() {
    return this.vertical;
  }

  public enum Horizontal {
    LEFT,
    CENTER,
    RIGHT
  }

  public enum Vertical {
    TOP,
    MIDDLE,
    BOTTOM
  }
}
