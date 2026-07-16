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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntry;
import de.markusbordihn.dialogqueststoryengine.data.story.StoryEntryType;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class StoryEntryParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "story_a");
  private static final String TEST_FILE = "test.json";

  private JsonObject json(String raw) {
    return GSON.fromJson(raw, JsonObject.class);
  }

  @Test
  void happyPath() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "type": "holopad",
          "title_key": "story.test.title",
          "theme": "test:my_theme",
          "pages": [
            { "text_key": "story.test.page_1" },
            { "text_key": "story.test.page_2" }
          ]
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    StoryEntry entry = result.value().orElseThrow();
    assertEquals(StoryEntryType.HOLOPAD, entry.type());
    assertEquals("story.test.title", entry.titleKey());
    assertEquals(new ResourceLocation("test", "my_theme"), entry.themeId());
    assertEquals(2, entry.pages().size());
    assertEquals("story.test.page_1", entry.pages().get(0).textKey());
  }

  @Test
  void missingSchemaProducesError() {
    JsonObject input =
        json(
            """
        {
          "type": "holopad",
          "title_key": "story.test.title",
          "theme": "test:my_theme",
          "pages": [{ "text_key": "story.test.page_1" }]
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertFalse(result.issues().isEmpty());
    assertEquals(IssueCode.MISSING_SCHEMA, result.issues().get(0).code());
  }

  @Test
  void unsupportedSchemaProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 99,
          "type": "holopad",
          "title_key": "story.test.title",
          "theme": "test:my_theme",
          "pages": [{ "text_key": "story.test.page_1" }]
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.UNSUPPORTED_SCHEMA, result.issues().get(0).code());
  }

  @Test
  void unknownTypeProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "type": "unknown_gadget",
          "title_key": "story.test.title",
          "theme": "test:my_theme",
          "pages": [{ "text_key": "story.test.page_1" }]
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.UNKNOWN_STORY_TYPE, result.issues().get(0).code());
  }

  @Test
  void missingTitleKeyProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "type": "holopad",
          "theme": "test:my_theme",
          "pages": [{ "text_key": "story.test.page_1" }]
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.MISSING_FIELD, result.issues().get(0).code());
  }

  @Test
  void invalidThemeResourceLocationProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "type": "holopad",
          "title_key": "story.test.title",
          "theme": "not a valid::rl!!",
          "pages": [{ "text_key": "story.test.page_1" }]
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.INVALID_RESOURCE_LOCATION, result.issues().get(0).code());
  }

  @Test
  void emptyPagesArrayProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "type": "holopad",
          "title_key": "story.test.title",
          "theme": "test:my_theme",
          "pages": []
        }
        """);

    ParseResult<StoryEntry> result = StoryEntryParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.EMPTY_PAGES, result.issues().get(0).code());
  }
}
