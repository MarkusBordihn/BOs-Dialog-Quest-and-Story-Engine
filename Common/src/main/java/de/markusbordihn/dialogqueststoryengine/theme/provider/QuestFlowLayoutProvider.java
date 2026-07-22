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
import de.markusbordihn.dialogqueststoryengine.data.theme.option.BackgroundMode;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.ButtonStyle;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.ThemeOptions;
import de.markusbordihn.dialogqueststoryengine.registry.ThemeProvider;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class QuestFlowLayoutProvider implements ThemeProvider {

  private static final ThemeLayoutContract CONTRACT =
      ThemeLayoutContract.builder(BuiltinLayouts.QUEST_FLOW)
          .requiredAreas(ThemeSlots.STAGES, ThemeSlots.DETAIL)
          .optionalAreas(ThemeSlots.LEGEND, ThemeSlots.FOOTER)
          .optionalSprites(
              ThemeSlots.BACKGROUND,
              ThemeSlots.FRAME,
              ThemeSlots.STAGE_DONE,
              ThemeSlots.STAGE_ACTIVE,
              ThemeSlots.STAGE_LOCKED,
              ThemeSlots.CONNECTOR)
          .colors(ThemeColors.NAMES)
          .option(ThemeOptions.buttonStyle(ButtonStyle.VANILLA))
          .option(ThemeOptions.backgroundMode(BackgroundMode.OPAQUE))
          .build();

  @Override
  public ResourceLocation layoutId() {
    return BuiltinLayouts.QUEST_FLOW;
  }

  @Override
  public ThemeLayoutContract contract() {
    return CONTRACT;
  }

  @Override
  public Theme fallbackTheme() {
    return BuiltinFallbackThemes.simple(
        new ResourceLocation(Constants.MOD_NAMESPACE, "fallback_quest_flow"),
        BuiltinLayouts.QUEST_FLOW,
        320,
        240,
        Map.of(
            ThemeSlots.STAGES, new ThemeArea(8, 24, 304, 60),
            ThemeSlots.DETAIL, new ThemeArea(8, 110, 304, 52),
            ThemeSlots.LEGEND, new ThemeArea(8, 168, 304, 20),
            ThemeSlots.FOOTER, new ThemeArea(8, 196, 304, 18)));
  }
}
