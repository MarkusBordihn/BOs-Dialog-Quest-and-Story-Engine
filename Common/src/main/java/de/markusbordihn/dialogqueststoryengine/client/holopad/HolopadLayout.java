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

package de.markusbordihn.dialogqueststoryengine.client.holopad;

import de.markusbordihn.dialogqueststoryengine.theme.TextArea;
import de.markusbordihn.dialogqueststoryengine.theme.Theme;
import net.minecraft.resources.ResourceLocation;

public record HolopadLayout(
    int leftPos,
    int topPos,
    int screenWidth,
    int screenHeight,
    ResourceLocation frameTexture,
    ResourceLocation backgroundTexture,
    TextArea textArea,
    boolean showPageNumbers,
    boolean showCloseButton) {

  public static HolopadLayout from(Theme theme, int guiWidth, int guiHeight) {
    TextArea textArea = theme.textArea();
    int screenWidth =
        theme.screenWidth() > 0 ? theme.screenWidth() : textArea.x() * 2 + textArea.width();
    int screenHeight =
        theme.screenHeight() > 0 ? theme.screenHeight() : textArea.y() * 2 + textArea.height();
    int leftPos = (guiWidth - screenWidth) / 2;
    int topPos = (guiHeight - screenHeight) / 2;

    return new HolopadLayout(
        leftPos,
        topPos,
        screenWidth,
        screenHeight,
        theme.frameTexture(),
        theme.backgroundTexture(),
        textArea,
        theme.showPageNumbers(),
        theme.showCloseButton());
  }
}
