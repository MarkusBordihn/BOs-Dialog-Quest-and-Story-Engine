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
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.color.ColorPalette;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.BreadcrumbBar;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ColumnListPanel;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.ScaledText;
import de.markusbordihn.dialogqueststoryengine.client.screen.ui.components.TextComponent;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogClientRegistry;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import java.util.List;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public class DialogOverviewScreen extends BaseScreen {

  public DialogOverviewScreen(List<BreadcrumbBar.Segment> ancestors) {
    setBreadcrumb(ancestors, "Dialogs");
  }

  public static void open(List<BreadcrumbBar.Segment> ancestors) {
    Minecraft.getInstance()
        .execute(
            () -> {
              DialogOverviewScreen screen = new DialogOverviewScreen(ancestors);
              screen.openScreen();
            });
  }

  @Override
  protected Component getTitle() {
    return TextComponent.ofKey("screen.dialog_quest_and_story_engine.dialog_overview");
  }

  @Override
  public void onScreenInit(int screenWidth, int screenHeight) {
    setSizeCentered(400, 260);
    refreshWidgets();
  }

  @Override
  protected void addWidgets() {
    ColumnListPanel<DialogDefinition> table =
        new ColumnListPanel<>(0, 0, getInnerWidth(), getInnerHeight());
    table.addColumn("column.id", 0.5f);
    table.addColumn("column.start_node", 0.3f);
    table.addColumn("column.nodes", 0.2f);
    table.setEntryHeight(20);
    table.setEntryRenderer(this::renderEntry);
    table.setItems(
        DialogClientRegistry.ids().stream()
            .map(id -> DialogClientRegistry.get(id).orElseThrow())
            .toList());
    addWidget(table);
  }

  private void renderEntry(
      GuiGraphics graphics,
      Font font,
      DialogDefinition definition,
      int index,
      int x,
      int y,
      int width,
      int height,
      ColorPalette palette,
      int[] columnOffsets) {
    float scale = ScaledText.SCALE_SMALL;
    ScaledText.draw(
        graphics,
        font,
        definition.id().toString(),
        x + columnOffsets[0],
        y + 3,
        palette.onSurface(),
        scale);
    ScaledText.draw(
        graphics,
        font,
        definition.startNode(),
        x + columnOffsets[1],
        y + 3,
        palette.onSurfaceLow(),
        scale);
    ScaledText.draw(
        graphics,
        font,
        String.valueOf(definition.nodes().size()),
        x + columnOffsets[2],
        y + 3,
        palette.onSurfaceLow(),
        scale);
  }
}
