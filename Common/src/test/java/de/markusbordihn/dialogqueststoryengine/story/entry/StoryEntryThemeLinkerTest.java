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

package de.markusbordihn.dialogqueststoryengine.story.entry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.client.screen.theme.BuiltinLayoutScreens;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntryType;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryPage;
import de.markusbordihn.dialogqueststoryengine.data.theme.BuiltinLayouts;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeAnchor;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeScaleLimits;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.theme.BuiltinThemeProviders;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoryEntryThemeLinkerTest {

  private static final ResourceLocation STORY_ID = new ResourceLocation("test", "story_a");
  private static final ResourceLocation THEME_ID = new ResourceLocation("test", "theme_a");
  private static final ResourceLocation MISSING_THEME_ID =
      new ResourceLocation("test", "nonexistent_theme");

  @BeforeAll
  static void registerLayouts() {
    if (!Registries.THEMES.contains(BuiltinLayouts.HOLOPAD)) {
      BuiltinThemeProviders.register();
    }
    BuiltinLayoutScreens.register();
  }

  private static StoryEntry buildStoryEntry(ResourceLocation id, ResourceLocation themeId) {
    return new StoryEntry(
        UUID.randomUUID(),
        id,
        1,
        StoryEntryType.HOLOPAD,
        "story.test.title",
        themeId,
        List.of(new StoryPage("story.test.page_1")));
  }

  private static Theme buildTheme(ResourceLocation id) {
    return new Theme(
        UUID.randomUUID(),
        id,
        1,
        BuiltinLayouts.HOLOPAD,
        320,
        240,
        ThemeScaleLimits.DEFAULT,
        ThemeAnchor.CENTER,
        Map.of(
            "text", new ThemeArea(24, 44, 272, 78),
            "choices", new ThemeArea(60, 128, 200, 62)),
        Map.of(),
        Map.of(),
        Map.of());
  }

  private static Theme buildDialogTheme(ResourceLocation id) {
    return new Theme(
        UUID.randomUUID(),
        id,
        1,
        BuiltinLayouts.DIALOG,
        427,
        240,
        ThemeScaleLimits.DEFAULT,
        ThemeAnchor.CENTER,
        Map.of(
            "text", new ThemeArea(76, 156, 343, 30),
            "choices", new ThemeArea(16, 190, 395, 40)),
        Map.of(),
        Map.of(),
        Map.of());
  }

  @BeforeEach
  void clearState() {
    StoryEntryClientRegistry.clear();
    ThemeClientRegistry.clear();
    ContentIssueTracker.clearAll();
  }

  @Test
  void noIssueWhenThemeIsLoaded() {
    ThemeClientRegistry.put(buildTheme(THEME_ID));
    StoryEntryClientRegistry.put(buildStoryEntry(STORY_ID, THEME_ID));

    StoryEntryThemeLinker.validate();

    assertTrue(ContentIssueTracker.issues().isEmpty());
  }

  @Test
  void missingThemeReferenceIssueRecorded() {
    StoryEntryClientRegistry.put(buildStoryEntry(STORY_ID, MISSING_THEME_ID));

    StoryEntryThemeLinker.validate();

    assertEquals(1, ContentIssueTracker.issues().size());
    assertEquals(IssueCode.MISSING_THEME_REFERENCE, ContentIssueTracker.issues().get(0).code());
    assertEquals(STORY_ID, ContentIssueTracker.issues().get(0).id());
  }

  @Test
  void revalidationClearsPreviousMissingThemeIssues() {
    StoryEntryClientRegistry.put(buildStoryEntry(STORY_ID, MISSING_THEME_ID));
    StoryEntryThemeLinker.validate();
    assertEquals(1, ContentIssueTracker.issues().size());

    ThemeClientRegistry.put(buildTheme(MISSING_THEME_ID));
    StoryEntryThemeLinker.validate();

    assertTrue(ContentIssueTracker.issues().isEmpty());
  }

  @Test
  void issueDetailsContainThemeId() {
    StoryEntryClientRegistry.put(buildStoryEntry(STORY_ID, MISSING_THEME_ID));

    StoryEntryThemeLinker.validate();

    assertEquals(
        MISSING_THEME_ID.toString(), ContentIssueTracker.issues().get(0).details().get("theme_id"));
  }

  @Test
  void storyReferencingNonStoryLayoutIsFlagged() {
    ThemeClientRegistry.put(buildDialogTheme(THEME_ID));
    StoryEntryClientRegistry.put(buildStoryEntry(STORY_ID, THEME_ID));

    StoryEntryThemeLinker.validate();

    assertEquals(1, ContentIssueTracker.issues().size());
    assertEquals(IssueCode.THEME_LAYOUT_MISMATCH, ContentIssueTracker.issues().get(0).code());
  }
}
