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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Widget;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ListPanel<T> extends ScrollPanel {

  private static final int SCROLLBAR_INSET = 8;

  private final List<T> items = new ArrayList<>();
  private int selectedIndex = -1;
  private int entryHeight = 18;
  private Consumer<T> onSelect;
  private EntryRenderer<T> entryRenderer;

  public ListPanel(int posX, int posY, int width, int height) {
    super(posX, posY, width, height);
  }

  public List<T> getItems() {
    return this.items;
  }

  public void setItems(List<T> items) {
    this.items.clear();
    this.items.addAll(items);
    this.selectedIndex = -1;
    this.setScrollY(0);
    this.contentHeight = items.size() * this.entryHeight + this.padding;
  }

  public void setEntryHeight(int entryHeight) {
    this.entryHeight = entryHeight;
    this.contentHeight = this.items.size() * entryHeight + this.padding;
  }

  public void setOnSelect(Consumer<T> onSelect) {
    this.onSelect = onSelect;
  }

  public void setEntryRenderer(EntryRenderer<T> renderer) {
    this.entryRenderer = renderer;
  }

  public T getSelected() {
    return this.selectedIndex >= 0 && this.selectedIndex < this.items.size()
        ? this.items.get(this.selectedIndex)
        : null;
  }

  public int getSelectedIndex() {
    return this.selectedIndex;
  }

  public void setSelectedIndex(int index) {
    this.selectedIndex = index;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }
    this.renderBackground(graphics, mouseX, mouseY, partialTick);
    if (this.clipChildren) {
      this.enableScissor(graphics);
    }

    ColorPalette palette = ColorPalette.current();
    Font font = Minecraft.getInstance().font;
    int contentX = this.getContentX();
    int contentY = this.getContentY();
    int listWidth = this.getInnerWidth() - SCROLLBAR_INSET;

    for (int i = 0; i < this.items.size(); i++) {
      int entryY = contentY + i * this.entryHeight;
      if (entryY + this.entryHeight < this.getY() || entryY > this.getY() + this.height) {
        continue;
      }

      int backgroundColor;
      if (i == this.selectedIndex) {
        backgroundColor = palette.listHighlight();
      } else if (mouseX >= contentX
          && mouseX < contentX + listWidth
          && mouseY >= entryY
          && mouseY < entryY + this.entryHeight) {
        backgroundColor = palette.listHover();
      } else {
        backgroundColor = (i % 2 == 0) ? palette.listStripe() : 0x00000000;
      }

      graphics.fill(
          contentX, entryY, contentX + listWidth, entryY + this.entryHeight, backgroundColor);

      if (this.entryRenderer != null) {
        this.entryRenderer.render(
            graphics,
            font,
            this.items.get(i),
            i,
            contentX + 2,
            entryY + 1,
            listWidth - 4,
            this.entryHeight - 2,
            palette);
      } else {
        ScaledText.draw(
            graphics,
            font,
            this.items.get(i).toString(),
            contentX + 4,
            entryY + (this.entryHeight - font.lineHeight) / 2,
            palette.onSurface(),
            ScaledText.SCALE_SMALL);
      }
    }

    if (this.clipChildren) {
      this.disableScissor(graphics);
    }

    for (Widget child : this.children) {
      if (child.isVisible()) {
        child.render(graphics, mouseX, mouseY, partialTick);
      }
    }

    this.renderForeground(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    ColorPalette palette = ColorPalette.current();
    int x = this.getX();
    int y = this.getY();
    graphics.fill(x, y, x + this.width, y + this.height, palette.surface());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!this.visible || !this.active || !this.isMouseOver(mouseX, mouseY)) {
      return false;
    }

    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }

    int contentX = this.getContentX();
    int contentY = this.getContentY();
    int listWidth = this.getInnerWidth() - SCROLLBAR_INSET;
    if (mouseX >= contentX && mouseX < contentX + listWidth) {
      int clickedIndex = (int) ((mouseY - contentY) / this.entryHeight);
      if (clickedIndex >= 0 && clickedIndex < this.items.size()) {
        this.selectedIndex = clickedIndex;
        if (this.onSelect != null) {
          this.onSelect.accept(this.items.get(clickedIndex));
        }
        return true;
      }
    }
    return false;
  }

  @FunctionalInterface
  public interface EntryRenderer<T> {
    void render(
        GuiGraphics graphics,
        Font font,
        T item,
        int index,
        int x,
        int y,
        int width,
        int height,
        ColorPalette palette);
  }
}
