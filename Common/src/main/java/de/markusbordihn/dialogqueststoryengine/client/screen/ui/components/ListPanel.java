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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.ScaledText;
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
    return items;
  }

  public void setItems(List<T> items) {
    this.items.clear();
    this.items.addAll(items);
    this.selectedIndex = -1;
    setScrollY(0);
    this.contentHeight = items.size() * entryHeight + padding;
  }

  public void setEntryHeight(int entryHeight) {
    this.entryHeight = entryHeight;
    this.contentHeight = items.size() * entryHeight + padding;
  }

  public void setOnSelect(Consumer<T> onSelect) {
    this.onSelect = onSelect;
  }

  public void setEntryRenderer(EntryRenderer<T> renderer) {
    this.entryRenderer = renderer;
  }

  public T getSelected() {
    return selectedIndex >= 0 && selectedIndex < items.size() ? items.get(selectedIndex) : null;
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setSelectedIndex(int index) {
    this.selectedIndex = index;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }
    renderBackground(graphics, mouseX, mouseY, partialTick);
    if (clipChildren) {
      enableScissor(graphics);
    }

    ColorPalette palette = ColorPalette.current();
    Font font = Minecraft.getInstance().font;
    int contentX = getContentX();
    int contentY = getContentY();
    int listWidth = getInnerWidth() - SCROLLBAR_INSET;

    for (int i = 0; i < items.size(); i++) {
      int entryY = contentY + i * entryHeight;
      if (entryY + entryHeight < getY() || entryY > getY() + height) {
        continue;
      }

      int bgColor;
      if (i == selectedIndex) {
        bgColor = palette.listHighlight();
      } else if (mouseX >= contentX
          && mouseX < contentX + listWidth
          && mouseY >= entryY
          && mouseY < entryY + entryHeight) {
        bgColor = palette.listHover();
      } else {
        bgColor = (i % 2 == 0) ? palette.listStripe() : 0x00000000;
      }

      graphics.fill(contentX, entryY, contentX + listWidth, entryY + entryHeight, bgColor);

      if (entryRenderer != null) {
        entryRenderer.render(
            graphics,
            font,
            items.get(i),
            i,
            contentX + 2,
            entryY + 1,
            listWidth - 4,
            entryHeight - 2,
            palette);
      } else {
        ScaledText.draw(
            graphics,
            font,
            items.get(i).toString(),
            contentX + 4,
            entryY + (entryHeight - font.lineHeight) / 2,
            palette.onSurface(),
            ScaledText.SCALE_SMALL);
      }
    }

    if (clipChildren) {
      disableScissor(graphics);
    }

    for (Widget child : children) {
      if (child.isVisible()) {
        child.render(graphics, mouseX, mouseY, partialTick);
      }
    }

    renderForeground(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  protected void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    graphics.fill(x, y, x + width, y + height, palette.surface());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!visible || !active || !isMouseOver(mouseX, mouseY)) {
      return false;
    }
    if (super.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    int contentX = getContentX();
    int contentY = getContentY();
    int listWidth = getInnerWidth() - SCROLLBAR_INSET;

    if (mouseX >= contentX && mouseX < contentX + listWidth) {
      int clickedIndex = (int) ((mouseY - contentY) / entryHeight);
      if (clickedIndex >= 0 && clickedIndex < items.size()) {
        selectedIndex = clickedIndex;
        if (onSelect != null) {
          onSelect.accept(items.get(clickedIndex));
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
