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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Widget;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class TabBar extends Widget {

  private final List<Tab> tabs = new ArrayList<>();
  private int selectedIndex = 0;
  private int tabHeight = 20;
  private Consumer<Integer> onTabChanged;

  public TabBar(int posX, int posY, int width) {
    super(posX, posY, width, 20);
  }

  public void addTab(String label, Panel contentPanel) {
    tabs.add(new Tab(label, contentPanel));
    updateTabVisibility();
  }

  public void setOnTabChanged(Consumer<Integer> onTabChanged) {
    this.onTabChanged = onTabChanged;
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setSelectedIndex(int index) {
    if (index >= 0 && index < tabs.size()) {
      selectedIndex = index;
      updateTabVisibility();
      if (onTabChanged != null) {
        onTabChanged.accept(index);
      }
    }
  }

  public Panel getSelectedPanel() {
    if (selectedIndex >= 0 && selectedIndex < tabs.size()) {
      return tabs.get(selectedIndex).panel;
    }
    return null;
  }

  private void updateTabVisibility() {
    for (int i = 0; i < tabs.size(); i++) {
      tabs.get(i).panel.setVisible(i == selectedIndex);
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible || tabs.isEmpty()) {
      return;
    }
    ColorPalette palette = ColorPalette.current();
    Font font = Minecraft.getInstance().font;
    int x = getX();
    int y = getY();
    int tabWidth = width / tabs.size();

    for (int i = 0; i < tabs.size(); i++) {
      int tabX = x + i * tabWidth;
      boolean isSelected = i == selectedIndex;
      boolean isHovered =
          mouseX >= tabX && mouseX < tabX + tabWidth && mouseY >= y && mouseY < y + tabHeight;

      int bgColor;
      if (isSelected) {
        bgColor = palette.surface();
      } else if (isHovered) {
        bgColor = palette.surfaceContainerHigh();
      } else {
        bgColor = palette.surfaceContainer();
      }

      graphics.fill(tabX, y, tabX + tabWidth, y + tabHeight, bgColor);

      if (isSelected) {
        graphics.fill(tabX + 1, y, tabX + tabWidth - 1, y + 2, palette.listHighlight());
      }

      int borderLight = lighten(palette.outline(), 0.30f);
      int borderDark = darken(palette.outline(), 0.20f);
      graphics.fill(tabX + 1, y, tabX + tabWidth - 1, y + 1, borderLight); // top
      graphics.fill(tabX, y + 1, tabX + 1, y + tabHeight - 1, borderLight); // left
      graphics.fill(
          tabX + tabWidth - 1, y + 1, tabX + tabWidth, y + tabHeight - 1, borderDark); // right
      if (!isSelected) {
        graphics.fill(
            tabX + 1, y + tabHeight - 1, tabX + tabWidth - 1, y + tabHeight, borderDark); // bottom
      }

      int textColor = isSelected ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.drawCentered(
          graphics,
          font,
          tabs.get(i).label,
          tabX + tabWidth / 2,
          y + (tabHeight - font.lineHeight) / 2 + 1,
          textColor,
          ScaledText.SCALE_SMALL);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (!visible || !active || button != 0) {
      return false;
    }
    int x = getX();
    int y = getY();
    if (mouseY < y || mouseY >= y + tabHeight) {
      return false;
    }
    int tabWidth = width / tabs.size();
    for (int i = 0; i < tabs.size(); i++) {
      int tabX = x + i * tabWidth;
      if (mouseX >= tabX && mouseX < tabX + tabWidth) {
        setSelectedIndex(i);
        return true;
      }
    }
    return false;
  }

  private record Tab(String label, Panel panel) {}
}
