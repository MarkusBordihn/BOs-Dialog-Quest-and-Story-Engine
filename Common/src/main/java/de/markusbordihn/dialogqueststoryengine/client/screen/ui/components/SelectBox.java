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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SelectBox<T> extends AbstractButton {

  private static final int ARROW_WIDTH = 14;
  private static final int MAX_DROPDOWN_ENTRIES = 6;
  private static final int ENTRY_HEIGHT = 16;
  private static final int FILTER_HEIGHT = 16;
  private static final int SEARCH_THRESHOLD = 8;

  private final List<SelectOption<T>> options;
  private final Consumer<T> onChange;
  private final Consumer<Panel> openOverlay;
  private final Runnable closeOverlay;
  private int selectedIndex = -1;
  private boolean searchable;

  public SelectBox(
      int posX,
      int posY,
      int width,
      int height,
      List<SelectOption<T>> options,
      Consumer<T> onChange,
      Consumer<Panel> openOverlay,
      Runnable closeOverlay) {
    super(posX, posY, width, height);
    this.options = new ArrayList<>(options);
    this.onChange = onChange;
    this.openOverlay = openOverlay;
    this.closeOverlay = closeOverlay;
  }

  public void setOptions(List<SelectOption<T>> options) {
    this.options.clear();
    this.options.addAll(options);
    if (this.selectedIndex >= this.options.size()) {
      this.selectedIndex = -1;
    }
  }

  public void setSearchable(boolean searchable) {
    this.searchable = searchable;
  }

  public int getSelectedIndex() {
    return this.selectedIndex;
  }

  public void setSelectedIndex(int index) {
    this.selectedIndex = (index >= 0 && index < this.options.size()) ? index : -1;
  }

  public T getSelectedValue() {
    return (this.selectedIndex >= 0 && this.selectedIndex < this.options.size())
        ? this.options.get(this.selectedIndex).value()
        : null;
  }

  public void selectByValue(T value) {
    for (int i = 0; i < this.options.size(); i++) {
      if (this.options.get(i).value() != null && this.options.get(i).value().equals(value)) {
        this.selectedIndex = i;
        return;
      }
    }

    this.selectedIndex = -1;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }

    ColorPalette palette = ColorPalette.current();
    int x = this.getX();
    int y = this.getY();
    this.hovered = this.isMouseOver(mouseX, mouseY);

    int backgroundColor;
    if (!this.active) {
      backgroundColor = palette.surfaceContainerLow();
    } else if (this.pressed || this.hovered) {
      backgroundColor = palette.surfaceContainerHigh();
    } else {
      backgroundColor = palette.surfaceContainer();
    }
    fillRoundedRect(graphics, x, y, this.width, this.height, backgroundColor);
    drawBorderRoundedBevel(graphics, x, y, this.width, this.height, palette.outline());

    Font font = Minecraft.getInstance().font;
    String label =
        (this.selectedIndex >= 0 && this.selectedIndex < this.options.size())
            ? this.options.get(this.selectedIndex).label()
            : "—";
    int textColor = this.active ? palette.onSurface() : palette.onSurfaceLow();
    int textY = y + (this.height - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2;
    ScaledText.draw(graphics, font, label, x + 4, textY, textColor, ScaledText.SCALE_BODY);

    int arrowX = x + this.width - ARROW_WIDTH;
    graphics.fill(arrowX, y + 1, arrowX + 1, y + this.height - 1, palette.outline());
    ScaledText.drawCentered(
        graphics,
        font,
        "▾",
        arrowX + ARROW_WIDTH / 2,
        y + (this.height - font.lineHeight) / 2,
        textColor,
        ScaledText.SCALE_NORMAL);
  }

  @Override
  protected void onPress() {
    if (this.openOverlay == null) {
      return;
    }

    boolean withFilter = this.searchable || this.options.size() > SEARCH_THRESHOLD;
    int listHeight = Math.min(this.options.size(), MAX_DROPDOWN_ENTRIES) * ENTRY_HEIGHT + 2;
    int dropdownHeight =
        withFilter ? FILTER_HEIGHT + MAX_DROPDOWN_ENTRIES * ENTRY_HEIGHT + 2 : listHeight;
    DropdownPanel<T> dropdown =
        new DropdownPanel<>(
            this.getX(),
            this.getY() + this.height,
            this.width,
            dropdownHeight,
            this.options,
            withFilter,
            index -> {
              this.selectedIndex = index;
              if (this.onChange != null) {
                this.onChange.accept(this.options.get(index).value());
              }
              if (this.closeOverlay != null) {
                this.closeOverlay.run();
              }
            });
    this.openOverlay.accept(dropdown);
  }

  private static class DropdownPanel<T> extends Panel {

    private final List<SelectOption<T>> items;
    private final Consumer<Integer> onSelect;
    private final boolean withFilter;
    private final TextInput filterInput;
    private final List<Integer> filtered = new ArrayList<>();
    private int scrollOffset;

    DropdownPanel(
        int absoluteX,
        int absoluteY,
        int width,
        int height,
        List<SelectOption<T>> items,
        boolean withFilter,
        Consumer<Integer> onSelect) {
      super(absoluteX, absoluteY, width, height);
      this.items = items;
      this.onSelect = onSelect;
      this.withFilter = withFilter;
      this.clipChildren = true;
      for (int i = 0; i < items.size(); i++) {
        this.filtered.add(i);
      }
      if (withFilter) {
        this.filterInput =
            new TextInput(
                absoluteX + 2, absoluteY + 1, width - 4, FILTER_HEIGHT - 2, this::applyFilter);
        this.filterInput.setSuggestion("search...");
        this.filterInput.focus();
      } else {
        this.filterInput = null;
      }
    }

    private void applyFilter(String query) {
      String needle = query.trim().toLowerCase(Locale.ROOT);
      this.filtered.clear();
      for (int i = 0; i < this.items.size(); i++) {
        if (needle.isEmpty()
            || this.items.get(i).label().toLowerCase(Locale.ROOT).contains(needle)) {
          this.filtered.add(i);
        }
      }
      this.scrollOffset = 0;
    }

    private int listTop() {
      return this.posY + 1 + (this.withFilter ? FILTER_HEIGHT : 0);
    }

    private int maxVisible() {
      return this.withFilter
          ? MAX_DROPDOWN_ENTRIES
          : Math.min(this.items.size(), MAX_DROPDOWN_ENTRIES);
    }

    @Override
    public int getX() {
      return this.posX;
    }

    @Override
    public int getY() {
      return this.posY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (!this.visible) {
        return;
      }

      ColorPalette palette = ColorPalette.current();
      int x = this.posX;
      int y = this.posY;
      Font font = Minecraft.getInstance().font;

      graphics.fill(x + 2, y + 2, x + this.width + 2, y + this.height + 2, 0x55000000);
      fillRoundedRect(graphics, x, y, this.width, this.height, palette.surfaceContainer());
      drawBorderRoundedBevel(graphics, x, y, this.width, this.height, palette.outline());

      if (this.withFilter) {
        this.filterInput.render(graphics, mouseX, mouseY, partialTick);
      }

      int listTop = this.listTop();
      this.enableScissor(graphics);
      int visibleRows = Math.min(this.maxVisible(), this.filtered.size() - this.scrollOffset);
      for (int row = 0; row < visibleRows; row++) {
        int optionIndex = this.filtered.get(this.scrollOffset + row);
        int entryY = listTop + row * ENTRY_HEIGHT;
        boolean itemHovered =
            mouseX >= x
                && mouseX < x + this.width
                && mouseY >= entryY
                && mouseY < entryY + ENTRY_HEIGHT;
        if (itemHovered) {
          graphics.fill(
              x + 1, entryY, x + this.width - 1, entryY + ENTRY_HEIGHT, palette.listHover());
        }
        int textY =
            entryY + (ENTRY_HEIGHT - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2;
        ScaledText.draw(
            graphics,
            font,
            this.items.get(optionIndex).label(),
            x + 5,
            textY,
            palette.onSurface(),
            ScaledText.SCALE_BODY);
      }
      this.disableScissor(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!this.isMouseOver(mouseX, mouseY)) {
        return false;
      }

      if (this.withFilter && this.filterInput.isMouseOver(mouseX, mouseY)) {
        this.filterInput.mouseClicked(mouseX, mouseY, button);
        return true;
      }

      int row = (int) ((mouseY - this.listTop()) / ENTRY_HEIGHT);
      int filteredIndex = this.scrollOffset + row;
      if (row >= 0
          && row < this.maxVisible()
          && filteredIndex >= 0
          && filteredIndex < this.filtered.size()) {
        this.onSelect.accept(this.filtered.get(filteredIndex));
        return true;
      }

      return false;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
      return this.withFilter && this.filterInput.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
      return this.withFilter && this.filterInput.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
      int maxOffset = Math.max(0, this.filtered.size() - this.maxVisible());
      this.scrollOffset =
          Math.max(0, Math.min(maxOffset, this.scrollOffset - (int) Math.signum(delta)));
      return true;
    }

    @Override
    public void tick() {
      super.tick();
      if (this.withFilter) {
        this.filterInput.tick();
      }
    }
  }
}
