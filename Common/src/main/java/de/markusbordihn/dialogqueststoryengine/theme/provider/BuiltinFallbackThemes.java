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
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeAnchor;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeScaleLimits;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSlots;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.registry.ThemeProvider;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public final class BuiltinFallbackThemes {

  private BuiltinFallbackThemes() {}

  public static Theme forLayout(ResourceLocation layoutId) {
    return Registries.THEMES
        .get(layoutId)
        .map(ThemeProvider::fallbackTheme)
        .orElseGet(BuiltinFallbackThemes::generic);
  }

  private static Theme generic() {
    return simple(
        new ResourceLocation(Constants.MOD_NAMESPACE, "fallback_generic"),
        new ResourceLocation(Constants.MOD_NAMESPACE, "dialog"),
        320,
        240,
        Map.of(
            ThemeSlots.TEXT, new ThemeArea(16, 16, 288, 160),
            ThemeSlots.CHOICES, new ThemeArea(16, 180, 288, 52)));
  }

  public static Theme simple(
      ResourceLocation id,
      ResourceLocation layoutId,
      int logicalWidth,
      int logicalHeight,
      Map<String, ThemeArea> areas) {
    return new Theme(
        UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)),
        id,
        1,
        layoutId,
        logicalWidth,
        logicalHeight,
        ThemeScaleLimits.DEFAULT,
        ThemeAnchor.CENTER,
        areas,
        Map.of(),
        Map.of(),
        Map.of());
  }
}
