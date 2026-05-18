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

package de.markusbordihn.dialogqueststoryengine.content.dialog;

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

class DialogContentParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "dialog_a");
  private static final String TEST_FILE = "test.json";

  private static final ResourceLocation EXAMPLES_HELLO_ID =
      new ResourceLocation("dialog_quest_and_story_engine_examples", "hello");

  private static JsonObject loadJson(String classpathPath) {
    try (InputStream stream =
        DialogContentParserTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
  void parsesExampleHelloDialog() {
    JsonObject input =
        loadJson("data/dialog_quest_and_story_engine_examples/dqse/dialogs/hello.json");

    ParseResult<DialogDefinition> result =
        DialogContentParser.parse(EXAMPLES_HELLO_ID, "hello.json", input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    assertEquals(EXAMPLES_HELLO_ID, result.value().get().id());
    assertEquals("greeting", result.value().get().startNode());
    assertEquals(2, result.value().get().nodes().size());
    assertEquals(
        "example.villager.name", result.value().get().nodes().get("greeting").speakerKey());
  }

  @Test
  void missingSchema() {
    JsonObject input =
        json(
            """
        {
          "start_node": "root",
          "nodes": {}
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_SCHEMA));
  }

  @Test
  void unsupportedSchema() {
    JsonObject input =
        json(
            """
        {
          "schema": 999,
          "start_node": "root",
          "nodes": {}
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.UNSUPPORTED_SCHEMA));
  }

  @Test
  void missingStartNode() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "nodes": {
            "root": {
              "speaker_key": "npc.elder",
              "text_key": "dialog.test.root"
            }
          }
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
  }

  @Test
  void startNodeNotInNodes() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "start_node": "missing",
          "nodes": {
            "root": {
              "speaker_key": "npc.elder",
              "text_key": "dialog.test.root"
            }
          }
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_START_NODE));
  }

  @Test
  void duplicateNodeId() {
    JsonObject nodesJson = new JsonObject();
    JsonObject nodeA = new JsonObject();
    nodeA.addProperty("speaker_key", "npc.elder");
    nodeA.addProperty("text_key", "dialog.test.root");
    nodesJson.add("root", nodeA);

    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "start_node": "root",
          "nodes": {}
        }
        """);
    input.add("nodes", nodesJson);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertEquals(1, result.value().get().nodes().size());
  }

  @Test
  void choiceWithCloseBuiltin() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "start_node": "root",
          "nodes": {
            "root": {
              "speaker_key": "npc.elder",
              "text_key": "dialog.test.root",
              "choices": [
                {
                  "id": "c1",
                  "label_key": "dialog.choice.close",
                  "action": "close"
                }
              ]
            }
          }
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    DialogNodeDefinition node = result.value().get().nodes().get("root");
    assertEquals(1, node.choices().size());
    assertTrue(node.choices().get(0).builtin().isPresent());
    assertEquals(BuiltinChoiceAction.CLOSE, node.choices().get(0).builtin().get());
  }

  @Test
  void emptyChoicesWarning() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "start_node": "root",
          "nodes": {
            "root": {
              "speaker_key": "npc.elder",
              "text_key": "dialog.test.root"
            }
          }
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.EMPTY_CHOICES));
  }

  @Test
  void choiceWithRawConditionsPreserved() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "start_node": "root",
          "nodes": {
            "root": {
              "speaker_key": "npc.elder",
              "text_key": "dialog.test.root",
              "choices": [
                {
                  "id": "c1",
                  "label_key": "dialog.choice.unlock",
                  "conditions": [
                    { "type": "has_item", "item": "minecraft:diamond" }
                  ]
                }
              ]
            }
          }
        }
        """);

    ParseResult<DialogDefinition> result = DialogContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    DialogNodeDefinition node = result.value().get().nodes().get("root");
    assertEquals(1, node.choices().get(0).conditions().size());
    assertEquals(
        "has_item",
        node.choices().get(0).conditions().get(0).jsonObject().get("type").getAsString());
  }
}
