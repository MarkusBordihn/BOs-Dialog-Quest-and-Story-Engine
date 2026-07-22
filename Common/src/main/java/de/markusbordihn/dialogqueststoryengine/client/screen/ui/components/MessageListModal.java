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

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Panel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class MessageListModal extends Panel {

  private static final int WIDTH = 330;
  private static final int TITLE_HEIGHT = 16;
  private static final int LINE_HEIGHT = 11;
  private static final int MAX_LINES = 8;
  private static final int BUTTON_WIDTH = 92;
  private static final int BUTTON_HEIGHT = 20;

  private final Component title;
  private final List<String> lines;
  private final int boxHeight;
  private final TextButton closeButton;

  public MessageListModal(
      int screenWidth, int screenHeight, String titleKey, List<String> lines, Runnable onClose) {
    super(0, 0, WIDTH, 0);
    this.clipChildren = false;
    this.title = TextComponent.of(titleKey);
    this.lines = lines != null ? lines : List.of();
    int shown = Math.min(this.lines.size(), MAX_LINES);
    this.boxHeight = TITLE_HEIGHT + 8 + Math.max(1, shown) * LINE_HEIGHT + 8 + BUTTON_HEIGHT + 8;
    this.height = this.boxHeight;
    this.posX = (screenWidth - WIDTH) / 2;
    this.posY = (screenHeight - this.boxHeight) / 2;
    this.closeButton =
        new TextButton(
            this.posX + (WIDTH - BUTTON_WIDTH) / 2,
            this.posY + this.boxHeight - BUTTON_HEIGHT - 8,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            "button.close",
            button -> onClose.run());
  }

  @Override
  public int getX() {
    return this.posX;
  }

  @Override
  public int getY() {
    return this.posY;
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }

    ColorPalette palette = ColorPalette.current();
    int x = this.posX;
    int y = this.posY;
    Font font = Minecraft.getInstance().font;

    graphics.fill(x + 3, y + 3, x + WIDTH + 3, y + this.boxHeight + 3, 0x80000000);
    graphics.fill(x, y, x + WIDTH, y + this.boxHeight, palette.surface());
    graphics.fill(x, y, x + WIDTH, y + TITLE_HEIGHT, palette.surfaceContainerHigh());
    drawBorderBevel(graphics, x, y, WIDTH, this.boxHeight, palette.outline());

    ScaledText.draw(
        graphics, font, this.title, x + 6, y + 4, palette.onSurface(), ScaledText.SCALE_BODY);

    int textY = y + TITLE_HEIGHT + 6;
    graphics.enableScissor(x + 6, textY, x + WIDTH - 6, textY + MAX_LINES * LINE_HEIGHT);
    int shown = Math.min(this.lines.size(), MAX_LINES);
    for (int i = 0; i < shown; i++) {
      graphics.drawString(
          font, this.lines.get(i), x + 8, textY + i * LINE_HEIGHT, palette.onSurface(), false);
    }
    graphics.disableScissor();

    this.closeButton.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    this.closeButton.mouseClicked(mouseX, mouseY, button);
    return true;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    this.closeButton.mouseReleased(mouseX, mouseY, button);
    return true;
  }
}
