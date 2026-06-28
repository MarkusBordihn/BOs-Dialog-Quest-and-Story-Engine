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

package de.markusbordihn.dialogqueststoryengine.client.dialog;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.screen.dialog.DialogScreen;
import de.markusbordihn.dialogqueststoryengine.client.screen.dialog.DialogSessionData;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacketType;
import de.markusbordihn.dialogqueststoryengine.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientDialogOpener {

  public static final ResourceLocation DEFAULT_DIALOG_THEME_ID =
      new ResourceLocation(Constants.MOD_ID, "default_dialog");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ClientDialogOpener() {}

  public static void handle(DialogSessionPacket packet) {
    if (packet.type() == DialogSessionPacketType.OPEN_DIALOG) {
      openFromPacket(packet);
    } else {
      navigateCurrentScreen(packet);
    }
  }

  public static void closeSession(UUID sessionId) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen instanceof DialogScreen dialogScreen
        && sessionId.equals(dialogScreen.sessionId())) {
      minecraft.execute(() -> minecraft.setScreen(null));
    }
  }

  public static void handleRejection(UUID sessionId) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen instanceof DialogScreen dialogScreen
        && sessionId.equals(dialogScreen.sessionId())) {
      dialogScreen.onServerRejection();
    }
  }

  private static void openFromPacket(DialogSessionPacket packet) {
    Theme theme =
        ThemeClientRegistry.getOrDefault(DEFAULT_DIALOG_THEME_ID)
            .orElseGet(
                () -> {
                  log.warn(
                      "{} No theme found for dialog {}, not even default_dialog",
                      Constants.LOG_PREFIX,
                      packet.dialogId());
                  return null;
                });

    if (theme == null) {
      return;
    }

    DialogScreen.open(
        new DialogSessionData(
            packet.sessionId(),
            packet.dialogId(),
            packet.nodeId(),
            packet.speakerKey(),
            packet.textKey(),
            packet.allowedChoiceIds(),
            packet.choiceLabels(),
            packet.revision()),
        theme);
  }

  private static void navigateCurrentScreen(DialogSessionPacket packet) {
    Minecraft minecraft = Minecraft.getInstance();
    if (minecraft.screen instanceof DialogScreen dialogScreen
        && packet.sessionId().equals(dialogScreen.sessionId())) {
      minecraft.execute(
          () ->
              dialogScreen.navigateToNode(
                  packet.nodeId(),
                  packet.speakerKey(),
                  packet.textKey(),
                  packet.allowedChoiceIds(),
                  packet.choiceLabels(),
                  packet.revision()));
    }
  }
}
