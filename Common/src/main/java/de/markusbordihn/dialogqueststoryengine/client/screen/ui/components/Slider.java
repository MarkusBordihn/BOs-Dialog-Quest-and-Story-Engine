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
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

public class Slider extends Widget {

  private static final int TRACK_H = 4;
  private static final int THUMB_D = 10;

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
    this.value = clamp(initialValue);
    this.onChange = onChange;
  }

  private static String formatValue(float v) {
    if (v == Math.floor(v) && !Float.isInfinite(v)) {
      return String.valueOf((int) v);
    }
    return String.format("%.1f", v);
  }

  public float getValue() {
    return value;
  }

  public void setValue(float value) {
    this.value = clamp(value);
  }

  public void setMin(float min) {
    this.min = min;
    this.value = clamp(value);
  }

  public void setMax(float max) {
    this.max = max;
    this.value = clamp(value);
  }

  public void setStep(float step) {
    this.step = step;
  }

  public void setOnChange(Consumer<Float> onChange) {
    this.onChange = onChange;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) return;
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();
    hovered = isMouseOver(mouseX, mouseY);

    int trackY = y + (height - TRACK_H) / 2;
    fillRect(
        graphics, x, trackY, width, TRACK_H, active ? palette.outline() : palette.onSurfaceLow());
    int thumbX = thumbCenterX();
    if (thumbX > x) {
      fillRect(
          graphics,
          x,
          trackY,
          thumbX - x,
          TRACK_H,
          active ? palette.primary() : palette.onSurfaceLow());
    }

    int thumbLeft = thumbX - THUMB_D / 2;
    int thumbTop = y + (height - THUMB_D) / 2;
    int thumbColor =
        active
            ? (hovered || dragging ? darken(palette.primary(), 0.15f) : palette.primary())
            : palette.onSurfaceLow();
    fillRoundedRect(graphics, thumbLeft, thumbTop, THUMB_D, THUMB_D, thumbColor);
    drawBorderRounded(
        graphics,
        thumbLeft,
        thumbTop,
        THUMB_D,
        THUMB_D,
        active ? palette.primaryVariant() : palette.outline());

    if (active) {
      Font font = Minecraft.getInstance().font;
      String label = formatValue(value);
      int labelW = ScaledText.getScaledWidth(font, label, ScaledText.SCALE_BODY);
      int labelX = thumbX - labelW / 2;
      int labelY = thumbTop - ScaledText.getScaledHeight(font, ScaledText.SCALE_BODY) - 2;
      if (labelY < 0) labelY = thumbTop + THUMB_D + 2;
      ScaledText.draw(
          graphics, font, label, labelX, labelY, palette.onSurface(), ScaledText.SCALE_BODY);
    }
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (active && visible && button == 0 && isMouseOver(mouseX, mouseY)) {
      dragging = true;
      setValueFromX((int) mouseX);
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    if (dragging && button == 0) {
      dragging = false;
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseDragged(
      double mouseX, double mouseY, int button, double dragX, double dragY) {
    if (dragging && active) {
      setValueFromX((int) mouseX);
      return true;
    }
    return false;
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    if (active && visible && isMouseOver(mouseX, mouseY)) {
      float newVal = clamp(value + (float) delta * step);
      if (newVal != value) {
        value = newVal;
        fireChange();
      }
      return true;
    }
    return false;
  }

  private int thumbCenterX() {
    int x = getX();
    float ratio = (max == min) ? 0f : (value - min) / (max - min);
    return x + (int) (ratio * width);
  }

  private void setValueFromX(int mouseX) {
    int x = getX();
    float ratio = Math.max(0f, Math.min(1f, (mouseX - x) / (float) width));
    float raw = min + ratio * (max - min);
    float snapped = (step > 0) ? Math.round(raw / step) * step : raw;
    float clamped = clamp(snapped);
    if (clamped != value) {
      value = clamped;
      fireChange();
    }
  }

  private float clamp(float v) {
    return Math.max(min, Math.min(max, v));
  }

  private void fireChange() {
    if (onChange != null) {
      onChange.accept(value);
    }
  }
}
