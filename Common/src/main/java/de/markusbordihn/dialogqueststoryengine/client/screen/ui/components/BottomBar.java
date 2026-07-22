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
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.resources.language.I18n;

public class BottomBar extends Widget {

  private static final int SWITCH_HEIGHT = 14;

  private final Runnable onThemeChange;
  private final ToggleSwitch themeSwitch;

  public BottomBar(int posX, int posY, int width, int height, Runnable onThemeChange) {
    super(posX, posY, width, height);
    this.onThemeChange = onThemeChange;
    this.themeSwitch =
        new ToggleSwitch(
            0, 0, "Dark mode", ColorPalette.isDark(), toggled -> this.onThemeChange.run());
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }
    ColorPalette palette = ColorPalette.current();
    int x = this.getX();
    int y = this.getY();

    graphics.fill(
        x, y, x + this.width, y + this.height, (palette.outline() & 0x00FFFFFF) | 0x30000000);
    graphics.fill(x, y, x + this.width, y + 1, palette.outline());

    boolean dark = ColorPalette.isDark();
    this.themeSwitch.setToggled(dark);
    this.themeSwitch.setLabel(
        I18n.get(
            dark
                ? "gui.dialog_quest_and_story_engine.theme.dark"
                : "gui.dialog_quest_and_story_engine.theme.light"));
    int switchWidth = this.themeSwitch.getWidth();
    this.themeSwitch.setPosition(
        x + this.width - switchWidth - 4, y + (this.height - SWITCH_HEIGHT) / 2);
    this.themeSwitch.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    return this.themeSwitch.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    return this.themeSwitch.mouseReleased(mouseX, mouseY, button);
  }
}
