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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Checkbox extends AbstractButton {

  private static final int BOX_SIZE = 14;
  private static final int LABEL_GAP = 6;
  private static final String CHECK_MARK = "\u2713";

  private boolean checked;
  private String label;
  private Consumer<Boolean> onChange;

  public Checkbox(int posX, int posY, boolean initialState, Consumer<Boolean> onChange) {
    this(posX, posY, null, initialState, onChange);
  }

  public Checkbox(
      int posX, int posY, String label, boolean initialState, Consumer<Boolean> onChange) {
    super(posX, posY, BOX_SIZE, BOX_SIZE);
    this.label = label;
    this.checked = initialState;
    this.onChange = onChange;
    updateWidth();
  }

  public boolean isChecked() {
    return checked;
  }

  public void setChecked(boolean checked) {
    this.checked = checked;
  }

  public String getLabel() {
    return label;
  }

  public void setLabel(String label) {
    this.label = label;
    updateWidth();
  }

  public void setOnChange(Consumer<Boolean> onChange) {
    this.onChange = onChange;
  }

  @Override
  protected void onPress() {
    checked = !checked;
    if (onChange != null) {
      onChange.accept(checked);
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) return;
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    hovered = isMouseOver(mouseX, mouseY);

    int bgColor;
    if (!active) {
      bgColor = palette.surfaceContainerLow();
    } else if (checked) {
      bgColor = hovered ? darken(palette.primary(), 0.15f) : palette.primary();
    } else {
      bgColor = hovered ? palette.surfaceContainerHigh() : palette.surfaceContainer();
    }
    fillRoundedRect(graphics, x, y, BOX_SIZE, BOX_SIZE, bgColor);

    int borderColor =
        !active
            ? palette.onSurfaceLow()
            : checked ? bgColor : (hovered ? palette.primary() : palette.outline());
    drawBorderRounded(graphics, x, y, BOX_SIZE, BOX_SIZE, borderColor);

    if (checked) {
      Font font = Minecraft.getInstance().font;
      ScaledText.drawCentered(
          graphics,
          font,
          CHECK_MARK,
          x + BOX_SIZE / 2,
          y + 2,
          active ? palette.onPrimary() : palette.onSurfaceLow(),
          ScaledText.SCALE_NORMAL);
    }

    if (label != null && !label.isEmpty()) {
      Font font = Minecraft.getInstance().font;
      int textColor = active ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.draw(
          graphics,
          font,
          label,
          x + BOX_SIZE + LABEL_GAP,
          y + (BOX_SIZE - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2,
          textColor,
          ScaledText.SCALE_BODY);
    }
  }

  private void updateWidth() {
    if (label != null && !label.isEmpty()) {
      Font font = Minecraft.getInstance().font;
      width = BOX_SIZE + LABEL_GAP + ScaledText.getScaledWidth(font, label, ScaledText.SCALE_BODY);
    } else {
      width = BOX_SIZE;
    }
  }
}
