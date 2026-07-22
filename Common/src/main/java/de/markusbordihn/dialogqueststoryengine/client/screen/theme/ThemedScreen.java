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

package de.markusbordihn.dialogqueststoryengine.client.screen.theme;

import de.markusbordihn.dialogqueststoryengine.data.theme.ResolvedLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayoutContract;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSprite;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeViewport;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.Optional;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public abstract class ThemedScreen extends Screen {

  protected final Theme theme;
  protected ThemeViewport viewport;
  protected ResolvedLayout resolvedLayout;

  protected ThemedScreen(Component title, Theme theme) {
    super(title);
    this.theme = theme;
  }

  protected static int alignedX(ThemeArea area, ThemeTextAlignment alignment, int contentWidth) {
    return switch (alignment) {
      case LEFT -> area.x();
      case CENTER -> area.x() + (area.width() - contentWidth) / 2;
      case RIGHT -> area.x() + area.width() - contentWidth;
    };
  }

  protected static int centeredY(ThemeArea area, int lineHeight) {
    return area.y() + (area.height() - lineHeight) / 2;
  }

  private static ResolvedLayout resolve(Theme theme, ThemeViewport viewport) {
    return Registries.THEMES
        .get(theme.layoutId())
        .map(provider -> provider.resolve(theme, viewport))
        .orElseGet(
            () ->
                ResolvedLayout.of(
                    theme, ThemeLayoutContract.builder(theme.layoutId()).build(), viewport));
  }

  @Override
  protected void init() {
    this.viewport =
        ThemeViewport.of(
            this.theme.logicalWidth(),
            this.theme.logicalHeight(),
            this.width,
            this.height,
            this.theme.scaleLimits(),
            this.theme.anchor());
    this.resolvedLayout = resolve(this.theme, this.viewport);
    this.initThemed();
  }

  protected void initThemed() {}

  protected abstract void renderThemed(
      GuiGraphics graphics, int mouseX, int mouseY, float partialTick);

  @Override
  public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    this.renderBackdrop(graphics);
    int logicalMouseX = (int) Math.floor(this.viewport.toLogicalX(mouseX));
    int logicalMouseY = (int) Math.floor(this.viewport.toLogicalY(mouseY));

    graphics.pose().pushPose();
    graphics.pose().translate(this.viewport.originX(), this.viewport.originY(), 0.0f);
    graphics.pose().scale(this.viewport.scale(), this.viewport.scale(), 1.0f);
    this.renderThemed(graphics, logicalMouseX, logicalMouseY, partialTick);
    super.render(graphics, logicalMouseX, logicalMouseY, partialTick);
    graphics.pose().popPose();
  }

  protected void renderBackdrop(GuiGraphics graphics) {
    graphics.fillGradient(0, 0, this.width, this.height, 0xC0101010, 0xD0101010);
  }

  protected void renderViewportSprite(GuiGraphics graphics, Optional<ThemeSprite> sprite) {
    sprite.ifPresent(
        value ->
            ThemeSpriteRenderer.render(
                graphics, value, 0, 0, this.theme.logicalWidth(), this.theme.logicalHeight()));
  }

  protected void enableLogicalScissor(GuiGraphics graphics, ThemeArea area) {
    ThemeViewport.ScreenRect rect = this.viewport.toScreenRect(area);
    graphics.enableScissor(rect.x(), rect.y(), rect.x() + rect.width(), rect.y() + rect.height());
  }

  @Override
  public boolean mouseClicked(double x, double y, int button) {
    return super.mouseClicked(this.toLogicalX(x), this.toLogicalY(y), button);
  }

  @Override
  public boolean mouseReleased(double x, double y, int button) {
    return super.mouseReleased(this.toLogicalX(x), this.toLogicalY(y), button);
  }

  @Override
  public boolean mouseDragged(double x, double y, int button, double dragX, double dragY) {
    return super.mouseDragged(
        this.toLogicalX(x),
        this.toLogicalY(y),
        button,
        dragX / this.viewport.scale(),
        dragY / this.viewport.scale());
  }

  @Override
  public void mouseMoved(double x, double y) {
    super.mouseMoved(this.toLogicalX(x), this.toLogicalY(y));
  }

  @Override
  public boolean mouseScrolled(double x, double y, double delta) {
    return super.mouseScrolled(this.toLogicalX(x), this.toLogicalY(y), delta);
  }

  private double toLogicalX(double screenX) {
    return this.viewport.toLogicalX(screenX);
  }

  private double toLogicalY(double screenY) {
    return this.viewport.toLogicalY(screenY);
  }
}
