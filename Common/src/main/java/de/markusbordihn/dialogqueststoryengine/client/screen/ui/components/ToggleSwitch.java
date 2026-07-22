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

  private static final int TRACK_WIDTH = 22;
  private static final int TRACK_HEIGHT = 14;
  private static final int THUMB_DIAMETER = 10;
  private static final int LABEL_GAP = 6;

  private boolean toggled;
  private String label;
  private Consumer<Boolean> onChange;

  public ToggleSwitch(int posX, int posY, boolean initialState, Consumer<Boolean> onChange) {
    this(posX, posY, null, initialState, onChange);
  }

  public ToggleSwitch(
      int posX, int posY, String label, boolean initialState, Consumer<Boolean> onChange) {
    super(posX, posY, TRACK_WIDTH, TRACK_HEIGHT);
    this.label = label;
    this.toggled = initialState;
    this.onChange = onChange;
    this.updateWidth();
  }

  public boolean isToggled() {
    return this.toggled;
  }

  public void setToggled(boolean toggled) {
    this.toggled = toggled;
  }

  public String getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = label;
    this.updateWidth();
  }

  public void setOnChange(Consumer<Boolean> onChange) {
    this.onChange = onChange;
  }

  @Override
  protected void onPress() {
    this.toggled = !this.toggled;
    if (this.onChange != null) {
      this.onChange.accept(this.toggled);
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

    int trackColor;
    if (!this.active) {
      trackColor = palette.surfaceContainerLow();
    } else if (this.toggled) {
      trackColor = this.hovered ? darken(palette.primary(), 0.15f) : palette.primary();
    } else {
      trackColor = this.hovered ? palette.surfaceContainerHigh() : palette.outline();
    }
    fillRoundedRect(graphics, x, y, TRACK_WIDTH, TRACK_HEIGHT, trackColor);
    if (!this.toggled || !this.active) {
      drawBorderRounded(
          graphics,
          x,
          y,
          TRACK_WIDTH,
          TRACK_HEIGHT,
          this.active ? palette.outline() : palette.onSurfaceLow());
    }

    int thumbPadding = (TRACK_HEIGHT - THUMB_DIAMETER) / 2;
    int thumbX = this.toggled ? x + TRACK_WIDTH - THUMB_DIAMETER - thumbPadding : x + thumbPadding;
    int thumbColor;
    if (!this.active) {
      thumbColor = palette.onSurfaceLow();
    } else if (this.toggled) {
      thumbColor = palette.onPrimary();
    } else {
      thumbColor = palette.surface();
    }
    fillRoundedRect(graphics, thumbX, y + thumbPadding, THUMB_DIAMETER, THUMB_DIAMETER, thumbColor);

    if (this.label != null && !this.label.isEmpty()) {
      Font font = Minecraft.getInstance().font;
      int textColor = this.active ? palette.onSurface() : palette.onSurfaceLow();
      ScaledText.draw(
          graphics,
          font,
          this.label,
          x + TRACK_WIDTH + LABEL_GAP,
          y + (TRACK_HEIGHT - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY)) / 2,
          textColor,
          ScaledText.SCALE_BODY);
    }
  }

  private void updateWidth() {
    if (this.label != null && !this.label.isEmpty()) {
      Font font = Minecraft.getInstance().font;
      this.width =
          TRACK_WIDTH
              + LABEL_GAP
              + ScaledText.getScaledWidth(font, this.label, ScaledText.SCALE_BODY);
    } else {
      this.width = TRACK_WIDTH;
    }
  }
}
