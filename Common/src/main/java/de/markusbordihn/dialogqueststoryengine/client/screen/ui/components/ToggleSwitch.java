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
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class ToggleSwitch extends AbstractButton {

  private static final int TRACK_W = 22;
  private static final int TRACK_H = 14;
  private static final int THUMB_D = 10;
  private static final int LABEL_GAP = 6;

  private boolean toggled;
  private String label;
  private Consumer<Boolean> onChange;

  public ToggleSwitch(int posX, int posY, boolean initialState, Consumer<Boolean> onChange) {
    this(posX, posY, null, initialState, onChange);
  }

  public ToggleSwitch(
      int posX, int posY, String label, boolean initialState, Consumer<Boolean> onChange) {
    super(posX, posY, TRACK_W, TRACK_H);
    this.label = label;
    this.toggled = initialState;
    this.onChange = onChange;
    updateWidth();
  }

  public boolean isToggled() {
    return toggled;
  }

  public void setToggled(boolean toggled) {
    this.toggled = toggled;
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
    toggled = !toggled;
    if (onChange != null) {
      onChange.accept(toggled);
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

    int trackColor;
    if (!active) {
      trackColor = palette.surfaceContainerLow();
    } else if (toggled) {
      trackColor = hovered ? darken(palette.primary(), 0.15f) : palette.primary();
    } else {
      trackColor = hovered ? palette.surfaceContainerHigh() : palette.outline();
    }
    fillRoundedRect(graphics, x, y, TRACK_W, TRACK_H, trackColor);
    if (!toggled || !active) {
      drawBorderRounded(
          graphics, x, y, TRACK_W, TRACK_H, active ? palette.outline() : palette.onSurfaceLow());
    }

    int thumbPad = (TRACK_H - THUMB_D) / 2;
    int thumbX = toggled ? x + TRACK_W - THUMB_D - thumbPad : x + thumbPad;
    fillRoundedRect(
        graphics,
        thumbX,
        y + thumbPad,
        THUMB_D,
        THUMB_D,
        active ? (toggled ? palette.onPrimary() : palette.surface()) : palette.onSurfaceLow());

    if (label != null && !label.isEmpty()) {
      Font font = Minecraft.getInstance().font;
      int textColor = active ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.draw(
          graphics,
          font,
          label,
          x + TRACK_W + LABEL_GAP,
          y + (TRACK_H - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2,
          textColor,
          ScaledText.SCALE_BODY);
    }
  }

  private void updateWidth() {
    if (label != null && !label.isEmpty()) {
      Font font = Minecraft.getInstance().font;
      width = TRACK_W + LABEL_GAP + ScaledText.getScaledWidth(font, label, ScaledText.SCALE_BODY);
    } else {
      width = TRACK_W;
    }
  }
}
