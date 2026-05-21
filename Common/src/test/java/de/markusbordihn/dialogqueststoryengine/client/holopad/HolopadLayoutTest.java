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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.theme.TextArea;
import de.markusbordihn.dialogqueststoryengine.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeLayout;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class HolopadLayoutTest {

  private static final ResourceLocation FRAME_TEX = new ResourceLocation("test", "gui/frame.png");
  private static final ResourceLocation BG_TEX = new ResourceLocation("test", "gui/background.png");

  private Theme buildTheme(int screenWidth, int screenHeight, TextArea textArea) {
    return new Theme(
        UUID.randomUUID(),
        new ResourceLocation("test", "theme"),
        1,
        ThemeLayout.HOLOPAD,
        FRAME_TEX,
        BG_TEX,
        true,
        true,
        textArea,
        screenWidth,
        screenHeight);
  }

  @Test
  void explicitScreenDimensionsAreUsed() {
    Theme theme = buildTheme(370, 250, new TextArea(12, 118, 338, 120));
    HolopadLayout layout = HolopadLayout.from(theme, 800, 600);
    assertEquals(370, layout.screenWidth());
    assertEquals(250, layout.screenHeight());
  }

  @Test
  void zeroDimensionsFallsBackToDerivedFormula() {
    TextArea textArea = new TextArea(24, 32, 176, 120);
    Theme theme = buildTheme(0, 0, textArea);
    HolopadLayout layout = HolopadLayout.from(theme, 800, 600);
    assertEquals(24 * 2 + 176, layout.screenWidth());
    assertEquals(32 * 2 + 120, layout.screenHeight());
  }

  @Test
  void screenIsCenteredInGui() {
    Theme theme = buildTheme(200, 100, new TextArea(10, 10, 180, 80));
    HolopadLayout layout = HolopadLayout.from(theme, 800, 600);
    assertEquals((800 - 200) / 2, layout.leftPos());
    assertEquals((600 - 100) / 2, layout.topPos());
  }

  @Test
  void textureAndButtonFlagsPassThrough() {
    Theme theme = buildTheme(100, 100, new TextArea(5, 5, 90, 90));
    HolopadLayout layout = HolopadLayout.from(theme, 400, 300);
    assertEquals(FRAME_TEX, layout.frameTexture());
    assertEquals(BG_TEX, layout.backgroundTexture());
    assertTrue(layout.showPageNumbers());
    assertTrue(layout.showCloseButton());
  }

  @Test
  void textAreaIsPreserved() {
    TextArea textArea = new TextArea(12, 118, 338, 120);
    Theme theme = buildTheme(370, 250, textArea);
    HolopadLayout layout = HolopadLayout.from(theme, 800, 600);
    assertEquals(textArea, layout.textArea());
  }
}
