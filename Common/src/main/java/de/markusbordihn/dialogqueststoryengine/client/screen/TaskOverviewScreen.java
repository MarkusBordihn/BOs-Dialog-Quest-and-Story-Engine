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

package de.markusbordihn.dialogqueststoryengine.client.screen;

import de.markusbordihn.dialogqueststoryengine.client.screen.ui.BaseScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.Label;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public class TaskOverviewScreen extends BaseScreen {

  public TaskOverviewScreen(List<BreadcrumbBar.Segment> ancestors) {
    this.setBreadcrumb(ancestors, "Tasks");
  }

  public static void open(List<BreadcrumbBar.Segment> ancestors) {
    Minecraft.getInstance()
        .execute(
            () -> {
              TaskOverviewScreen screen = new TaskOverviewScreen(ancestors);
              screen.openScreen();
            });
  }

  @Override
  protected Component getTitle() {
    return TextComponent.ofKey("screen.dialog_quest_and_story_engine.task_overview");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    this.setSizeCentered(400, 260);
    this.refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    this.addWidget(
        new Label(
            this.getInnerWidth() / 2,
            16,
            "coming_soon",
            0,
            ScaledText.SCALE_NORMAL,
            Label.Alignment.CENTER));
  }
}
