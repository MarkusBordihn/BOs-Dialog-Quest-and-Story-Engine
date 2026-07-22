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

package de.markusbordihn.dialogqueststoryengine.theme.provider;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.theme.BuiltinLayouts;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeColors;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayoutContract;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSlots;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.BackgroundMode;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.ButtonStyle;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.PortraitSide;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.ThemeOptions;
import de.markusbordihn.dialogqueststoryengine.registry.ThemeProvider;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class DialogLetterboxLayoutProvider implements ThemeProvider {

  private static final ThemeLayoutContract CONTRACT =
      ThemeLayoutContract.builder(BuiltinLayouts.DIALOG_LETTERBOX)
          .requiredAreas(ThemeSlots.TEXT, ThemeSlots.CHOICES)
          .optionalAreas(
              ThemeSlots.SPEAKER, ThemeSlots.PORTRAIT, ThemeSlots.SCENE, ThemeSlots.HISTORY)
          .optionalSprites(ThemeSlots.BACKGROUND, ThemeSlots.FRAME, ThemeSlots.LETTERBOX_BAR)
          .colors(ThemeColors.NAMES)
          .option(ThemeOptions.speakerAlignment(ThemeTextAlignment.LEFT))
          .option(ThemeOptions.textAlignment(ThemeTextAlignment.LEFT))
          .option(ThemeOptions.portraitSide(PortraitSide.LEFT))
          .option(ThemeOptions.buttonStyle(ButtonStyle.VANILLA))
          .option(ThemeOptions.backgroundMode(BackgroundMode.LETTERBOX))
          .option(ThemeOptions.showCloseButton(true))
          .build();

  @Override
  public ResourceLocation layoutId() {
    return BuiltinLayouts.DIALOG_LETTERBOX;
  }

  @Override
  public ThemeLayoutContract contract() {
    return CONTRACT;
  }

  @Override
  public Theme fallbackTheme() {
    return BuiltinFallbackThemes.simple(
        new ResourceLocation(Constants.MOD_NAMESPACE, "fallback_dialog_letterbox"),
        BuiltinLayouts.DIALOG_LETTERBOX,
        427,
        240,
        Map.of(
            ThemeSlots.SPEAKER, new ThemeArea(27, 168, 92, 12),
            ThemeSlots.TEXT, new ThemeArea(24, 190, 388, 40),
            ThemeSlots.CHOICES, new ThemeArea(280, 114, 134, 52)));
  }
}
