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

import de.markusbordihn.dialogqueststoryengine.client.screen.theme.ThemedScreen;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public abstract class StoryScreen extends ThemedScreen {

  protected static final int CHOICE_BUTTON_HEIGHT = 16;
  protected static final int CHOICE_BUTTON_SPACING = 4;
  protected static final int LINE_SPACING = 2;

  protected StoryScreen(Component title, Theme theme) {
    super(title, theme);
  }

  protected static void scheduleOpen(Screen screen) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft == null) {
      return;
    }

    minecraft.execute(() -> minecraft.setScreen(screen));
  }

  protected void renderRevealedText(
      GuiGraphics graphics, ThemeArea textArea, TypewriterAnimator animator, int textColor) {
    int lineHeight = this.font.lineHeight + LINE_SPACING;

    enableLogicalScissor(graphics, textArea);

    List<String> revealedLines = animator.getRevealedLines();
    for (int i = 0; i < revealedLines.size(); i++) {
      graphics.drawString(
          this.font,
          revealedLines.get(i),
          textArea.x(),
          textArea.y() + i * lineHeight,
          textColor,
          false);
    }

    String partialLine = animator.getPartialLine();
    if (!partialLine.isEmpty()) {
      graphics.drawString(
          this.font,
          partialLine,
          textArea.x(),
          textArea.y() + revealedLines.size() * lineHeight,
          textColor,
          false);
    }

    graphics.disableScissor();
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
