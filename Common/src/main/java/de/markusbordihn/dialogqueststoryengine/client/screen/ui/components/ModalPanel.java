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
import net.minecraft.client.gui.GuiGraphics;

public class ModalPanel extends Panel {

  private static final int TITLE_H = TitleBar.HEIGHT;
  private static final int ACTION_H = 28;
  private static final int ACTION_BTN_W = 72;
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

    titleBar = new TitleBar(0, 0, width, () -> title, onClose);

    int contentH = height - TITLE_H - 2;
    contentPanel = new Panel(0, TITLE_H + 2, width, contentH);
    contentPanel.setParent(this);
  }

  public void addContentWidget(Widget widget) {
    contentPanel.addWidget(widget);
  }

  public void addAction(String label, java.util.function.Consumer<TextButton> onPress) {
    if (!hasActions) {
      hasActions = true;
      // Shrink content panel to make room for action row
      contentPanel.setHeight(contentPanel.getHeight() - ACTION_H - 2);
    }
    TextButton btn = new TextButton(0, 0, ACTION_BTN_W, ACTION_H - 6, label, onPress);
    actionButtons.add(btn);
    layoutActionButtons();
  }

  public void centerOn(int screenWidth, int screenHeight) {
    posX = (screenWidth - width) / 2;
    posY = (screenHeight - height) / 2;
  }

  private void layoutActionButtons() {
    int totalButtonWidth =
        actionButtons.size() * ACTION_BTN_W + (actionButtons.size() - 1) * ACTION_GAP;
    int startX = (width - totalButtonWidth) / 2;
    int buttonY = TITLE_H + 2 + contentPanel.getHeight() + 4;
    for (int i = 0; i < actionButtons.size(); i++) {
      actionButtons.get(i).setPosition(startX + i * (ACTION_BTN_W + ACTION_GAP), buttonY);
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }
    ColorPalette palette = ColorPalette.current();
    int x = getX();
    int y = getY();

    graphics.fill(x + 3, y + 3, x + width + 3, y + height + 3, 0x60000000);
    graphics.fill(x, y, x + width, y + height, palette.surface());
    drawBorderBevel(graphics, x, y, width, height, palette.outline());

    titleBar.setPosition(x, y);
    titleBar.setWidth(width);
    titleBar.render(graphics, mouseX, mouseY, partialTick);

    contentPanel.render(graphics, mouseX, mouseY, partialTick);

    for (TextButton btn : actionButtons) {
      btn.render(graphics, mouseX, mouseY, partialTick);
    }
  }

  @Override
  public int getContentX() {
    return getX() + padding;
  }

  @Override
  public int getContentY() {
    return getY() + TITLE_H + 2 + padding;
  }

  @Override
  public int getInnerHeight() {
    return contentPanel.getHeight() - padding * 2;
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    if (titleBar.mouseClicked(mouseX, mouseY, button)) {
      return true;
    }
    for (TextButton btn : actionButtons) {
      if (btn.isVisible()
          && btn.isActive()
          && btn.isMouseOver(mouseX, mouseY)
          && btn.mouseClicked(mouseX, mouseY, button)) {
        return true;
      }
    }

    return contentPanel.mouseClicked(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseReleased(double mouseX, double mouseY, int button) {
    for (TextButton btn : actionButtons) {
      if (btn.mouseReleased(mouseX, mouseY, button)) {
        return true;
      }
    }
    return contentPanel.mouseReleased(mouseX, mouseY, button);
  }

  @Override
  public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
    return contentPanel.mouseScrolled(mouseX, mouseY, delta);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == 256 && onClose != null) {
      onClose.run();
      return true;
    }

    return contentPanel.keyPressed(keyCode, scanCode, modifiers);
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    return contentPanel.charTyped(codePoint, modifiers);
  }
}
