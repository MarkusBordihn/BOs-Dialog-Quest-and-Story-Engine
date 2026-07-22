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
import de.markusbordihn.dialogqueststoryengine.data.theme.BuiltinLayouts;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ThemeParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "theme_a");
  private static final String TEST_FILE = "test.json";

  @BeforeAll
  static void registerProviders() {
    if (!Registries.THEMES.contains(BuiltinLayouts.HOLOPAD)) {
      BuiltinThemeProviders.register();
    }
  }

  private static boolean hasCode(ParseResult<Theme> result, IssueCode code) {
    return result.issues().stream().anyMatch(issue -> issue.code() == code);
  }

  private static String holopadWith(String body) {
    return "{\"schema\": 1, \"layout\": \"dqse:holopad\", \"logical_width\": 320, "
        + "\"logical_height\": 240, "
        + body.strip()
        + "}";
  }

  private JsonObject json(String raw) {
    return GSON.fromJson(raw, JsonObject.class);
  }

  @Test
  void happyPath() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                """
            {
              "schema": 1,
              "layout": "dqse:holopad",
              "logical_width": 320,
              "logical_height": 240,
              "areas": {
                "text": {"x": 24, "y": 44, "width": 272, "height": 78},
                "choices": {"x": 60, "y": 128, "width": 200, "height": 62}
              },
              "colors": {"accent": "#35E0D5"},
              "options": {"title_alignment": "center", "show_page_numbers": true}
            }
            """));

    assertTrue(result.isSuccess());
    Theme theme = result.value().orElseThrow();
    assertEquals(BuiltinLayouts.HOLOPAD, theme.layoutId());
    assertEquals(320, theme.logicalWidth());
    assertTrue(theme.areas().containsKey("text"));
    assertEquals(0xFF35E0D5, theme.colors().get("accent"));
    assertEquals(ThemeTextAlignment.CENTER, theme.options().get("title_alignment"));
    assertEquals(Boolean.TRUE, theme.options().get("show_page_numbers"));
  }

  @Test
  void missingSchemaProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                """
            {
              "layout": "dqse:holopad",
              "logical_width": 320,
              "logical_height": 240,
              "areas": {"text": {"x": 0, "y": 0, "width": 100, "height": 40}}
            }
            """));

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.MISSING_SCHEMA, result.issues().get(0).code());
  }

  @Test
  void unknownLayoutProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                """
            {
              "schema": 1,
              "layout": "test:nonexistent_layout",
              "logical_width": 320,
              "logical_height": 240,
              "areas": {"text": {"x": 0, "y": 0, "width": 100, "height": 40}}
            }
            """));

    assertFalse(result.isSuccess());
    assertEquals(IssueCode.UNKNOWN_THEME_LAYOUT, result.issues().get(0).code());
  }

  @Test
  void missingRequiredAreaProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {"choices": {"x": 60, "y": 128, "width": 200, "height": 62}}
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.MISSING_THEME_SLOT));
  }

  @Test
  void unknownSlotProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {
              "text": {"x": 24, "y": 44, "width": 272, "height": 78},
              "choices": {"x": 60, "y": 128, "width": 200, "height": 62},
              "bogus": {"x": 0, "y": 0, "width": 10, "height": 10}
            }
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.UNKNOWN_THEME_SLOT));
  }

  @Test
  void areaOutOfBoundsProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {
              "text": {"x": 24, "y": 44, "width": 400, "height": 78},
              "choices": {"x": 60, "y": 128, "width": 200, "height": 62}
            }
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.THEME_AREA_OUT_OF_BOUNDS));
  }

  @Test
  void invalidNineSliceSpriteProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {
              "text": {"x": 24, "y": 44, "width": 272, "height": 78},
              "choices": {"x": 60, "y": 128, "width": 200, "height": 62}
            },
            "sprites": {
              "frame": {
                "texture": "test:textures/frame.png",
                "texture_width": 512, "texture_height": 256,
                "source_width": 16, "source_height": 16,
                "scaling": "nine_slice",
                "border": 8
              }
            }
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.INVALID_THEME_SPRITE));
  }

  @Test
  void invalidColorProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {
              "text": {"x": 24, "y": 44, "width": 272, "height": 78},
              "choices": {"x": 60, "y": 128, "width": 200, "height": 62}
            },
            "colors": {"accent": "#ZZZZZZ"}
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.INVALID_THEME_COLOR));
  }

  @Test
  void unknownOptionKeyProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {
              "text": {"x": 24, "y": 44, "width": 272, "height": 78},
              "choices": {"x": 60, "y": 128, "width": 200, "height": 62}
            },
            "options": {"bogus_option": "value"}
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.INVALID_THEME_OPTION));
  }

  @Test
  void invalidOptionValueProducesError() {
    ParseResult<Theme> result =
        ThemeParser.parse(
            TEST_ID,
            TEST_FILE,
            this.json(
                holopadWith(
                    """
            "areas": {
              "text": {"x": 24, "y": 44, "width": 272, "height": 78},
              "choices": {"x": 60, "y": 128, "width": 200, "height": 62}
            },
            "options": {"title_alignment": "diagonal"}
            """)));

    assertFalse(result.isSuccess());
    assertTrue(hasCode(result, IssueCode.INVALID_THEME_OPTION));
  }
}
