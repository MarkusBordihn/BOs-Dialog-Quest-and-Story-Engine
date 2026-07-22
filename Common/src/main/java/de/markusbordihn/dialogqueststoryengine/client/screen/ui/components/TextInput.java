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
    return this.editBox != null ? this.editBox.getValue() : this.value;
  }

  public void setValue(String value) {
    this.value = value;
    if (this.editBox != null) {
      this.editBox.setValue(value);
    }
  }

  public void setSuggestion(String suggestion) {
    this.suggestion = suggestion;
    if (this.editBox != null) {
      this.editBox.setSuggestion(null);
    }
  }

  public void setMaxLength(int maxLength) {
    this.maxLength = maxLength;
    if (this.editBox != null) {
      this.editBox.setMaxLength(maxLength);
    }
  }

  public void setOnChange(Consumer<String> onChange) {
    this.onChange = onChange;
  }

  public void focus() {
    this.ensureEditBox();
    this.editBox.setFocused(true);
  }

  private void ensureEditBox() {
    if (this.editBox == null) {
      Font font = Minecraft.getInstance().font;
      this.editBox =
          new EditBox(
              font,
              this.getX() + 2,
              this.getY() + 1,
              this.width - 4,
              this.height - 2,
              Component.empty());
      this.editBox.setMaxLength(this.maxLength);
      this.editBox.setValue(this.value);
      this.editBox.setBordered(false);
      this.editBox.setSuggestion(null);
      if (this.onChange != null) {
        this.editBox.setResponder(this.onChange);
      }
    }
  }

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    if (!this.visible) {
      return;
    }

    this.ensureEditBox();
    this.editBox.setX(this.getX() + 2);
    this.editBox.setY(this.getY() + (this.height - 8) / 2);
    this.editBox.setWidth(this.width - 4);
    this.editBox.setEditable(this.active);
    ColorPalette palette = ColorPalette.current();
    this.editBox.setTextColor(this.active ? palette.onBackground() : palette.onSurfaceLow());
    this.editBox.setTextColorUneditable(palette.onSurfaceLow());
    int x = this.getX();
    int y = this.getY();

    graphics.fill(x, y, x + this.width, y + this.height, palette.background());
    drawBorderBevel(graphics, x, y, this.width, this.height, palette.outline());

    graphics.enableScissor(x + 2, y, x + this.width - 2, y + this.height);
    this.renderInputText(graphics, palette);
    graphics.disableScissor();
  }

  private void renderInputText(GuiGraphics graphics, ColorPalette palette) {
    Font font = Minecraft.getInstance().font;
    String text = this.getValue();
    int textX = this.getX() + 2;
    int textY = this.getY() + (this.height - 8) / 2;
    if (text.isEmpty()) {
      if (this.suggestion != null && !this.suggestion.isBlank() && !this.editBox.isFocused()) {
        graphics.drawString(font, this.suggestion, textX, textY, palette.onSurfaceLow(), false);
      }
      this.renderCursor(graphics, palette, textX, textY);
      return;
    }

    int cursorPosition = Math.min(this.editBox.getCursorPosition(), text.length());
    int cursorX = this.editBox.getScreenX(cursorPosition);
    int visibleTextX = cursorX - font.width(text.substring(0, cursorPosition));
    graphics.drawString(
        font,
        text,
        visibleTextX,
        textY,
        this.active ? palette.onBackground() : palette.onSurfaceLow(),
        false);
    this.renderCursor(graphics, palette, cursorX, textY);
  }

  private void renderCursor(GuiGraphics graphics, ColorPalette palette, int cursorX, int textY) {
    if (!this.active || !this.editBox.isFocused() || (this.tickCount / 6) % 2 != 0) {
      return;
    }

    graphics.fill(cursorX, textY - 1, cursorX + 1, textY + 9, palette.onBackground());
  }

  @Override
  public boolean mouseClicked(double mouseX, double mouseY, int button) {
    this.ensureEditBox();
    if (this.active && this.visible && this.isMouseOver(mouseX, mouseY)) {
      this.editBox.setFocused(true);
      return this.editBox.mouseClicked(mouseX, mouseY, button);
    } else if (this.editBox != null) {
      this.editBox.setFocused(false);
    }

    return false;
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (this.editBox != null && this.editBox.isFocused()) {
      return this.editBox.keyPressed(keyCode, scanCode, modifiers);
    }

    return false;
  }

  @Override
  public boolean charTyped(char codePoint, int modifiers) {
    if (this.editBox != null && this.editBox.isFocused()) {
      return this.editBox.charTyped(codePoint, modifiers);
    }

    return false;
  }

  @Override
  public void tick() {
    this.tickCount++;
    if (this.editBox != null) {
      this.editBox.tick();
    }
  }
}
