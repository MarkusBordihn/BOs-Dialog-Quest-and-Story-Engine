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

package de.markusbordihn.dialogqueststoryengine.network.message;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.screen.ActionOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.DialogOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.InteractionOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.MainScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.QuestOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.ScreenType;
import de.markusbordihn.dialogqueststoryengine.client.screen.StoryOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.TaskOverviewScreen;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record OpenOverviewScreenMessage(ScreenType screenType) implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":open_overview_screen");

  public static OpenOverviewScreenMessage create(FriendlyByteBuf buffer) {
    return new OpenOverviewScreenMessage(buffer.readEnum(ScreenType.class));
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeEnum(this.screenType);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleClient() {
    if (screenType == null) {
      MainScreen.open();
      return;
    }
    switch (screenType) {
      case INTERACTIONS -> InteractionOverviewScreen.open();
      case DIALOGS -> DialogOverviewScreen.open(List.of());
      case QUESTS -> QuestOverviewScreen.open(List.of());
      case TASKS -> TaskOverviewScreen.open(List.of());
      case STORIES -> StoryOverviewScreen.open(List.of());
      case ACTIONS -> ActionOverviewScreen.open(List.of());
      default -> MainScreen.open();
    }
  }
}
