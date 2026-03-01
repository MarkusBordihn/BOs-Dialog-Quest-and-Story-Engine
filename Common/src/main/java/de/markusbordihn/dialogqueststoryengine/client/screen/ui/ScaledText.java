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

package de.markusbordihn.dialogqueststoryengine.client.screen.ui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.network.chat.Component;

public final class ScaledText {

  public static final float SCALE_NORMAL = 1.0f;
  public static final float SCALE_SMALL = 0.75f;
  public static final float SCALE_COMPACT = 0.5f;

  public static final float SCALE_HEADING = SCALE_NORMAL;

  public static final float SCALE_BODY = SCALE_SMALL;

  public static final float SCALE_CAPTION = SCALE_COMPACT;

  private ScaledText() {}

  public static String resolveText(String text) {
    if (text != null && text.matches("[a-z][a-z0-9_]*(\\.[a-z0-9_]+)+")) {
      return I18n.get(text);
    }
    return text;
  }

  public static void draw(
      GuiGraphics graphics, Font font, String text, int x, int y, int color, float scale) {
    text = resolveText(text);
    if (scale == SCALE_NORMAL) {
      graphics.drawString(font, text, x, y, color, false);
      return;
    }
    graphics.pose().pushPose();
    graphics.pose().translate(x, y, 0);
    graphics.pose().scale(scale, scale, 1.0f);
    graphics.drawString(font, text, 0, 0, color, false);
    graphics.pose().popPose();
  }

  public static void draw(
      GuiGraphics graphics, Font font, Component text, int x, int y, int color, float scale) {
    if (scale == SCALE_NORMAL) {
      graphics.drawString(font, text, x, y, color, false);
      return;
    }
    graphics.pose().pushPose();
    graphics.pose().translate(x, y, 0);
    graphics.pose().scale(scale, scale, 1.0f);
    graphics.drawString(font, text, 0, 0, color, false);
    graphics.pose().popPose();
  }

  public static void drawShadow(
      GuiGraphics graphics, Font font, String text, int x, int y, int color, float scale) {
    text = resolveText(text);
    if (scale == SCALE_NORMAL) {
      graphics.drawString(font, text, x, y, color, true);
      return;
    }
    graphics.pose().pushPose();
    graphics.pose().translate(x, y, 0);
    graphics.pose().scale(scale, scale, 1.0f);
    graphics.drawString(font, text, 0, 0, color, true);
    graphics.pose().popPose();
  }

  public static void drawCentered(
      GuiGraphics graphics, Font font, String text, int centerX, int y, int color, float scale) {
    text = resolveText(text);
    int textWidth = getScaledWidth(font, text, scale);
    draw(graphics, font, text, centerX - textWidth / 2, y, color, scale);
  }

  public static void drawCentered(
      GuiGraphics graphics, Font font, Component text, int centerX, int y, int color, float scale) {
    int textWidth = getScaledWidth(font, text, scale);
    draw(graphics, font, text, centerX - textWidth / 2, y, color, scale);
  }

  public static int getScaledWidth(Font font, String text, float scale) {
    return (int) (font.width(resolveText(text)) * scale);
  }

  public static int getScaledWidth(Font font, Component text, float scale) {
    return (int) (font.width(text) * scale);
  }

  public static int getScaledHeight(Font font, float scale) {
    return (int) (font.lineHeight * scale);
  }
}
