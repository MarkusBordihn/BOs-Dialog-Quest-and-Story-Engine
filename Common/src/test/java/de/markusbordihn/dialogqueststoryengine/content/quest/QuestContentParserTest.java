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

package de.markusbordihn.dialogqueststoryengine.content.quest;

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

class QuestContentParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "quest_a");
  private static final String TEST_FILE = "test.json";

  private static final ResourceLocation EXAMPLES_FIRST_QUEST_ID =
      new ResourceLocation("dialog_quest_and_story_engine_examples", "first_quest");

  private static JsonObject loadJson(String classpathPath) {
    try (InputStream stream =
        QuestContentParserTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
  void parsesExampleFirstQuest() {
    JsonObject input =
        loadJson("data/dialog_quest_and_story_engine_examples/dqse/quests/first_quest.json");

    ParseResult<QuestDefinition> result =
        QuestContentParser.parse(EXAMPLES_FIRST_QUEST_ID, "first_quest.json", input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    assertEquals(EXAMPLES_FIRST_QUEST_ID, result.value().get().id());
    assertEquals("example.quest.first_quest.title", result.value().get().display().titleKey());
    assertEquals(2, result.value().get().logic().steps().size());
    assertEquals(CompletionPolicy.ALL_STEPS, result.value().get().logic().completionPolicy());
  }

  @Test
  void missingDisplay() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "logic": {
            "steps": {}
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
  }

  @Test
  void missingLogic() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": {
            "title_key": "quest.test.title",
            "description_key": "quest.test.desc"
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
  }

  @Test
  void emptyStepsWarning() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": {
            "title_key": "quest.test.title",
            "description_key": "quest.test.desc"
          },
          "logic": {
            "steps": {}
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.EMPTY_STEPS));
  }

  @Test
  void repeatableDefaultsFalse() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": {
            "title_key": "quest.test.title",
            "description_key": "quest.test.desc"
          },
          "logic": {
            "steps": {
              "step_1": { "type": "dqse:collect_item", "item": "minecraft:apple", "count": 1 }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertFalse(result.value().get().logic().repeatable());
  }

  @Test
  void anyStepCompletionPolicy() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": {
            "title_key": "quest.test.title",
            "description_key": "quest.test.desc"
          },
          "logic": {
            "completion_policy": "any_step",
            "steps": {
              "step_1": { "type": "dqse:collect_item", "item": "minecraft:apple", "count": 1 }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertEquals(CompletionPolicy.ANY_STEP, result.value().get().logic().completionPolicy());
  }

  @Test
  void visibilityConditionParsed() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": {
            "title_key": "quest.test.title",
            "description_key": "quest.test.desc"
          },
          "logic": {
            "steps": {
              "step_1": { "type": "dqse:collect_item", "item": "minecraft:apple", "count": 1 }
            },
            "visibility": {
              "when": { "type": "has_flag", "flag": "quest_unlocked" }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(result.value().get().logic().visibilityCondition().isPresent());
  }

  @Test
  void rewardsParsedAsRawActions() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "display": {
            "title_key": "quest.test.title",
            "description_key": "quest.test.desc"
          },
          "logic": {
            "steps": {
              "step_1": { "type": "dqse:collect_item", "item": "minecraft:apple", "count": 1 }
            }
          },
          "rewards": [
            { "type": "dqse:give_item", "item": "minecraft:diamond", "count": 1 }
          ]
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertEquals(1, result.value().get().rewards().actions().size());
  }
}
