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

import java.util.ArrayList;
import java.util.List;

public final class NineSliceGeometry {

  private NineSliceGeometry() {}

  public static List<Region> regions(
      int sourceWidth,
      int sourceHeight,
      ThemeSpriteBorder border,
      int destinationWidth,
      int destinationHeight) {
    List<Region> regions = new ArrayList<>(9);
    if (destinationWidth <= 0 || destinationHeight <= 0) {
      return regions;
    }

    int sourceLeft = border.left();
    int sourceRight = border.right();
    int sourceTop = border.top();
    int sourceBottom = border.bottom();

    int destinationLeft = sourceLeft;
    int destinationRight = sourceRight;
    if (destinationLeft + destinationRight > destinationWidth) {
      int total = destinationLeft + destinationRight;
      destinationLeft = total == 0 ? 0 : Math.round((float) sourceLeft * destinationWidth / total);
      destinationRight = destinationWidth - destinationLeft;
    }
    int destinationTop = sourceTop;
    int destinationBottom = sourceBottom;
    if (destinationTop + destinationBottom > destinationHeight) {
      int total = destinationTop + destinationBottom;
      destinationTop = total == 0 ? 0 : Math.round((float) sourceTop * destinationHeight / total);
      destinationBottom = destinationHeight - destinationTop;
    }

    int sourceCenterWidth = sourceWidth - sourceLeft - sourceRight;
    int sourceCenterHeight = sourceHeight - sourceTop - sourceBottom;
    int destinationCenterWidth = destinationWidth - destinationLeft - destinationRight;
    int destinationCenterHeight = destinationHeight - destinationTop - destinationBottom;

    int sourceRightX = sourceWidth - sourceRight;
    int sourceBottomY = sourceHeight - sourceBottom;
    int destinationRightX = destinationWidth - destinationRight;
    int destinationBottomY = destinationHeight - destinationBottom;

    add(regions, 0, 0, sourceLeft, sourceTop, 0, 0, destinationLeft, destinationTop, false, false);
    add(
        regions,
        sourceLeft,
        0,
        sourceCenterWidth,
        sourceTop,
        destinationLeft,
        0,
        destinationCenterWidth,
        destinationTop,
        true,
        false);
    add(
        regions,
        sourceRightX,
        0,
        sourceRight,
        sourceTop,
        destinationRightX,
        0,
        destinationRight,
        destinationTop,
        false,
        false);

    add(
        regions,
        0,
        sourceTop,
        sourceLeft,
        sourceCenterHeight,
        0,
        destinationTop,
        destinationLeft,
        destinationCenterHeight,
        false,
        true);
    add(
        regions,
        sourceLeft,
        sourceTop,
        sourceCenterWidth,
        sourceCenterHeight,
        destinationLeft,
        destinationTop,
        destinationCenterWidth,
        destinationCenterHeight,
        true,
        true);
    add(
        regions,
        sourceRightX,
        sourceTop,
        sourceRight,
        sourceCenterHeight,
        destinationRightX,
        destinationTop,
        destinationRight,
        destinationCenterHeight,
        false,
        true);

    add(
        regions,
        0,
        sourceBottomY,
        sourceLeft,
        sourceBottom,
        0,
        destinationBottomY,
        destinationLeft,
        destinationBottom,
        false,
        false);
    add(
        regions,
        sourceLeft,
        sourceBottomY,
        sourceCenterWidth,
        sourceBottom,
        destinationLeft,
        destinationBottomY,
        destinationCenterWidth,
        destinationBottom,
        true,
        false);
    add(
        regions,
        sourceRightX,
        sourceBottomY,
        sourceRight,
        sourceBottom,
        destinationRightX,
        destinationBottomY,
        destinationRight,
        destinationBottom,
        false,
        false);

    return regions;
  }

  private static void add(
      List<Region> regions,
      int sourceX,
      int sourceY,
      int sourceWidth,
      int sourceHeight,
      int destinationX,
      int destinationY,
      int destinationWidth,
      int destinationHeight,
      boolean tileX,
      boolean tileY) {
    if (sourceWidth <= 0 || sourceHeight <= 0 || destinationWidth <= 0 || destinationHeight <= 0) {
      return;
    }
    regions.add(
        new Region(
            sourceX,
            sourceY,
            sourceWidth,
            sourceHeight,
            destinationX,
            destinationY,
            destinationWidth,
            destinationHeight,
            tileX,
            tileY));
  }

  public record Region(
      int sourceX,
      int sourceY,
      int sourceWidth,
      int sourceHeight,
      int destinationX,
      int destinationY,
      int destinationWidth,
      int destinationHeight,
      boolean tileX,
      boolean tileY) {}
}
