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
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

class ThemeViewportTest {

  @Test
  void integerScaleWhenItFits() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 640, 480, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    assertEquals(2.0f, viewport.scale());
    assertEquals(0, viewport.originX());
    assertEquals(0, viewport.originY());
  }

  @Test
  void scaleFloorsToIntegerAndCenters() {
    // fit = 2.5 -> floor 2.0; scaled 640x480 centered in 800x600.
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 800, 600, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    assertEquals(2.0f, viewport.scale());
    assertEquals(80, viewport.originX());
    assertEquals(60, viewport.originY());
  }

  @Test
  void fractionalScaleUsesQuarterSteps() {
    // fit = min(1.0, 0.75) = 0.75 -> quarter step 0.75.
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 320, 180, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    assertEquals(0.75f, viewport.scale());
    assertEquals(240, viewport.scaledWidth());
    assertEquals(180, viewport.scaledHeight());
    assertEquals(40, viewport.originX());
    assertEquals(0, viewport.originY());
  }

  @Test
  void minLimitNeverForcesOverflow() {
    // Window exactly fits one logical pixel per screen pixel; a min limit of 2.0 must not upscale
    // to 2.0 and overflow the window.
    ThemeScaleLimits limits = new ThemeScaleLimits(2.0f, 4.0f);
    ThemeViewport viewport = ThemeViewport.of(320, 240, 320, 240, limits, ThemeAnchor.CENTER);
    assertEquals(1.0f, viewport.scale());
    assertTrue(viewport.scaledWidth() <= 320);
    assertTrue(viewport.scaledHeight() <= 240);
  }

  @Test
  void maxLimitCapsScale() {
    ThemeScaleLimits limits = new ThemeScaleLimits(0.5f, 3.0f);
    ThemeViewport viewport = ThemeViewport.of(320, 240, 1600, 1200, limits, ThemeAnchor.CENTER);
    assertEquals(3.0f, viewport.scale());
  }

  @Test
  void mouseInverseRoundTrips() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 800, 600, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    for (int lx = 0; lx <= 320; lx += 16) {
      for (int ly = 0; ly <= 240; ly += 16) {
        double backX = viewport.toLogicalX(viewport.toScreenX(lx));
        double backY = viewport.toLogicalY(viewport.toScreenY(ly));
        assertEquals(lx, backX, 1e-3);
        assertEquals(ly, backY, 1e-3);
      }
    }
  }

  @Test
  void toScreenRectMapsAreaToPixels() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 800, 600, ThemeScaleLimits.DEFAULT, ThemeAnchor.CENTER);
    ThemeViewport.ScreenRect rect = viewport.toScreenRect(new ThemeArea(10, 20, 100, 50));
    assertEquals(100, rect.x());
    assertEquals(100, rect.y());
    assertEquals(200, rect.width());
    assertEquals(100, rect.height());
  }

  @Test
  void topLeftAnchorPlacesOriginAtZero() {
    ThemeViewport viewport =
        ThemeViewport.of(320, 240, 800, 600, ThemeScaleLimits.DEFAULT, ThemeAnchor.TOP_LEFT);
    assertEquals(0, viewport.originX());
    assertEquals(0, viewport.originY());
  }
}
