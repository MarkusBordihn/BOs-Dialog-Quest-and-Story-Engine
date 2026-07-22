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

import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThemeViewportEquivalenceTest {

  private static boolean areaContains(ThemeArea area, double lx, double ly) {
    return lx >= area.x() && lx < area.right() && ly >= area.y() && ly < area.bottom();
  }

  private static boolean rectContains(ThemeViewport.ScreenRect rect, double px, double py) {
    return px >= rect.x()
        && px < rect.x() + rect.width()
        && py >= rect.y()
        && py < rect.y() + rect.height();
  }

  private static boolean nearBoundary(ThemeViewport.ScreenRect rect, int px, int py) {
    return within(px, rect.x())
        || within(px, rect.x() + rect.width())
        || within(py, rect.y())
        || within(py, rect.y() + rect.height());
  }

  private static boolean within(int value, int edge) {
    return Math.abs(value - edge) <= 2;
  }

  @Test
  void screenRectCenterMapsInsideLogicalArea() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 800, 600, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    ThemeArea area = new ThemeArea(60, 128, 200, 62);
    ThemeViewport.ScreenRect rect = viewport.toScreenRect(area);

    double centerX = rect.x() + rect.width() / 2.0;
    double centerY = rect.y() + rect.height() / 2.0;
    assertTrue(areaContains(area, viewport.toLogicalX(centerX), viewport.toLogicalY(centerY)));
  }

  @Test
  void pointsOutsideRectMapOutsideArea() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 854, 480, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    ThemeArea area = new ThemeArea(24, 44, 272, 78);
    ThemeViewport.ScreenRect rect = viewport.toScreenRect(area);

    double leftX = rect.x() - 8;
    double belowY = rect.y() + rect.height() + 8;
    assertTrue(!areaContains(area, viewport.toLogicalX(leftX), viewport.toLogicalY(rect.y() + 1)));
    assertTrue(!areaContains(area, viewport.toLogicalX(rect.x() + 1), viewport.toLogicalY(belowY)));
  }

  @Test
  void containmentAgreesOnAGrid() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 800, 600, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    ThemeArea area = new ThemeArea(40, 30, 220, 120);
    ThemeViewport.ScreenRect rect = viewport.toScreenRect(area);

    // Interior/exterior points (kept off the 1px boundary to avoid rounding ambiguity) must agree.
    for (int px = 0; px < viewport.guiWidth(); px += 17) {
      for (int py = 0; py < viewport.guiHeight(); py += 17) {
        boolean inRect = rectContains(rect, px, py);
        boolean inArea = areaContains(area, viewport.toLogicalX(px), viewport.toLogicalY(py));
        if (nearBoundary(rect, px, py)) {
          continue;
        }
        assertTrue(inRect == inArea, "mismatch at screen " + px + "," + py);
      }
    }
  }
}
