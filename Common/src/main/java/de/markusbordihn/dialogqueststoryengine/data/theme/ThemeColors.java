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

import java.util.Map;

public final class ThemeColors {

  public static final String SURFACE = "surface";
  public static final String ON_SURFACE = "on_surface";
  public static final String ON_SURFACE_MUTED = "on_surface_muted";
  public static final String ACCENT = "accent";
  public static final String SUCCESS = "success";
  public static final String WARNING = "warning";
  public static final String ERROR = "error";
  public static final String LOCKED = "locked";

  public static final String[] NAMES = {
    SURFACE, ON_SURFACE, ON_SURFACE_MUTED, ACCENT, SUCCESS, WARNING, ERROR, LOCKED
  };

  public static final Map<String, Integer> DEFAULTS =
      Map.ofEntries(
          Map.entry(SURFACE, 0xFF2E2E33),
          Map.entry(ON_SURFACE, 0xFFE8E8E8),
          Map.entry(ON_SURFACE_MUTED, 0xFFAAAAAA),
          Map.entry(ACCENT, 0xFF35E0D5),
          Map.entry(SUCCESS, 0xFF55FF55),
          Map.entry(WARNING, 0xFFFFB02E),
          Map.entry(ERROR, 0xFFFF5555),
          Map.entry(LOCKED, 0xFF7E7E7E));

  private ThemeColors() {}

  public static int defaultColor(String name) {
    return DEFAULTS.getOrDefault(name, 0xFFFFFFFF);
  }
}
