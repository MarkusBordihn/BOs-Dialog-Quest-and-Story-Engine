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
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Slider extends Widget {

  private static final int TRACK_HEIGHT = 4;
  private static final int THUMB_DIAMETER = 10;

  private float min;
  private float max;
  private float step;
  private float value;
  private boolean dragging;
  private Consumer<Float> onChange;

  private boolean hovered;

  public Slider(
      int posX,
      int posY,
      int width,
      int height,
      float min,
      float max,
      float step,
      float initialValue,
      Consumer<Float> onChange) {
    super(posX, posY, width, height);
    this.min = min;
    this.max = max;
    this.step = step;
    this.value = this.clamp(initialValue);
    this.onChange = onChange;
  }

  private static String formatValue(float value) {
    if (value == Math.floor(value) && !Float.isInfinite(value)) {
      return String.valueOf((int) value);
    }

    return String.format("%.1f", value);
  }

  public float getValue() {
    return this.value;
  }

  public void setValue(float value) {
    this.value = this.clamp(value);
  }

  public void setMin(float min) {
    this.min = min;
    this.value = this.clamp(this.value);
  }

  public void setMax(float max) {
    this.max = max;
    this.value = this.clamp(this.value);
  }

  public void setStep(float step) {
    this.step = step;
  }

  public void setOnChange(Consumer<Float> onChange) {
    this.onChange = onChange;
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

    int trackY = y + (this.height - TRACK_HEIGHT) / 2;
    fillRect(
        graphics,
        x,
        trackY,
        this.width,
        TRACK_HEIGHT,
        this.active ? palette.outline() : palette.onSurfaceLow());
    int thumbX = this.thumbCenterX();
    if (thumbX > x) {
      fillRect(
          graphics,
          x,
          trackY,
          thumbX - x,
          TRACK_HEIGHT,
          this.active ? palette.primary() : palette.onSurfaceLow());
    }

    int thumbLeft = thumbX - THUMB_DIAMETER / 2;
    int thumbTop = y + (this.height - THUMB_DIAMETER) / 2;
    int thumbColor;
    if (!this.active) {
      thumbColor = palette.onSurfaceLow();
    } else if (this.hovered || this.dragging) {
      thumbColor = darken(palette.primary(), 0.15f);
    } else {
      thumbColor = palette.primary();
    }
    fillRoundedRect(graphics, thumbLeft, thumbTop, THUMB_DIAMETER, THUMB_DIAMETER, thumbColor);
    drawBorderRounded(
        graphics,
        thumbLeft,
        thumbTop,
        THUMB_DIAMETER,
        THUMB_DIAMETER,
        this.active ? palette.primaryVariant() : palette.outline());

    if (this.active) {
      Font font = Minecraft.getInstance().font;
      String label = formatValue(this.value);
      int labelWidth = ScaledText.getScaledWidth(font, label, ScaledText.SCALE_BODY);
      int labelX = thumbX - labelWidth / 2;
      int labelY = thumbTop - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY) - 2;
      if (labelY < 0) {
        labelY = thumbTop + THUMB_DIAMETER + 2;
      }
      ScaledText.draw(
          graphics, font, label, labelX, labelY, palette.onSurface(), ScaledText.SCALE_BODY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.active && this.visible && button == 0 && this.isMouseOver(mouseX, mouseY)) {
      this.dragging = true;
      this.setValueFromX((int) mouseX);
      return true;
    }

    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (this.dragging && button == 0) {
      this.dragging = false;
      return true;
    }

    return false;
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (this.dragging && this.active) {
      this.setValueFromX((int) mouseX);
      return true;
    }

    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (this.active && this.visible && this.isMouseOver(mouseX, mouseY)) {
      float newValue = this.clamp(this.value + (float) delta * this.step);
      if (newValue != this.value) {
        this.value = newValue;
        this.fireChange();
      }
      return true;
    }

    return false;
  }

  private int thumbCenterX() {
    int x = this.getX();
    float ratio = (this.max == this.min) ? 0f : (this.value - this.min) / (this.max - this.min);
    return x + (int) (ratio * this.width);
  }

  private void setValueFromX(int mouseX) {
    int x = this.getX();
    float ratio = Math.max(0f, Math.min(1f, (mouseX - x) / (float) this.width));
    float raw = this.min + ratio * (this.max - this.min);
    float snapped = (this.step > 0) ? Math.round(raw / this.step) * this.step : raw;
    float clamped = this.clamp(snapped);
    if (clamped != this.value) {
      this.value = clamped;
      this.fireChange();
    }
  }

  private float clamp(float rawValue) {
    return Math.max(this.min, Math.min(this.max, rawValue));
  }

  private void fireChange() {
    if (this.onChange != null) {
      this.onChange.accept(this.value);
    }
  }
}
