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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ThemeSpriteTest {

  private static final ResourceLocation TEXTURE = new ResourceLocation("test", "gui/frame.png");

  @Test
  void validNineSliceConstructs() {
    ThemeSprite sprite =
        new ThemeSprite(
            TEXTURE,
            512,
            256,
            0,
            0,
            64,
            64,
            ThemeSpriteScaling.NINE_SLICE,
            ThemeSpriteBorder.all(8));
    assertEquals(ThemeSpriteScaling.NINE_SLICE, sprite.scaling());
    assertEquals(8, sprite.border().left());
  }

  @Test
  void nineSliceWithoutHorizontalCenterRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ThemeSprite(
                TEXTURE,
                512,
                256,
                0,
                0,
                16,
                64,
                ThemeSpriteScaling.NINE_SLICE,
                new ThemeSpriteBorder(8, 4, 8, 4)));
  }

  @Test
  void nineSliceWithoutVerticalCenterRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () ->
            new ThemeSprite(
                TEXTURE,
                512,
                256,
                0,
                0,
                64,
                16,
                ThemeSpriteScaling.NINE_SLICE,
                new ThemeSpriteBorder(4, 8, 4, 8)));
  }

  @Test
  void negativeBorderRejected() {
    assertThrows(IllegalArgumentException.class, () -> new ThemeSpriteBorder(-1, 0, 0, 0));
  }

  @Test
  void sourceExceedingTextureRejected() {
    assertThrows(
        IllegalArgumentException.class,
        () -> new ThemeSprite(TEXTURE, 64, 64, 32, 0, 64, 64, ThemeSpriteScaling.STRETCH, null));
  }

  @Test
  void stretchSpriteDefaultsBorderToZero() {
    ThemeSprite sprite =
        new ThemeSprite(TEXTURE, 512, 256, 0, 0, 370, 250, ThemeSpriteScaling.STRETCH, null);
    assertEquals(ThemeSpriteBorder.ZERO, sprite.border());
  }
}
