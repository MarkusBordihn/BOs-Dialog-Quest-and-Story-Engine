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

import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;

public final class ChoiceListLayout {

  private ChoiceListLayout() {}

  public static int buttonX(ThemeArea choices, int buttonWidth) {
    return choices.x() + (choices.width() - buttonWidth) / 2;
  }

  public static int buttonY(
      ThemeArea choices, int index, int count, int buttonHeight, int spacing) {
    int totalHeight = count * buttonHeight + Math.max(0, count - 1) * spacing;
    int startY = choices.y() + Math.max(0, (choices.height() - totalHeight) / 2);
    return startY + index * (buttonHeight + spacing);
  }
}
