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

import de.markusbordihn.dialogqueststoryengine.data.theme.NineSliceGeometry.Region;
import java.util.List;
import org.junit.jupiter.api.Test;

class NineSliceGeometryTest {

  @Test
  void normalCaseProducesNineRegions() {
    List<Region> regions = NineSliceGeometry.regions(16, 16, ThemeSpriteBorder.all(4), 100, 50);
    assertEquals(9, regions.size());

    Region topLeft = regions.get(0);
    assertEquals(0, topLeft.destinationX());
    assertEquals(0, topLeft.destinationY());
    assertEquals(4, topLeft.destinationWidth());
    assertEquals(4, topLeft.destinationHeight());
    assertTrue(!topLeft.tileX() && !topLeft.tileY());

    Region center = regions.get(4);
    assertEquals(4, center.destinationX());
    assertEquals(4, center.destinationY());
    assertEquals(92, center.destinationWidth());
    assertEquals(42, center.destinationHeight());
    assertTrue(center.tileX() && center.tileY());
  }

  @Test
  void edgesTileOnOneAxisOnly() {
    List<Region> regions = NineSliceGeometry.regions(16, 16, ThemeSpriteBorder.all(4), 100, 50);
    Region topEdge = regions.get(1);
    assertTrue(topEdge.tileX());
    assertTrue(!topEdge.tileY());
    Region leftEdge = regions.get(3);
    assertTrue(!leftEdge.tileX());
    assertTrue(leftEdge.tileY());
  }

  @Test
  void cornersClampProportionallyWhenTooNarrow() {
    // Border left+right = 8 exceeds dest width 6 -> clamp to 3/3, no center column.
    List<Region> regions = NineSliceGeometry.regions(16, 16, ThemeSpriteBorder.all(4), 6, 50);
    int totalCornerAndEdgeWidth =
        regions.stream()
            .filter(region -> region.destinationY() == 0)
            .mapToInt(Region::destinationWidth)
            .sum();
    assertEquals(6, totalCornerAndEdgeWidth);
    for (Region region : regions) {
      assertTrue(region.destinationWidth() > 0, "no zero-width regions");
      assertTrue(region.destinationHeight() > 0, "no zero-height regions");
      assertTrue(
          region.sourceWidth() > 0 && region.sourceHeight() > 0, "no zero-size source regions");
    }
  }

  @Test
  void zeroDestReturnsEmpty() {
    assertTrue(NineSliceGeometry.regions(16, 16, ThemeSpriteBorder.all(4), 0, 50).isEmpty());
    assertTrue(NineSliceGeometry.regions(16, 16, ThemeSpriteBorder.all(4), 50, 0).isEmpty());
  }

  @Test
  void destinationRegionsTileTheWholeArea() {
    List<Region> regions = NineSliceGeometry.regions(16, 16, ThemeSpriteBorder.all(4), 100, 50);
    int maxRight =
        regions.stream()
            .mapToInt(region -> region.destinationX() + region.destinationWidth())
            .max()
            .orElse(0);
    int maxBottom =
        regions.stream()
            .mapToInt(region -> region.destinationY() + region.destinationHeight())
            .max()
            .orElse(0);
    assertEquals(100, maxRight);
    assertEquals(50, maxBottom);
  }
}
