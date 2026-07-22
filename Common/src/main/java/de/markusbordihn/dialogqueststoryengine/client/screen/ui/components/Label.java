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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class Label extends Widget {

  private Component text;
  private int color;
  private float scale;
  private Alignment alignment;

  public Label(int posX, int posY, String text) {
    this(posX, posY, TextComponent.of(text), 0, ScaledText.SCALE_NORMAL, Alignment.LEFT);
  }

  public Label(int posX, int posY, String text, int color) {
    this(posX, posY, TextComponent.of(text), color, ScaledText.SCALE_NORMAL, Alignment.LEFT);
  }

  public Label(int posX, int posY, String text, int color, float scale, Alignment alignment) {
    this(posX, posY, TextComponent.of(text), color, scale, alignment);
  }

  public Label(int posX, int posY, Component text, int color, float scale, Alignment alignment) {
    super(posX, posY, 0, 0);
    this.text = text;
    this.color = color;
    this.scale = scale;
    this.alignment = alignment;
    recalculateSize();
  }

  public Component getText() {
    return text;
  }

  public void setText(String text) {
    this.text = TextComponent.of(text);
    recalculateSize();
  }

  public void setText(Component text) {
    this.text = text;
    recalculateSize();
  }

  public void setColor(int color) {
    this.color = color;
  }

  public void setScale(float scale) {
    this.scale = scale;
    recalculateSize();
  }

  public void setAlignment(Alignment alignment) {
    this.alignment = alignment;
  }

  private void recalculateSize() {
    Font font = Minecraft.getInstance().font;
    this.width = ScaledText.getScaledWidth(font, this.text, this.scale);
    this.height = ScaledText.getScaledHeight(font, this.scale);
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible || this.text == null || this.text.getString().isEmpty()) {
      return;
    }

    Font font = Minecraft.getInstance().font;
    int effectiveColor = this.color != 0 ? this.color : ColorPalette.current().onSurface();
    int x = this.getX();
    int y = this.getY();

    switch (this.alignment) {
      case CENTER ->
          ScaledText.drawCentered(
              graphics, font, this.text, x + this.width / 2, y, effectiveColor, this.scale);
      case RIGHT -> {
        int textWidth = ScaledText.getScaledWidth(font, this.text, this.scale);
        ScaledText.draw(
            graphics, font, this.text, x + this.width - textWidth, y, effectiveColor, this.scale);
      }
      default -> ScaledText.draw(graphics, font, this.text, x, y, effectiveColor, this.scale);
    }
  }

  public enum Alignment {
    LEFT,
    CENTER,
    RIGHT
  }
}
