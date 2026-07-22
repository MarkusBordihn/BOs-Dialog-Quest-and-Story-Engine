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

package de.markusbordihn.dialogqueststoryengine.client.screen.ui.layout;

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Widget;

public final class GridLayout {

  private final int originX;
  private final int originY;
  private final int totalWidth;
  private final int columns;
  private final int columnGap;
  private final int rowGap;

  private GridLayout(
      int originX, int originY, int totalWidth, int columns, int columnGap, int rowGap) {
    this.originX = originX;
    this.originY = originY;
    this.totalWidth = totalWidth;
    this.columns = columns;
    this.columnGap = columnGap;
    this.rowGap = rowGap;
  }

  public static GridLayout of(int originX, int originY, int totalWidth, int columns, int gap) {
    return new GridLayout(originX, originY, totalWidth, columns, gap, gap);
  }

  public static GridLayout of(
      int originX, int originY, int totalWidth, int columns, int columnGap, int rowGap) {
    return new GridLayout(originX, originY, totalWidth, columns, columnGap, rowGap);
  }

  public int cellWidth() {
    return (this.totalWidth - this.columnGap * (this.columns - 1)) / this.columns;
  }

  public int cellX(int column) {
    return this.originX + column * (this.cellWidth() + this.columnGap);
  }

  public int cellY(int row, int rowHeight) {
    return this.originY + row * (rowHeight + this.rowGap);
  }

  public void fill(Widget widget, int column, int row, int rowHeight) {
    widget.setPosition(this.cellX(column), this.cellY(row, rowHeight));
    widget.setSize(this.cellWidth(), rowHeight);
  }

  public int getX(int column) {
    return this.cellX(column);
  }

  public int getY(int row, int rowHeight) {
    return this.cellY(row, rowHeight);
  }

  public int getWidth() {
    return this.cellWidth();
  }
}
