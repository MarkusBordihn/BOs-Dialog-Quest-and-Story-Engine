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
import java.util.function.Consumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;

public class TextInput extends Widget {

  private EditBox editBox;
  private String value = "";
  private String suggestion = null;
  private int maxLength = 256;
  private int tickCount;
  private Consumer<String> onChange;

  public TextInput(int posX, int posY, int width, int height) {
    this(posX, posY, width, height, null);
  }

  public TextInput(int posX, int posY, int width, int height, Consumer<String> onChange) {
    super(posX, posY, width, height);
    this.onChange = onChange;
  }

  public String getValue() {
    return editBox != null ? editBox.getValue() : value;
  }

  public void setValue(String value) {
    this.value = value;
    if (editBox != null) {
      editBox.setValue(value);
    }
  }

  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
    if (editBox != null) {
      editBox.setSuggestion(null);
    }
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
    if (editBox != null) {
      editBox.setMaxLength(maxLength);
    }
  }

  public void setOnChange(Consumer<String> onChange) {
    this.onChange = onChange;
  }

  private void ensureEditBox() {
    if (editBox == null) {
      Font font = Minecraft.getInstance().font;
      editBox = new EditBox(font, getX() + 2, getY() + 1, width - 4, height - 2, Component.empty());
      editBox.setMaxLength(maxLength);
      editBox.setValue(value);
      editBox.setBordered(false);
      editBox.setSuggestion(null);
      if (onChange != null) {
        editBox.setResponder(onChange);
      }
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!visible) {
      return;
    }
    ensureEditBox();
    editBox.setX(getX() + 2);
    editBox.setY(getY() + (height - 8) / 2);
    editBox.setWidth(width - 4);
    editBox.setEditable(active);
    ColorPalette palette = ColorPalette.current();
    editBox.setTextColor(active ? palette.onBackground() : palette.onSurfaceLow());
    editBox.setTextColorUneditable(palette.onSurfaceLow());
    int x = getX();
    int y = getY();

    graphics.fill(x, y, x + width, y + height, palette.background());
    drawBorderBevel(graphics, x, y, width, height, palette.outline());

    graphics.enableScissor(x + 2, y, x + width - 2, y + height);
    renderInputText(graphics, palette);
    graphics.disableScissor();
  }

  private void renderInputText(GuiGraphics graphics, ColorPalette palette) {
    Font font = Minecraft.getInstance().font;
    String text = getValue();
    int textX = getX() + 2;
    int textY = getY() + (height - 8) / 2;
    if (text.isEmpty()) {
      if (suggestion != null && !suggestion.isBlank() && !editBox.isFocused()) {
        graphics.drawString(font, suggestion, textX, textY, palette.onSurfaceLow(), false);
      }
      renderCursor(graphics, palette, textX, textY);
      return;
    }

    int cursorPosition = Math.min(editBox.getCursorPosition(), text.length());
    int cursorX = editBox.getScreenX(cursorPosition);
    int visibleTextX = cursorX - font.width(text.substring(0, cursorPosition));
    graphics.drawString(
        font,
        text,
        visibleTextX,
        textY,
        active ? palette.onBackground() : palette.onSurfaceLow(),
        false);
    renderCursor(graphics, palette, cursorX, textY);
  }

  private void renderCursor(GuiGraphics graphics, ColorPalette palette, int cursorX, int textY) {
    if (!active || !editBox.isFocused() || (tickCount / 6) % 2 != 0) {
      return;
    }

    graphics.fill(cursorX, textY - 1, cursorX + 1, textY + 9, palette.onBackground());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    ensureEditBox();
    if (active && visible && isMouseOver(mouseX, mouseY)) {
      editBox.setFocused(true);
      return editBox.mouseClicked(mouseX, mouseY, button);
    } else if (editBox != null) {
      editBox.setFocused(false);
    }

    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (editBox != null && editBox.isFocused()) {
      return editBox.keyPressed(keyCode, scanCode, modifiers);
    }

    return false;
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (editBox != null && editBox.isFocused()) {
      return editBox.charTyped(codePoint, modifiers);
    }

    return false;
  }

  @Override
  public void tick() {
    tickCount++;
    if (editBox != null) {
      editBox.tick();
    }
  }
}
