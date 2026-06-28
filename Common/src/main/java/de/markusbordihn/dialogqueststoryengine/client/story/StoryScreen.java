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

import com.mojang.blaze3d.systems.RenderSystem;
import de.markusbordihn.dialogqueststoryengine.theme.ScreenLayout;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import org.lwjgl.glfw.GLFW;

public abstract class StoryScreen extends Screen {

  protected static final int COLOR_TEXT = 0xFFFFFF;
  protected static final int TEXTURE_WIDTH = 512;
  protected static final int TEXTURE_HEIGHT = 256;
  protected static final int CHOICE_BUTTON_HEIGHT = 16;
  protected static final int CHOICE_BUTTON_SPACING = 4;
  protected static final int LINE_SPACING = 2;

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

  protected void renderScreenTexture(
      GuiGraphics graphics, ResourceLocation texture, ScreenLayout layout) {
    RenderSystem.setShaderTexture(0, texture);
    graphics.blit(
        texture,
        layout.leftPos(),
        layout.topPos(),
        0,
        0,
        layout.screenWidth(),
        layout.screenHeight(),
        TEXTURE_WIDTH,
        TEXTURE_HEIGHT);
  }

  protected void renderRevealedText(
      GuiGraphics graphics, ScreenLayout layout, TypewriterAnimator animator) {
    int textX = layout.leftPos() + layout.textArea().x();
    int textY = layout.topPos() + layout.textArea().y();
    int lineHeight = this.font.lineHeight + LINE_SPACING;

    graphics.enableScissor(
        layout.leftPos() + layout.textArea().x(),
        layout.topPos() + layout.textArea().y(),
        layout.leftPos() + layout.textArea().x() + layout.textArea().width(),
        layout.topPos() + layout.textArea().y() + layout.textArea().height());

    List<String> revealedLines = animator.getRevealedLines();
    for (int i = 0; i < revealedLines.size(); i++) {
      graphics.drawString(
          this.font, revealedLines.get(i), textX, textY + i * lineHeight, COLOR_TEXT, false);
    }

    String partialLine = animator.getPartialLine();
    if (!partialLine.isEmpty()) {
      graphics.drawString(
          this.font,
          partialLine,
          textX,
          textY + revealedLines.size() * lineHeight,
          COLOR_TEXT,
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
