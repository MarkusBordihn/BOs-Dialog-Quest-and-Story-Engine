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

package de.markusbordihn.dialogqueststoryengine.client.screen.ui.components;

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Panel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ColumnListPanel<T> extends Panel {

  private static final int HEADER_HEIGHT = 14;

  private final List<Column> columns = new ArrayList<>();
  private List<T> items = new ArrayList<>();
  private int entryHeight = 18;
  private Consumer<T> onSelect;
  private Renderer<T> entryRenderer;
  private ListPanel<T> listPanel;

  public ColumnListPanel(int posX, int posY, int width, int height) {
    super(posX, posY, width, height);
    this.padding = 0;
    this.clipChildren = false;
  }

  public void addColumn(String labelKey, float widthFraction) {
    this.columns.add(new Column(labelKey, widthFraction));
  }

  public void setEntryHeight(int height) {
    this.entryHeight = height;
  }

  public void setOnSelect(Consumer<T> onSelect) {
    this.onSelect = onSelect;
  }

  public void setEntryRenderer(Renderer<T> renderer) {
    this.entryRenderer = renderer;
  }

  public void setItems(List<T> newItems) {
    this.items = new ArrayList<>(newItems);
    if (this.listPanel != null) {
      this.listPanel.setItems(this.items);
    }
  }

  protected void renderEntry(
      GuiGraphics graphics,
      Font font,
      T entry,
      int index,
      int x,
      int y,
      int width,
      int height,
      ColorPalette palette,
      int[] columnOffsets) {
    if (this.entryRenderer != null) {
      this.entryRenderer.render(
          graphics, font, entry, index, x, y, width, height, palette, columnOffsets);
    }
  }

  @Override
  protected void addWidgets() {
    int[] columnOffsets = buildColumnOffsets();
    for (int i = 0; i < this.columns.size(); i++) {
      addWidget(
          new Label(
              columnOffsets[i],
              0,
              this.columns.get(i).labelKey(),
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
    }
    addWidget(new Separator(0, 12, this.width, true));

    this.listPanel = new ListPanel<>(0, HEADER_HEIGHT, this.width, this.height - HEADER_HEIGHT);
    this.listPanel.setEntryHeight(this.entryHeight);
    if (this.onSelect != null) {
      this.listPanel.setOnSelect(this.onSelect);
    }
    this.listPanel.setEntryRenderer(
        (graphics, font, entry, index, x, y, width, height, palette) ->
            renderEntry(graphics, font, entry, index, x, y, width, height, palette, columnOffsets));
    this.listPanel.setItems(this.items);
    addWidget(this.listPanel);
  }

  private int[] buildColumnOffsets() {
    int[] offsets = new int[this.columns.size()];
    int cursor = 0;
    for (int i = 0; i < this.columns.size(); i++) {
      offsets[i] = cursor;
      cursor += (int) (this.width * this.columns.get(i).widthFraction());
    }
    return offsets;
  }

  @FunctionalInterface
  public interface Renderer<T> {
    void render(
        GuiGraphics graphics,
        Font font,
        T entry,
        int index,
        int x,
        int y,
        int width,
        int height,
        ColorPalette palette,
        int[] columnOffsets);
  }

  public record Column(String labelKey, float widthFraction) {}
}
