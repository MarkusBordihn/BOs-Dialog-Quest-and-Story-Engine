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

package de.markusbordihn.dialogqueststoryengine.content.story;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class InteractiveStoryContentParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "story_a");
  private static final String TEST_FILE = "test.json";

  private static final ResourceLocation EXAMPLES_VILLAGE_INTRO_ID =
      new ResourceLocation("dialog_quest_and_story_engine_examples", "village_intro");

  private static JsonObject loadJson(String classpathPath) {
    try (InputStream stream =
        InteractiveStoryContentParserTest.class
            .getClassLoader()
            .getResourceAsStream(classpathPath)) {
      Objects.requireNonNull(stream, "Missing test resource: " + classpathPath);
      return new Gson()
          .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private JsonObject json(String raw) {
    return GSON.fromJson(raw, JsonObject.class);
  }

  @Test
  void parsesExampleVillageIntro() {
    JsonObject input =
        loadJson(
            "data/dialog_quest_and_story_engine_examples/dqse/interactive_story/village_intro.json");

    ParseResult<InteractiveStoryDefinition> result =
        InteractiveStoryContentParser.parse(EXAMPLES_VILLAGE_INTRO_ID, "village_intro.json", input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    assertEquals(EXAMPLES_VILLAGE_INTRO_ID, result.value().get().id());
    assertEquals(
        new ResourceLocation("dialog_quest_and_story_engine_examples", "village_intro"),
        result.value().get().displayStoryId());
    assertEquals(InteractiveStoryMode.SERVER_SYNCED, result.value().get().mode());
    assertEquals(2, result.value().get().choices().size());
    assertEquals(1, result.value().get().onOpen().size());
  }

  @Test
  void missingMode() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": "test:story_layout"
        }
        """);

    ParseResult<InteractiveStoryDefinition> result =
        InteractiveStoryContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
  }

  @Test
  void missingDisplayStoryId() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "mode": "server_synced"
        }
        """);

    ParseResult<InteractiveStoryDefinition> result =
        InteractiveStoryContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
  }

  @Test
  void unknownMode() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": "test:story_layout",
          "mode": "invalid_mode"
        }
        """);

    ParseResult<InteractiveStoryDefinition> result =
        InteractiveStoryContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.UNKNOWN_STORY_MODE));
  }

  @Test
  void onOpenParsedAsRawActions() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": "test:story_layout",
          "mode": "client_only",
          "on_open": [
            { "type": "dqse:play_sound", "sound": "minecraft:block.note_block.harp" }
          ]
        }
        """);

    ParseResult<InteractiveStoryDefinition> result =
        InteractiveStoryContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertEquals(1, result.value().get().onOpen().size());
    assertEquals(
        "dqse:play_sound",
        result.value().get().onOpen().get(0).jsonObject().get("type").getAsString());
  }

  @Test
  void choiceConditionsPreserved() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": "test:story_layout",
          "mode": "server_synced",
          "choices": [
            {
              "id": "accept",
              "label_key": "story.choice.accept",
              "conditions": [
                { "type": "has_flag", "flag": "story_started" }
              ]
            }
          ]
        }
        """);

    ParseResult<InteractiveStoryDefinition> result =
        InteractiveStoryContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertEquals(1, result.value().get().choices().get(0).conditions().members().size());
  }
}
