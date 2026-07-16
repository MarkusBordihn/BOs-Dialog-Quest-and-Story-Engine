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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.theme.ScreenLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ScreenLayoutTest {

  private static final ResourceLocation FRAME_TEX = new ResourceLocation("test", "gui/frame.png");
  private static final ResourceLocation BG_TEX = new ResourceLocation("test", "gui/background.png");

  private Theme buildTheme(int screenWidth, int screenHeight, ThemeArea textArea) {
    return buildTheme(screenWidth, screenHeight, textArea, true, true);
  }

  private Theme buildTheme(
      int screenWidth,
      int screenHeight,
      ThemeArea textArea,
      boolean showPageNumbers,
      boolean showCloseButton) {
    return new Theme(
        UUID.randomUUID(),
        new ResourceLocation("test", "theme"),
        1,
        ThemeLayout.HOLOPAD,
        FRAME_TEX,
        BG_TEX,
        showPageNumbers,
        showCloseButton,
        new ThemeArea(16, 16, 338, 100),
        new ThemeArea(24, 101, 322, 12),
        ThemeTextAlignment.CENTER,
        textArea,
        new ThemeArea(54, 39, 262, 56),
        screenWidth,
        screenHeight);
  }

  @Test
  void explicitScreenDimensionsAreUsed() {
    Theme theme = buildTheme(370, 250, new ThemeArea(14, 121, 334, 104));
    ScreenLayout layout = ScreenLayout.from(theme, 800, 600);
    assertEquals(370, layout.screenWidth());
    assertEquals(250, layout.screenHeight());
  }

  @Test
  void zeroDimensionsFallsBackToDerivedFormula() {
    ThemeArea textArea = new ThemeArea(24, 32, 176, 120);
    Theme theme = buildTheme(0, 0, textArea);
    ScreenLayout layout = ScreenLayout.from(theme, 800, 600);
    assertEquals(354, layout.screenWidth());
    assertEquals(152, layout.screenHeight());
  }

  @Test
  void screenIsCenteredInGui() {
    Theme theme = buildTheme(200, 100, new ThemeArea(10, 10, 180, 80));
    ScreenLayout layout = ScreenLayout.from(theme, 800, 600);
    assertEquals((800 - 200) / 2, layout.leftPos());
    assertEquals((600 - 100) / 2, layout.topPos());
  }

  @Test
  void texturesAndLayoutAreasPassThrough() {
    ThemeArea textArea = new ThemeArea(14, 121, 334, 104);
    Theme theme = buildTheme(370, 250, textArea);
    ScreenLayout layout = ScreenLayout.from(theme, 800, 600);
    assertEquals(FRAME_TEX, layout.frameTexture());
    assertEquals(BG_TEX, layout.backgroundTexture());
    assertEquals(new ThemeArea(16, 16, 338, 100), layout.displayArea());
    assertEquals(new ThemeArea(24, 101, 322, 12), layout.titleArea());
    assertEquals(ThemeTextAlignment.CENTER, layout.titleAlignment());
    assertEquals(textArea, layout.textArea());
    assertEquals(new ThemeArea(54, 39, 262, 56), layout.choiceArea());
  }

  @Test
  void buttonFlagsPassThrough() {
    ScreenLayout enabled =
        ScreenLayout.from(buildTheme(100, 100, new ThemeArea(5, 5, 90, 90), true, true), 400, 300);
    assertTrue(enabled.showPageNumbers());
    assertTrue(enabled.showCloseButton());

    ScreenLayout disabled =
        ScreenLayout.from(
            buildTheme(100, 100, new ThemeArea(5, 5, 90, 90), false, false), 400, 300);
    assertFalse(disabled.showPageNumbers());
    assertFalse(disabled.showCloseButton());
  }

  @Test
  void choiceButtonsFitInsideChoiceArea() {
    ScreenLayout layout =
        ScreenLayout.from(buildTheme(370, 250, new ThemeArea(14, 121, 334, 104)), 800, 600);
    int buttonHeight = 16;
    int spacing = 4;
    int lastButtonY = layout.choiceButtonY(2, 3, buttonHeight, spacing);

    assertEquals(262, layout.choiceButtonWidth());
    assertTrue(lastButtonY + buttonHeight <= layout.topPos() + layout.choiceArea().bottom());
    assertTrue(layout.choiceButtonX(layout.choiceButtonWidth()) >= layout.leftPos());
  }
}
