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

package de.markusbordihn.dialogqueststoryengine.data.theme.layout;

import de.markusbordihn.dialogqueststoryengine.data.theme.ResolvedLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeColors;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSlots;
import java.util.Optional;

public record JournalLayout(ResolvedLayout base) {

  public ThemeArea tabs() {
    return this.base.area(ThemeSlots.TABS);
  }

  public ThemeArea index() {
    return this.base.area(ThemeSlots.INDEX);
  }

  public ThemeArea detail() {
    return this.base.area(ThemeSlots.DETAIL);
  }

  public Optional<ThemeArea> objectives() {
    return this.base.optionalArea(ThemeSlots.OBJECTIVES);
  }

  public Optional<ThemeArea> footer() {
    return this.base.optionalArea(ThemeSlots.FOOTER);
  }

  public int accentColor() {
    return this.base.color(ThemeColors.ACCENT);
  }

  public int textColor() {
    return this.base.color(ThemeColors.ON_SURFACE);
  }

  public int mutedColor() {
    return this.base.color(ThemeColors.ON_SURFACE_MUTED);
  }
}
