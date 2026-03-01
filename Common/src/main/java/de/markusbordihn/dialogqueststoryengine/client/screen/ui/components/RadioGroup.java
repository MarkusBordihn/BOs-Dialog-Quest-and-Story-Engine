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
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RadioGroup extends Panel {

  private static final int ITEM_HEIGHT = 16;

  private final List<RadioButton> buttons = new ArrayList<>();
  private int selectedIndex = -1;
  private Consumer<Integer> onSelect;
  private int startY = 0;

  public RadioGroup(int posX, int posY, int width, int height, Consumer<Integer> onSelect) {
    super(posX, posY, width, height);
    this.onSelect = onSelect;
    this.clipChildren = false;
  }

  public void addButton(RadioButton button) {
    button.groupIndex = buttons.size();
    button.ownerGroup = this;
    if (button.isSelected()) {
      // If multiple buttons claim selected, last wins
      if (selectedIndex >= 0) {
        buttons.get(selectedIndex).setSelected(false);
      }
      selectedIndex = button.groupIndex;
    }
    buttons.add(button);
    addWidget(button);
  }

  public void selectIndex(int index) {
    if (index < 0 || index >= buttons.size()) return;
    if (selectedIndex >= 0 && selectedIndex < buttons.size()) {
      buttons.get(selectedIndex).setSelected(false);
    }
    selectedIndex = index;
    buttons.get(index).setSelected(true);
    if (onSelect != null) {
      onSelect.accept(index);
    }
  }

  public int getSelectedIndex() {
    return selectedIndex;
  }

  public void setOnSelect(Consumer<Integer> onSelect) {
    this.onSelect = onSelect;
  }

  public void layoutVertical(int startX, int gap) {
    int y = startY;
    for (RadioButton btn : buttons) {
      btn.setPosition(startX, y);
      y += ITEM_HEIGHT + gap;
    }
    contentHeight = y;
  }

  public void setStartY(int startY) {
    this.startY = startY;
  }

  public List<RadioButton> getButtons() {
    return buttons;
  }
}
