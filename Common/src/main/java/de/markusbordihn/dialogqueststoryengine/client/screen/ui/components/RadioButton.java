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
    this.updateWidth();
  }

  public boolean isSelected() {
    return this.selected;
  }

  public void setSelected(boolean selected) {
    this.selected = selected;
  }

  public Component getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = label != null ? TextComponent.of(label) : null;
    this.updateWidth();
  }

  public void setLabel(Component label) {
    this.label = label;
    this.updateWidth();
  }

  @Override
  protected void onPress() {
    if (this.ownerGroup != null) {
      this.ownerGroup.selectIndex(this.groupIndex);
    } else {
      this.selected = true;
    }
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

    int ringColor;
    if (!this.active) {
      ringColor = palette.onSurfaceLow();
    } else if (this.selected || this.hovered) {
      ringColor = palette.primary();
    } else {
      ringColor = palette.outline();
    }
    drawBorderRounded(graphics, x, y, CIRCLE_SIZE, CIRCLE_SIZE, ringColor);

    if (this.hovered && !this.selected && this.active) {
      fillRoundedRect(
          graphics, x + 1, y + 1, CIRCLE_SIZE - 2, CIRCLE_SIZE - 2, palette.listHover());
    }

    if (this.selected) {
      int dotColor = this.active ? palette.primary() : palette.onSurfaceLow();
      int innerPadding = 3;
      fillRoundedRect(
          graphics,
          x + innerPadding,
          y + innerPadding,
          CIRCLE_SIZE - innerPadding * 2,
          CIRCLE_SIZE - innerPadding * 2,
          dotColor);
    }

    if (this.label != null && !this.label.getString().isEmpty()) {
      Font font = Minecraft.getInstance().font;
      int textColor = this.active ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.draw(
          graphics,
          font,
          this.label,
          x + CIRCLE_SIZE + LABEL_GAP,
          y + (CIRCLE_SIZE - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2,
          textColor,
          ScaledText.SCALE_BODY);
    }
  }

  private void updateWidth() {
    if (this.label != null && !this.label.getString().isEmpty()) {
      Font font = Minecraft.getInstance().font;
      this.width =
          CIRCLE_SIZE
              + LABEL_GAP
              + ScaledText.getScaledWidth(font, this.label, ScaledText.SCALE_BODY);
    } else {
      this.width = CIRCLE_SIZE;
    }
  }
}
