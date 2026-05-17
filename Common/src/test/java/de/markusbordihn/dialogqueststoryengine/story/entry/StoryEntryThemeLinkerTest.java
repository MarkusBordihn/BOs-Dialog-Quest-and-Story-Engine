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

import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.theme.TextArea;
import de.markusbordihn.dialogqueststoryengine.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeClientRegistry;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeLayout;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class StoryEntryThemeLinkerTest {

  private static final ResourceLocation STORY_ID = new ResourceLocation("test", "story_a");
  private static final ResourceLocation THEME_ID = new ResourceLocation("test", "theme_a");
  private static final ResourceLocation MISSING_THEME_ID =
      new ResourceLocation("test", "nonexistent_theme");

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
        ThemeLayout.HOLOPAD,
        new ResourceLocation("test", "textures/frame.png"),
        new ResourceLocation("test", "textures/background.png"),
        true,
        true,
        new TextArea(0, 0, 100, 80));
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
}
