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

public class ToggleButton extends AbstractButton {

  private boolean toggled;
  private String labelOn;
  private String labelOff;
  private Consumer<ToggleButton> onToggle;

  public ToggleButton(
      int posX,
      int posY,
      int width,
      int height,
      String labelOn,
      String labelOff,
      boolean initialState,
      Consumer<ToggleButton> onToggle) {
    super(posX, posY, width, height);
    this.labelOn = labelOn;
    this.labelOff = labelOff;
    this.toggled = initialState;
    this.onToggle = onToggle;
  }

  public boolean isToggled() {
    return toggled;
  }

  public void setToggled(boolean toggled) {
    this.toggled = toggled;
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

    fillRoundedRect(graphics, x, y, width, height, resolveBgColor(palette));
    drawBorderRoundedBevel(graphics, x, y, width, height, palette.outline());

    Font font = Minecraft.getInstance().font;
    String label = toggled ? labelOn : labelOff;
    ScaledText.drawCentered(
        graphics,
        font,
        label,
        x + width / 2,
        y + (height - font.lineHeight) / 2 + 1,
        resolveTextColor(palette),
        ScaledText.SCALE_NORMAL);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (active && visible && button == 0 && isMouseOver(mouseX, mouseY)) {
      toggled = !toggled;
      pressed = false;
      if (onToggle != null) {
        onToggle.accept(this);
      }
      return true;
    }

    return false;
  }
}
