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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class SelectBox<T> extends AbstractButton {

  private static final int ARROW_W = 14;
  private static final int MAX_DROPDOWN_ENTRIES = 6;
  private static final int ENTRY_H = 16;

  private final List<SelectOption<T>> options;
  private final Consumer<T> onChange;
  private final Consumer<Panel> openOverlay;
  private final Runnable closeOverlay;
  private int selectedIndex = -1;

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
    if (selectedIndex >= this.options.size()) selectedIndex = -1;
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setSelectedIndex(int index) {
    this.selectedIndex = (index >= 0 && index < options.size()) ? index : -1;
  }

  public T getSelectedValue() {
    return (selectedIndex >= 0 && selectedIndex < options.size())
        ? options.get(selectedIndex).value()
        : null;
  }

  public void selectByValue(T value) {
    for (int i = 0; i < options.size(); i++) {
      if (options.get(i).value() != null && options.get(i).value().equals(value)) {
        selectedIndex = i;
        return;
      }
    }
    selectedIndex = -1;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) return;
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    hovered = isMouseOver(mouseX, mouseY);

    int bgColor =
        !active
            ? palette.surfaceContainerLow()
            : (pressed || hovered) ? palette.surfaceContainerHigh() : palette.surfaceContainer();
    fillRoundedRect(graphics, x, y, width, height, bgColor);
    drawBorderRoundedBevel(graphics, x, y, width, height, palette.outline());

    Font font = Minecraft.getInstance().font;
    String label =
        (selectedIndex >= 0 && selectedIndex < options.size())
            ? options.get(selectedIndex).label()
            : "—";
    int textColor = active ? palette.onSurface() : palette.onSurfaceLow();
    int textY = y + (height - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2;
    ScaledText.draw(graphics, font, label, x + 4, textY, textColor, ScaledText.SCALE_BODY);

    int arrowX = x + width - ARROW_W;
    graphics.fill(arrowX, y + 1, arrowX + 1, y + height - 1, palette.outline());
    ScaledText.drawCentered(
        graphics,
        font,
        "\u25BE",
        arrowX + ARROW_W / 2,
        y + (height - font.lineHeight) / 2,
        textColor,
        ScaledText.SCALE_NORMAL);
  }

  @Override
  protected void onPress() {
    if (openOverlay == null) return;
    int absX = getX();
    int absY = getY() + height;
    int dropH = Math.min(options.size(), MAX_DROPDOWN_ENTRIES) * ENTRY_H + 2;
    DropdownPanel<T> dropdown =
        new DropdownPanel<>(
            absX,
            absY,
            width,
            dropH,
            options,
            idx -> {
              selectedIndex = idx;
              if (onChange != null) onChange.accept(options.get(idx).value());
              if (closeOverlay != null) closeOverlay.run();
            });
    openOverlay.accept(dropdown);
  }

  private static class DropdownPanel<T> extends Panel {

    private final List<SelectOption<T>> items;
    private final Consumer<Integer> onSelect;
    private int hoveredIndex = -1;

    DropdownPanel(
        int absX,
        int absY,
        int width,
        int height,
        List<SelectOption<T>> items,
        Consumer<Integer> onSelect) {
      super(absX, absY, width, height);
      this.items = items;
      this.onSelect = onSelect;
      this.clipChildren = true;
    }

    @Override
    public int getX() {
      return posX;
    }

    @Override
    public int getY() {
      return posY;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
      if (!visible) return;
      ColorPalette palette = ColorPalette.current();
      int x = posX;
      int y = posY;
      Font font = Minecraft.getInstance().font;

      // Background + shadow
      graphics.fill(x + 2, y + 2, x + width + 2, y + height + 2, 0x55000000);
      fillRoundedRect(graphics, x, y, width, height, palette.surfaceContainer());
      drawBorderRoundedBevel(graphics, x, y, width, height, palette.outline());

      enableScissor(graphics);
      for (int i = 0; i < items.size(); i++) {
        int ey = y + 1 + i * ENTRY_H;
        boolean itemHovered =
            mouseX >= x && mouseX < x + width && mouseY >= ey && mouseY < ey + ENTRY_H;
        if (itemHovered) hoveredIndex = i;
        int bg = itemHovered ? palette.listHover() : 0x00000000;
        if (bg != 0) graphics.fill(x + 1, ey, x + width - 1, ey + ENTRY_H, bg);
        int textY = ey + (ENTRY_H - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2;
        ScaledText.draw(
            graphics,
            font,
            items.get(i).label(),
            x + 5,
            textY,
            palette.onSurface(),
            ScaledText.SCALE_BODY);
      }
      disableScissor(graphics);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
      if (!isMouseOver(mouseX, mouseY)) return false;
      int clickedIdx = (int) ((mouseY - posY - 1) / ENTRY_H);
      if (clickedIdx >= 0 && clickedIdx < items.size()) {
        onSelect.accept(clickedIdx);
        return true;
      }
      return false;
    }
  }
}
