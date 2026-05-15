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

public final class StackLayout {

  private final int startX;
  private final int startY;
  private final int spacing;
  private final boolean vertical;

  private StackLayout(int startX, int startY, int spacing, boolean vertical) {
    this.startX = startX;
    this.startY = startY;
    this.spacing = spacing;
    this.vertical = vertical;
  }

  public static StackLayout vertical(int x, int y, int spacing) {
    return new StackLayout(x, y, spacing, true);
  }

  public static StackLayout horizontal(int x, int y, int spacing) {
    return new StackLayout(x, y, spacing, false);
  }

  public int apply(Widget... widgets) {
    int cursor = vertical ? startY : startX;
    for (Widget widget : widgets) {
      if (vertical) {
        widget.setPosition(startX, cursor);
        cursor += widget.getHeight() + spacing;
      } else {
        widget.setPosition(cursor, startY);
        cursor += widget.getWidth() + spacing;
      }
    }

    return cursor - spacing;
  }

  public int applyVisible(Widget... widgets) {
    int cursor = vertical ? startY : startX;
    for (Widget widget : widgets) {
      if (!widget.isVisible()) {
        continue;
      }
      if (vertical) {
        widget.setPosition(startX, cursor);
        cursor += widget.getHeight() + spacing;
      } else {
        widget.setPosition(cursor, startY);
        cursor += widget.getWidth() + spacing;
      }
    }

    return cursor > (vertical ? startY : startX) ? cursor - spacing : cursor;
  }
}
