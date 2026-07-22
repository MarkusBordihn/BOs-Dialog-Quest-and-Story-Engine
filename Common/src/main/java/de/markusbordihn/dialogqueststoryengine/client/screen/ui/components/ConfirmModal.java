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
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class ConfirmModal extends Panel {

  private static final int WIDTH = 300;
  private static final int HEIGHT = 116;
  private static final int TITLE_HEIGHT = 16;
  private static final int BUTTON_WIDTH = 92;
  private static final int BUTTON_HEIGHT = 20;

  private final Component title;
  private final Component message;
  private final String detail;
  private final Runnable onConfirm;
  private final TextButton confirmButton;
  private final TextButton cancelButton;

  public ConfirmModal(
      int screenWidth,
      int screenHeight,
      String titleKey,
      String messageKey,
      String detail,
      String confirmKey,
      Runnable onConfirm,
      Runnable onCancel) {
    super((screenWidth - WIDTH) / 2, (screenHeight - HEIGHT) / 2, WIDTH, HEIGHT);
    this.clipChildren = false;
    this.title = TextComponent.of(titleKey);
    this.message = TextComponent.of(messageKey);
    this.detail = detail != null ? detail : "";
    this.onConfirm = onConfirm;

    int buttonY = this.posY + HEIGHT - BUTTON_HEIGHT - 8;
    this.confirmButton =
        new TextButton(
            this.posX + WIDTH / 2 - BUTTON_WIDTH - 5,
            buttonY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            confirmKey,
            button -> onConfirm.run());
    this.cancelButton =
        new TextButton(
            this.posX + WIDTH / 2 + 5,
            buttonY,
            BUTTON_WIDTH,
            BUTTON_HEIGHT,
            "button.cancel",
            button -> onCancel.run());
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

    graphics.fill(x + 3, y + 3, x + WIDTH + 3, y + HEIGHT + 3, 0x80000000);
    graphics.fill(x, y, x + WIDTH, y + HEIGHT, palette.surface());
    graphics.fill(x, y, x + WIDTH, y + TITLE_HEIGHT, palette.surfaceContainerHigh());
    drawBorderBevel(graphics, x, y, WIDTH, HEIGHT, palette.outline());

    ScaledText.draw(
        graphics, font, this.title, x + 6, y + 4, palette.onSurface(), ScaledText.SCALE_BODY);
    ScaledText.drawCentered(
        graphics,
        font,
        this.message,
        x + WIDTH / 2,
        y + TITLE_HEIGHT + 12,
        palette.onSurface(),
        ScaledText.SCALE_BODY);
    if (!this.detail.isEmpty()) {
      ScaledText.drawCentered(
          graphics,
          font,
          this.detail,
          x + WIDTH / 2,
          y + TITLE_HEIGHT + 28,
          palette.onSurfaceLow(),
          ScaledText.SCALE_SMALL);
    }

    this.confirmButton.render(graphics, mouseX, mouseY, partialTick);
    this.cancelButton.render(graphics, mouseX, mouseY, partialTick);
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    this.confirmButton.mouseClicked(mouseX, mouseY, button);
    this.cancelButton.mouseClicked(mouseX, mouseY, button);
    return true;
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    this.confirmButton.mouseReleased(mouseX, mouseY, button);
    this.cancelButton.mouseReleased(mouseX, mouseY, button);
    return true;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
      this.onConfirm.run();
      return true;
    }

    return false;
  }
}
