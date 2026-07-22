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

import de.markusbordihn.dialogqueststoryengine.data.theme.option.ThemeOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public record ResolvedLayout(
    ResourceLocation themeId,
    ResourceLocation layoutId,
    ThemeViewport viewport,
    Map<String, ThemeArea> areas,
    Map<String, ThemeSprite> sprites,
    Map<String, Integer> colors,
    Map<String, Object> options) {

  public ResolvedLayout {
    areas = Map.copyOf(areas);
    sprites = Map.copyOf(sprites);
    colors = Map.copyOf(colors);
    options = Map.copyOf(options);
  }

  public static ResolvedLayout of(
      Theme theme, ThemeLayoutContract contract, ThemeViewport viewport) {
    Map<String, Integer> resolvedColors = new HashMap<>();
    for (String name : contract.requiredColors()) {
      resolvedColors.put(name, ThemeColors.defaultColor(name));
    }
    for (String name : contract.optionalColors()) {
      resolvedColors.put(name, ThemeColors.defaultColor(name));
    }
    resolvedColors.putAll(theme.colors());

    Map<String, Object> resolvedOptions = new HashMap<>();
    for (ThemeOption<?> option : contract.options().values()) {
      resolvedOptions.put(option.key(), option.defaultValue());
    }
    resolvedOptions.putAll(theme.options());

    return new ResolvedLayout(
        theme.id(),
        theme.layoutId(),
        viewport,
        theme.areas(),
        theme.sprites(),
        resolvedColors,
        resolvedOptions);
  }

  public ThemeArea area(String name) {
    ThemeArea area = this.areas.get(name);
    if (area == null) {
      throw new IllegalStateException("Missing required layout area: " + name);
    }
    return area;
  }

  public Optional<ThemeArea> optionalArea(String name) {
    return Optional.ofNullable(this.areas.get(name));
  }

  public Optional<ThemeSprite> sprite(String name) {
    return Optional.ofNullable(this.sprites.get(name));
  }

  public int color(String name) {
    return this.colors.getOrDefault(name, ThemeColors.defaultColor(name));
  }

  @SuppressWarnings("unchecked")
  public <T> T option(ThemeOption<T> option) {
    Object value = this.options.get(option.key());
    return value != null ? (T) value : option.defaultValue();
  }

  @SuppressWarnings("unchecked")
  public <T> T optionOr(String key, T fallback) {
    Object value = this.options.get(key);
    return value != null ? (T) value : fallback;
  }
}
