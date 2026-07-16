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

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ThemeClientRegistry {

  public static final ResourceLocation DEFAULT_THEME_ID =
      new ResourceLocation(Constants.MOD_ID, "default_holopad");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Map<ResourceLocation, Theme> entries = new LinkedHashMap<>();

  private ThemeClientRegistry() {}

  public static void put(Theme theme) {
    if (entries.containsKey(theme.id())) {
      log.warn(
          "{} Theme registry: duplicate id {} - overwriting.", Constants.LOG_PREFIX, theme.id());
    }

    entries.put(theme.id(), theme);
  }

  public static Optional<Theme> get(ResourceLocation id) {
    return Optional.ofNullable(entries.get(id));
  }

  public static Optional<Theme> getOrDefault(ResourceLocation id) {
    return get(id).or(() -> get(DEFAULT_THEME_ID));
  }

  public static boolean contains(ResourceLocation id) {
    return entries.containsKey(id);
  }

  public static int size() {
    return entries.size();
  }

  public static Set<ResourceLocation> ids() {
    return Collections.unmodifiableSet(entries.keySet());
  }

  public static void clear() {
    entries.clear();
  }
}
