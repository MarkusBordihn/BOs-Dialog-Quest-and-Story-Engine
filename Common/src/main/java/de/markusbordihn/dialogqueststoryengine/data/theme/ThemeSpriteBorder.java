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

public record ThemeSpriteBorder(int left, int top, int right, int bottom) {

  public static final ThemeSpriteBorder ZERO = new ThemeSpriteBorder(0, 0, 0, 0);

  public ThemeSpriteBorder {
    if (left < 0 || top < 0 || right < 0 || bottom < 0) {
      throw new IllegalArgumentException(
          "ThemeSpriteBorder values must be >= 0, got: "
              + left
              + ", "
              + top
              + ", "
              + right
              + ", "
              + bottom);
    }
  }

  public static ThemeSpriteBorder all(int value) {
    return new ThemeSpriteBorder(value, value, value, value);
  }

  public boolean isEmpty() {
    return this.left == 0 && this.top == 0 && this.right == 0 && this.bottom == 0;
  }
}
