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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.Widget;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import net.minecraft.client.gui.GuiGraphics;

public class ModalPanel extends Panel {

  private static final int TITLE_HEIGHT = TitleBar.HEIGHT;
  private static final int ACTION_HEIGHT = 28;
  private static final int ACTION_BUTTON_WIDTH = 72;
  private static final int ACTION_GAP = 6;

  private final TitleBar titleBar;
  private final Panel contentPanel;
  private final List<TextButton> actionButtons = new ArrayList<>();
  private final Runnable onClose;
  private boolean hasActions;

  public ModalPanel(int posX, int posY, int width, int height, String title, Runnable onClose) {
    super(posX, posY, width, height);
    this.onClose = onClose;
    this.clipChildren = false;

    this.titleBar = new TitleBar(0, 0, width, () -> title, onClose);

    int contentHeight = height - TITLE_HEIGHT - 2;
    this.contentPanel = new Panel(0, TITLE_HEIGHT + 2, width, contentHeight);
    this.contentPanel.setParent(this);
  }

  public void addContentWidget(Widget widget) {
    this.contentPanel.addWidget(widget);
  }

  public void addAction(String label, Consumer<TextButton> onPress) {
    if (!this.hasActions) {
      this.hasActions = true;
      this.contentPanel.setHeight(this.contentPanel.getHeight() - ACTION_HEIGHT - 2);
    }
    TextButton actionButton =
        new TextButton(0, 0, ACTION_BUTTON_WIDTH, ACTION_HEIGHT - 6, label, onPress);
    this.actionButtons.add(actionButton);
    this.layoutActionButtons();
  }

  public void centerOn(int screenWidth, int screenHeight) {
    this.posX = (screenWidth - this.width) / 2;
    this.posY = (screenHeight - this.height) / 2;
  }

  private void layoutActionButtons() {
    int totalButtonWidth =
        this.actionButtons.size() * ACTION_BUTTON_WIDTH
            + (this.actionButtons.size() - 1) * ACTION_GAP;
    int startX = (this.width - totalButtonWidth) / 2;
    int buttonY = TITLE_HEIGHT + 2 + this.contentPanel.getHeight() + 4;
    for (int i = 0; i < this.actionButtons.size(); i++) {
      this.actionButtons
          .get(i)
          .setPosition(startX + i * (ACTION_BUTTON_WIDTH + ACTION_GAP), buttonY);
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

    graphics.fill(x + 3, y + 3, x + this.width + 3, y + this.height + 3, 0x60000000);
    graphics.fill(x, y, x + this.width, y + this.height, palette.surface());
    drawBorderBevel(graphics, x, y, this.width, this.height, palette.outline());

    this.titleBar.setPosition(x, y);
    this.titleBar.setWidth(this.width);
    this.titleBar.render(graphics, mouseX, mouseY, partialTick);

    this.contentPanel.render(graphics, mouseX, mouseY, partialTick);

    for (TextButton actionButton : this.actionButtons) {
      actionButton.render(graphics, mouseX, mouseY, partialTick);
    }
  }

  @Override
  public int getContentX() {
    return this.getX() + this.padding;
  }

  @Override
  public int getContentY() {
    return this.getY() + TITLE_HEIGHT + 2 + this.padding;
  }

  @Override
  public int getInnerHeight() {
    return this.contentPanel.getHeight() - this.padding * 2;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (this.titleBar.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    for (TextButton actionButton : this.actionButtons) {
      if (actionButton.isVisible()
          && actionButton.isActive()
          && actionButton.isMouseOver(mouseX, mouseY)
          && actionButton.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }
    }

    return this.contentPanel.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    for (TextButton actionButton : this.actionButtons) {
      if (actionButton.mouseReleased(mouseX, mouseY, button)) {
        return true;
      }
    }
    return this.contentPanel.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    return this.contentPanel.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == 256 && this.onClose != null) {
      this.onClose.run();
      return true;
    }

    return this.contentPanel.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    return this.contentPanel.charTyped(codePoint, modifiers);
  }
}
