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

import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class LayoutScreenRegistry {

  private static final Map<ResourceLocation, LayoutScreenBinding> BINDINGS = new LinkedHashMap<>();
  private static final EnumMap<LayoutContentKind, ResourceLocation> DEFAULTS =
      new EnumMap<>(LayoutContentKind.class);

  private LayoutScreenRegistry() {}

  public static void register(LayoutScreenBinding binding, boolean asDefault) {
    BINDINGS.put(binding.layoutId(), binding);
    if (asDefault) {
      DEFAULTS.put(binding.contentKind(), binding.layoutId());
    }
  }

  public static Optional<LayoutScreenBinding> get(ResourceLocation layoutId) {
    return Optional.ofNullable(BINDINGS.get(layoutId));
  }

  public static boolean isCompatible(ResourceLocation layoutId, LayoutContentKind kind) {
    LayoutScreenBinding binding = BINDINGS.get(layoutId);
    return binding != null && binding.contentKind() == kind;
  }

  public static ResourceLocation defaultLayout(LayoutContentKind kind) {
    return DEFAULTS.get(kind);
  }

  public static Theme resolveTheme(ResourceLocation requestedThemeId, LayoutContentKind kind) {
    if (requestedThemeId != null) {
      Optional<Theme> theme = ThemeClientRegistry.get(requestedThemeId);
      if (theme.isPresent() && isCompatible(theme.get().layoutId(), kind)) {
        return theme.get();
      }
    }
    return ThemeClientRegistry.resolve(requestedThemeId, defaultLayout(kind));
  }
}
