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
import java.util.Locale;
import java.util.function.Consumer;
import java.util.function.Function;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ColumnListPanel<T> extends Panel {

  private static final int HEADER_HEIGHT = 14;
  private static final int SEARCH_HEIGHT = 18;

  private final List<Column> columns = new ArrayList<>();
  private List<T> allItems = new ArrayList<>();
  private List<T> items = new ArrayList<>();
  private int entryHeight = 18;
  private Consumer<T> onSelect;
  private Renderer<T> entryRenderer;
  private ListPanel<T> listPanel;
  private Function<T, String> searchTextFunction;
  private TextInput searchField;
  private String query = "";

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

  public void setSearchable(Function<T, String> searchTextFunction) {
    this.searchTextFunction = searchTextFunction;
  }

  public void setItems(List<T> newItems) {
    this.allItems = new ArrayList<>(newItems);
    applyFilter();
  }

  private void applyFilter() {
    if (this.searchTextFunction == null || this.query.isBlank()) {
      this.items = new ArrayList<>(this.allItems);
    } else {
      String needle = this.query.toLowerCase(Locale.ROOT);
      this.items = new ArrayList<>();
      for (T item : this.allItems) {
        String text = this.searchTextFunction.apply(item);
        if (text != null && text.toLowerCase(Locale.ROOT).contains(needle)) {
          this.items.add(item);
        }
      }
    }
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
    int top = 0;
    if (this.searchTextFunction != null) {
      this.searchField =
          new TextInput(
              0,
              0,
              this.width,
              14,
              value -> {
                this.query = value;
                this.applyFilter();
              });
      this.searchField.setSuggestion(TextComponent.of("search.placeholder").getString());
      this.searchField.setValue(this.query);
      this.addWidget(this.searchField);
      top = SEARCH_HEIGHT;
    }

    int[] columnOffsets = this.buildColumnOffsets();
    for (int i = 0; i < this.columns.size(); i++) {
      this.addWidget(
          new Label(
              columnOffsets[i],
              top,
              this.columns.get(i).labelKey(),
              0,
              ScaledText.SCALE_SMALL,
              Label.Alignment.LEFT));
    }
    this.addWidget(new Separator(0, top + 12, this.width, true));

    int listTop = top + HEADER_HEIGHT;
    this.listPanel = new ListPanel<>(0, listTop, this.width, this.height - listTop);
    this.listPanel.setEntryHeight(this.entryHeight);
    if (this.onSelect != null) {
      this.listPanel.setOnSelect(this.onSelect);
    }
    this.listPanel.setEntryRenderer(
        (graphics, font, entry, index, x, y, width, height, palette) ->
            this.renderEntry(
                graphics, font, entry, index, x, y, width, height, palette, columnOffsets));
    this.listPanel.setItems(this.items);
    this.addWidget(this.listPanel);
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
