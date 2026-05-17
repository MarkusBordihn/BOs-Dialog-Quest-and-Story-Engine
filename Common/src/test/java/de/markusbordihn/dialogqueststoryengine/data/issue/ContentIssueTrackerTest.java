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

package de.markusbordihn.dialogqueststoryengine.data.issue;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ContentIssueTrackerTest {

  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "entry_a");

  @BeforeEach
  void clearTracker() {
    ContentIssueTracker.clearAll();
  }

  @Test
  void recordedIssueIsRetrievable() {
    ContentIssue issue =
        ContentIssue.of(
            IssueCode.MISSING_FIELD, ContentType.STORY_ENTRY, TEST_ID, "test.json", "title_key");

    ContentIssueTracker.record(issue);

    assertEquals(1, ContentIssueTracker.issues().size());
  }

  @Test
  void issuesForFiltersCorrectly() {
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.MISSING_FIELD, ContentType.STORY_ENTRY, TEST_ID, "test.json", "title_key"));
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.MISSING_FIELD,
            ContentType.THEME,
            new ResourceLocation("test", "theme_a"),
            "theme.json",
            "layout"));

    List<ContentIssue> storyIssues = ContentIssueTracker.issuesFor(ContentType.STORY_ENTRY);

    assertEquals(1, storyIssues.size());
    assertEquals(ContentType.STORY_ENTRY, storyIssues.get(0).contentType());
  }

  @Test
  void hasErrorsDetectsErrorSeverity() {
    ContentIssueTracker.record(
        ContentIssue.of(IssueCode.INVALID_ID, ContentType.STORY_ENTRY, TEST_ID, "test.json", null));

    assertTrue(ContentIssueTracker.hasErrors());
  }

  @Test
  void clearForRemovesOnlyTargetType() {
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.MISSING_FIELD, ContentType.STORY_ENTRY, TEST_ID, "test.json", "title_key"));
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.MISSING_FIELD,
            ContentType.THEME,
            new ResourceLocation("test", "theme_a"),
            "theme.json",
            "layout"));

    ContentIssueTracker.clearFor(ContentType.STORY_ENTRY);

    assertEquals(0, ContentIssueTracker.issuesFor(ContentType.STORY_ENTRY).size());
    assertEquals(1, ContentIssueTracker.issuesFor(ContentType.THEME).size());
  }

  @Test
  void clearByCodeRemovesOnlyMatchingIssues() {
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.MISSING_THEME_REFERENCE,
            ContentType.STORY_ENTRY,
            TEST_ID,
            "[cross-reference]",
            "theme"));
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.MISSING_FIELD, ContentType.STORY_ENTRY, TEST_ID, "test.json", "title_key"));

    ContentIssueTracker.clearByCode(IssueCode.MISSING_THEME_REFERENCE);

    List<ContentIssue> remaining = ContentIssueTracker.issues();
    assertEquals(1, remaining.size());
    assertEquals(IssueCode.MISSING_FIELD, remaining.get(0).code());
  }

  @Test
  void errorsForExcludesWarnings() {
    ContentIssueTracker.record(
        ContentIssue.of(
            IssueCode.DUPLICATE_ID, ContentType.STORY_ENTRY, TEST_ID, "test.json", null));

    List<ContentIssue> errors = ContentIssueTracker.errorsFor(ContentType.STORY_ENTRY);

    assertTrue(errors.isEmpty());
    assertFalse(ContentIssueTracker.hasErrors());
  }
}
