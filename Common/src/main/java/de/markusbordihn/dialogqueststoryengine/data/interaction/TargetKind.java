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

package de.markusbordihn.dialogqueststoryengine.data.interaction;

import de.markusbordihn.dialogqueststoryengine.data.json.EnumKeys;
import java.util.Locale;

public enum TargetKind {
  ENTITY(0.2f, 1.0f, 0.2f, 0xFF55FF55),
  BLOCK(1.0f, 0.2f, 0.2f, 0xFFFF5555),
  BLOCK_ENTITY(0.2f, 0.6f, 1.0f, 0xFF55AAFF);

  private final float red;
  private final float green;
  private final float blue;
  private final int labelColor;
  private final String key = this.name().toLowerCase(Locale.ROOT);

  TargetKind(float red, float green, float blue, int labelColor) {
    this.red = red;
    this.green = green;
    this.blue = blue;
    this.labelColor = labelColor;
  }

  public static TargetKind fromName(String name) {
    return EnumKeys.byName(TargetKind.class, name).orElse(null);
  }

  public String key() {
    return this.key;
  }

  public float getRed() {
    return this.red;
  }

  public float getGreen() {
    return this.green;
  }

  public float getBlue() {
    return this.blue;
  }

  public int getLabelColor() {
    return this.labelColor;
  }
}
