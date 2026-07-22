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
import net.minecraft.network.chat.Component;

public class TextButton extends AbstractButton {

  private Component label;
  private Consumer<TextButton> onPressCallback;

  public TextButton(
      int posX, int posY, int width, int height, String label, Consumer<TextButton> onPress) {
    this(posX, posY, width, height, TextComponent.of(label), onPress);
  }

  public TextButton(
      int posX, int posY, int width, int height, Component label, Consumer<TextButton> onPress) {
    super(posX, posY, width, height);
    this.label = label;
    this.onPressCallback = onPress;
  }

  public Component getLabel() {
    return this.label;
  }

  public void setLabel(String label) {
    this.label = TextComponent.of(label);
  }

  public void setLabel(Component label) {
    this.label = label;
  }

  public void setOnPress(Consumer<TextButton> onPress) {
    this.onPressCallback = onPress;
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

    fillRoundedRect(graphics, x, y, this.width, this.height, this.resolveBackgroundColor(palette));
    drawBorderRoundedBevel(graphics, x, y, this.width, this.height, palette.outline());

    Font font = Minecraft.getInstance().font;
    ScaledText.drawCentered(
        graphics,
        font,
        this.label,
        x + this.width / 2,
        y + (this.height - font.lineHeight) / 2 + 1,
        this.resolveTextColor(palette),
        ScaledText.SCALE_NORMAL);
  }

  @Override
  protected void onPress() {
    if (this.onPressCallback != null) {
      this.onPressCallback.accept(this);
    }
  }
}
