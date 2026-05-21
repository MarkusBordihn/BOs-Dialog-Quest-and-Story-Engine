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

package de.markusbordihn.dialogqueststoryengine.client.holopad;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

public final class HolopadPageRenderer {

  static final int LINE_SPACING = 2;

  private HolopadPageRenderer() {}

  public static List<List<String>> paginate(
      Component text, Font font, int areaWidth, int areaHeight) {
    List<FormattedCharSequence> wrapped = font.split(text, areaWidth);
    List<String> lines = new ArrayList<>(wrapped.size());
    for (FormattedCharSequence sequence : wrapped) {
      lines.add(FormattedCharSequence.EMPTY.equals(sequence) ? "" : extractString(font, sequence));
    }

    int linesPerPage = Math.max(1, areaHeight / (font.lineHeight + LINE_SPACING));
    return paginateLines(lines, linesPerPage);
  }

  public static List<List<String>> paginateLines(List<String> lines, int linesPerPage) {
    if (lines.isEmpty()) {
      return List.of(List.of());
    }

    List<List<String>> pages = new ArrayList<>();
    for (int start = 0; start < lines.size(); start += linesPerPage) {
      int end = Math.min(start + linesPerPage, lines.size());
      pages.add(List.copyOf(lines.subList(start, end)));
    }

    return List.copyOf(pages);
  }

  private static String extractString(Font font, FormattedCharSequence sequence) {
    StringBuilder builder = new StringBuilder();
    sequence.accept(
        (index, style, codePoint) -> {
          builder.appendCodePoint(codePoint);
          return true;
        });
    return builder.toString();
  }
}
