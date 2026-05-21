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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;

class HolopadPageRendererTest {

  @Test
  void emptyLinesProducesSingleEmptyPage() {
    List<List<String>> pages = HolopadPageRenderer.paginateLines(List.of(), 5);
    assertEquals(1, pages.size());
    assertTrue(pages.get(0).isEmpty());
  }

  @Test
  void fitsOnOnePage() {
    List<String> lines = List.of("Line 1", "Line 2", "Line 3");
    List<List<String>> pages = HolopadPageRenderer.paginateLines(lines, 5);
    assertEquals(1, pages.size());
    assertEquals(lines, pages.get(0));
  }

  @Test
  void exactlyOnePage() {
    List<String> lines = List.of("A", "B", "C");
    List<List<String>> pages = HolopadPageRenderer.paginateLines(lines, 3);
    assertEquals(1, pages.size());
    assertEquals(lines, pages.get(0));
  }

  @Test
  void splitsAcrossMultiplePages() {
    List<String> lines = List.of("L1", "L2", "L3", "L4", "L5");
    List<List<String>> pages = HolopadPageRenderer.paginateLines(lines, 2);
    assertEquals(3, pages.size());
    assertEquals(List.of("L1", "L2"), pages.get(0));
    assertEquals(List.of("L3", "L4"), pages.get(1));
    assertEquals(List.of("L5"), pages.get(2));
  }

  @Test
  void singleLinePerPage() {
    List<String> lines = List.of("A", "B", "C");
    List<List<String>> pages = HolopadPageRenderer.paginateLines(lines, 1);
    assertEquals(3, pages.size());
    assertEquals(List.of("A"), pages.get(0));
    assertEquals(List.of("B"), pages.get(1));
    assertEquals(List.of("C"), pages.get(2));
  }

  @Test
  void pagesAreImmutable() {
    List<List<String>> pages = HolopadPageRenderer.paginateLines(List.of("A", "B"), 1);
    assertFalse(pages.isEmpty());
    assertFalse(pages.get(0).isEmpty());
  }

  @Test
  void singleLineSinglePage() {
    List<List<String>> pages = HolopadPageRenderer.paginateLines(List.of("Only"), 10);
    assertEquals(1, pages.size());
    assertEquals(List.of("Only"), pages.get(0));
  }
}
