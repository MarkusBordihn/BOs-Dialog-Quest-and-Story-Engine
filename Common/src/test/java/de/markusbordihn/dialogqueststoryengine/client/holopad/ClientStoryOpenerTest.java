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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import de.markusbordihn.dialogqueststoryengine.client.screen.theme.BuiltinLayoutScreens;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntryType;
import de.markusbordihn.dialogqueststoryengine.data.theme.BuiltinLayouts;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntryClientRegistry;
import de.markusbordihn.dialogqueststoryengine.theme.BuiltinThemeProviders;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ClientStoryOpenerTest {

  private static final ResourceLocation UNKNOWN_STORY = new ResourceLocation("test", "missing");
  private static final ResourceLocation KNOWN_STORY = new ResourceLocation("test", "known");
  private static final ResourceLocation KNOWN_THEME = new ResourceLocation("test", "theme");

  @BeforeAll
  static void registerLayouts() {
    if (!Registries.THEMES.contains(BuiltinLayouts.HOLOPAD)) {
      BuiltinThemeProviders.register();
    }
    BuiltinLayoutScreens.register();
  }

  @BeforeEach
  void clearRegistries() {
    StoryEntryClientRegistry.clear();
    ThemeClientRegistry.clear();
  }

  @Test
  void openWithUnregisteredStoryDoesNotThrow() {
    assertDoesNotThrow(() -> ClientStoryOpener.open(UNKNOWN_STORY));
  }

  @Test
  void openWithRegisteredStoryButNoThemeDoesNotThrow() {
    StoryEntry entry =
        new StoryEntry(
            UUID.randomUUID(),
            KNOWN_STORY,
            1,
            StoryEntryType.HOLOPAD,
            "title.key",
            KNOWN_THEME,
            List.of());
    StoryEntryClientRegistry.put(entry);

    assertDoesNotThrow(() -> ClientStoryOpener.open(KNOWN_STORY));
  }
}
