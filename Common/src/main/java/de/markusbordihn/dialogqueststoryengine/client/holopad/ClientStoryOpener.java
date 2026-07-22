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

package de.markusbordihn.dialogqueststoryengine.client.holopad;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.screen.theme.LayoutContentKind;
import de.markusbordihn.dialogqueststoryengine.client.screen.theme.LayoutScreenRegistry;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.network.message.session.OpenStorySessionPacket;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntryClientRegistry;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ClientStoryOpener {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ClientStoryOpener() {}

  public static void open(ResourceLocation storyId) {
    open(storyId, null);
  }

  public static void open(ResourceLocation storyId, ResourceLocation themeOverrideId) {
    StoryEntry entry =
        StoryEntryClientRegistry.get(storyId)
            .orElseGet(
                () -> {
                  log.warn("{} Story entry not found: {}", Constants.LOG_PREFIX, storyId);
                  return null;
                });

    if (entry == null) {
      return;
    }

    ResourceLocation requestedThemeId = themeOverrideId != null ? themeOverrideId : entry.themeId();
    Theme theme = LayoutScreenRegistry.resolveTheme(requestedThemeId, LayoutContentKind.STORY);
    HolopadScreen.open(entry, theme);
  }

  public static void openFromSession(OpenStorySessionPacket packet) {
    StoryEntry entry =
        StoryEntryClientRegistry.get(packet.displayStoryId())
            .orElseGet(
                () -> {
                  log.warn(
                      "{} Story entry not found for interactive session: {}",
                      Constants.LOG_PREFIX,
                      packet.displayStoryId());
                  return null;
                });

    if (entry == null) {
      return;
    }

    Theme theme = LayoutScreenRegistry.resolveTheme(entry.themeId(), LayoutContentKind.STORY);

    HolopadScreen.openInteractive(
        entry,
        theme,
        new HolopadScreen.SessionData(
            packet.sessionId(),
            packet.allowedChoiceIds(),
            packet.choiceLabels(),
            packet.revision()));
  }

  public static void closeSession(UUID sessionId) {
    Screen currentScreen = Minecraft.getInstance().screen;
    if (currentScreen instanceof HolopadScreen holopadScreen) {
      UUID openSessionId = holopadScreen.sessionId();
      if (sessionId.equals(openSessionId)) {
        Minecraft.getInstance().setScreen(null);
      }
    }
  }

  public static void handleRejection(UUID sessionId) {
    Screen currentScreen = Minecraft.getInstance().screen;
    if (currentScreen instanceof HolopadScreen holopadScreen
        && sessionId.equals(holopadScreen.sessionId())) {
      holopadScreen.onServerRejection();
    }
  }
}
