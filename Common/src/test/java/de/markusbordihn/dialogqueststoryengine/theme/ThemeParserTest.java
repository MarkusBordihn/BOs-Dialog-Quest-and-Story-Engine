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

package de.markusbordihn.dialogqueststoryengine.theme;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ThemeParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "theme_a");
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
          "layout": "dialog_quest_and_story_engine:holopad",
          "frame_texture": "test:textures/frame.png",
          "background_texture": "test:textures/background.png",
          "show_page_numbers": true,
          "show_close_button": false,
          "display_area": { "x": 1, "y": 2, "width": 300, "height": 100 },
          "title_area": { "x": 10, "y": 104, "width": 280, "height": 12 },
          "title_alignment": "center",
          "text_area": { "x": 10, "y": 120, "width": 100, "height": 80 },
          "choice_area": { "x": 40, "y": 30, "width": 220, "height": 60 }
        }
        """);

    ParseResult<Theme> result = ThemeParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    Theme theme = result.value().orElseThrow();
    assertEquals(ThemeLayout.HOLOPAD, theme.layout());
    assertTrue(theme.showPageNumbers());
    assertFalse(theme.showCloseButton());
    assertEquals(1, theme.displayArea().x());
    assertEquals(ThemeTextAlignment.CENTER, theme.titleAlignment());
    assertEquals(10, theme.textArea().x());
    assertEquals(80, theme.textArea().height());
    assertEquals(220, theme.choiceArea().width());
  }

  @Test
  void missingSchemaProducesError() {
    JsonObject input =
        json(
            """
        {
          "layout": "dialog_quest_and_story_engine:holopad",
          "frame_texture": "test:textures/frame.png",
          "background_texture": "test:textures/background.png",
          "show_page_numbers": true,
          "show_close_button": true,
          "display_area": { "x": 0, "y": 0, "width": 100, "height": 40 },
          "title_area": { "x": 0, "y": 40, "width": 100, "height": 12 },
          "title_alignment": "left",
          "text_area": { "x": 0, "y": 52, "width": 100, "height": 80 },
          "choice_area": { "x": 0, "y": 132, "width": 100, "height": 40 }
        }
        """);

    ParseResult<Theme> result = ThemeParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.MISSING_SCHEMA, result.issues().get(0).code());
  }

  @Test
  void unknownLayoutProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "layout": "test:nonexistent_layout",
          "frame_texture": "test:textures/frame.png",
          "background_texture": "test:textures/background.png",
          "show_page_numbers": true,
          "show_close_button": true,
          "display_area": { "x": 0, "y": 0, "width": 100, "height": 40 },
          "title_area": { "x": 0, "y": 40, "width": 100, "height": 12 },
          "title_alignment": "left",
          "text_area": { "x": 0, "y": 52, "width": 100, "height": 80 },
          "choice_area": { "x": 0, "y": 132, "width": 100, "height": 40 }
        }
        """);

    ParseResult<Theme> result = ThemeParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.UNKNOWN_THEME_LAYOUT, result.issues().get(0).code());
  }

  @Test
  void missingTextAreaFieldProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "layout": "dialog_quest_and_story_engine:holopad",
          "frame_texture": "test:textures/frame.png",
          "background_texture": "test:textures/background.png",
          "show_page_numbers": true,
          "show_close_button": true,
          "display_area": { "x": 0, "y": 0, "width": 100, "height": 40 },
          "title_area": { "x": 0, "y": 40, "width": 100, "height": 12 },
          "title_alignment": "left",
          "choice_area": { "x": 0, "y": 132, "width": 100, "height": 40 }
        }
        """);

    ParseResult<Theme> result = ThemeParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.MISSING_FIELD, result.issues().get(0).code());
  }

  @Test
  void invalidThemeAreaDimensionsProduceError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "layout": "dialog_quest_and_story_engine:holopad",
          "frame_texture": "test:textures/frame.png",
          "background_texture": "test:textures/background.png",
          "show_page_numbers": true,
          "show_close_button": true,
          "display_area": { "x": 0, "y": 0, "width": 100, "height": 40 },
          "title_area": { "x": 0, "y": 40, "width": 100, "height": 12 },
          "title_alignment": "left",
          "text_area": { "x": 0, "y": 52, "width": 0, "height": 80 },
          "choice_area": { "x": 0, "y": 132, "width": 100, "height": 40 }
        }
        """);

    ParseResult<Theme> result = ThemeParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, result.issues().get(0).code());
  }

  @Test
  void invalidTitleAlignmentProducesError() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "layout": "dialog_quest_and_story_engine:holopad",
          "frame_texture": "test:textures/frame.png",
          "background_texture": "test:textures/background.png",
          "show_page_numbers": true,
          "show_close_button": true,
          "display_area": { "x": 0, "y": 0, "width": 100, "height": 40 },
          "title_area": { "x": 0, "y": 40, "width": 100, "height": 12 },
          "title_alignment": "middle",
          "text_area": { "x": 0, "y": 52, "width": 100, "height": 80 },
          "choice_area": { "x": 0, "y": 132, "width": 100, "height": 40 }
        }
        """);

    ParseResult<Theme> result = ThemeParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.INVALID_FIELD_TYPE, result.issues().get(0).code());
  }
}
