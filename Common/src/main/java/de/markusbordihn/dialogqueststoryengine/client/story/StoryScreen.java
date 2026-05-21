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

package de.markusbordihn.dialogqueststoryengine.client.story;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class StoryScreen extends Screen {

  protected StoryScreen(Component title) {
    super(title);
  }

  protected static void scheduleOpen(Screen screen) {
    Minecraft minecraft = Minecraft.getInstance();
    minecraft.execute(() -> minecraft.setScreen(screen));
  }

  protected void renderDimBackground(GuiGraphics graphics) {
    graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
  }

  @Override
  public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
    if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
      this.onClose();
      return true;
    }

    if (keyCode == GLFW.GLFW_KEY_SPACE) {
      this.onSpacePressed();
      return true;
    }

    return super.keyPressed(keyCode, scanCode, modifiers);
  }

  protected void onSpacePressed() {}
}
