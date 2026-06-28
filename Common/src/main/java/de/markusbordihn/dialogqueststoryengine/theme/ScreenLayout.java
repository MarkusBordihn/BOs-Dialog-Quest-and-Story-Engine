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

package de.markusbordihn.dialogqueststoryengine.theme;

import net.minecraft.resources.ResourceLocation;

public record ScreenLayout(
    int leftPos,
    int topPos,
    int screenWidth,
    int screenHeight,
    ResourceLocation frameTexture,
    ResourceLocation backgroundTexture,
    ThemeArea displayArea,
    ThemeArea titleArea,
    ThemeTextAlignment titleAlignment,
    ThemeArea textArea,
    ThemeArea choiceArea,
    boolean showPageNumbers,
    boolean showCloseButton) {

  public static ScreenLayout from(Theme theme, int guiWidth, int guiHeight) {
    int screenWidth =
        theme.screenWidth() > 0
            ? theme.screenWidth()
            : maxRight(
                theme.displayArea(), theme.titleArea(), theme.textArea(), theme.choiceArea());
    int screenHeight =
        theme.screenHeight() > 0
            ? theme.screenHeight()
            : maxBottom(
                theme.displayArea(), theme.titleArea(), theme.textArea(), theme.choiceArea());
    int leftPos = (guiWidth - screenWidth) / 2;
    int topPos = (guiHeight - screenHeight) / 2;

    return new ScreenLayout(
        leftPos,
        topPos,
        screenWidth,
        screenHeight,
        theme.frameTexture(),
        theme.backgroundTexture(),
        theme.displayArea(),
        theme.titleArea(),
        theme.titleAlignment(),
        theme.textArea(),
        theme.choiceArea(),
        theme.showPageNumbers(),
        theme.showCloseButton());
  }

  private static int maxRight(ThemeArea... areas) {
    int right = 0;
    for (ThemeArea area : areas) {
      right = Math.max(right, area.right());
    }
    return right;
  }

  private static int maxBottom(ThemeArea... areas) {
    int bottom = 0;
    for (ThemeArea area : areas) {
      bottom = Math.max(bottom, area.bottom());
    }
    return bottom;
  }

  public int titleX(int contentWidth) {
    int areaX = leftPos + titleArea.x();
    return switch (titleAlignment) {
      case CENTER -> areaX + (titleArea.width() - contentWidth) / 2;
      case RIGHT -> areaX + titleArea.width() - contentWidth;
      case LEFT -> areaX;
    };
  }

  public int titleY(int lineHeight) {
    return topPos + titleArea.y() + (titleArea.height() - lineHeight) / 2;
  }

  public int choiceButtonWidth() {
    return choiceArea.width();
  }

  public int choiceButtonX(int buttonWidth) {
    return leftPos + choiceArea.x() + (choiceArea.width() - buttonWidth) / 2;
  }

  public int choiceButtonY(int index, int count, int buttonHeight, int spacing) {
    int totalHeight = count * buttonHeight + Math.max(0, count - 1) * spacing;
    int startY = topPos + choiceArea.y() + Math.max(0, (choiceArea.height() - totalHeight) / 2);
    return startY + index * (buttonHeight + spacing);
  }
}
