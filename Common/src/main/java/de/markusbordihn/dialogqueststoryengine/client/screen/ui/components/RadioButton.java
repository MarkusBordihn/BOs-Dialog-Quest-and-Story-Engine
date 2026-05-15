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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class RadioButton extends AbstractButton {

  private static final int CIRCLE_SIZE = 12;
  private static final int LABEL_GAP = 6;
  int groupIndex = -1;
  RadioGroup ownerGroup;
  private boolean selected;
  private Component label;

  public RadioButton(int posX, int posY, boolean initialSelected) {
    this(posX, posY, (Component) null, initialSelected);
  }

  public RadioButton(int posX, int posY, String label, boolean initialSelected) {
    this(posX, posY, label != null ? TextComponent.of(label) : null, initialSelected);
  }

  public RadioButton(int posX, int posY, Component label, boolean initialSelected) {
    super(posX, posY, CIRCLE_SIZE, CIRCLE_SIZE);
    this.label = label;
    this.selected = initialSelected;
    updateWidth();
  }

  public boolean isSelected() {
    return selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public Component getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label != null ? TextComponent.of(label) : null;
    updateWidth();
  }

  public void setLabel(Component label) {
    this.label = label;
    updateWidth();
  }

  @Override
  protected void onPress() {
    if (ownerGroup != null) {
      ownerGroup.selectIndex(groupIndex);
    } else {
      selected = true;
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    hovered = isMouseOver(mouseX, mouseY);

    int ringColor =
        !active
            ? palette.onSurfaceLow()
            : selected ? palette.primary() : (hovered ? palette.primary() : palette.outline());
    drawBorderRounded(graphics, x, y, CIRCLE_SIZE, CIRCLE_SIZE, ringColor);

    if (hovered && !selected && active) {
      fillRoundedRect(
          graphics, x + 1, y + 1, CIRCLE_SIZE - 2, CIRCLE_SIZE - 2, palette.listHover());
    }

    if (selected) {
      int dotColor = active ? palette.primary() : palette.onSurfaceLow();
      int innerPad = 3;
      fillRoundedRect(
          graphics,
          x + innerPad,
          y + innerPad,
          CIRCLE_SIZE - innerPad * 2,
          CIRCLE_SIZE - innerPad * 2,
          dotColor);
    }

    if (label != null && !label.getString().isEmpty()) {
      Font font = Minecraft.getInstance().font;
      int textColor = active ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.draw(
          graphics,
          font,
          label,
          x + CIRCLE_SIZE + LABEL_GAP,
          y + (CIRCLE_SIZE - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2,
          textColor,
          ScaledText.SCALE_BODY);
    }
  }

  private void updateWidth() {
    if (label != null && !label.getString().isEmpty()) {
      Font font = Minecraft.getInstance().font;
      width =
          CIRCLE_SIZE + LABEL_GAP + ScaledText.getScaledWidth(font, label, ScaledText.SCALE_BODY);
    } else {
      width = CIRCLE_SIZE;
    }
  }
}
