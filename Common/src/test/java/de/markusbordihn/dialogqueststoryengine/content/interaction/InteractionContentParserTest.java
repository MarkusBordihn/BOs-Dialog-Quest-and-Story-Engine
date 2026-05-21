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

package de.markusbordihn.dialogqueststoryengine.content.interaction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
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

class InteractionContentParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "interaction_a");
  private static final String TEST_FILE = "test.json";

  private static final ResourceLocation EXAMPLES_ENTITY_BOUND_ID =
      new ResourceLocation("dialog_quest_and_story_engine_examples", "entity_bound_dialog");
  private static final ResourceLocation EXAMPLES_OPEN_HELLO_ID =
      new ResourceLocation("dialog_quest_and_story_engine_examples", "open_hello_dialog");

  private static JsonObject loadJson(String classpathPath) {
    try (InputStream stream =
        InteractionContentParserTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
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
  void parsesExampleEntityBoundDialog() {
    JsonObject input =
        loadJson(
            "data/dialog_quest_and_story_engine_examples/dqse/interactions/entity_bound_dialog.json");

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(EXAMPLES_ENTITY_BOUND_ID, "entity_bound_dialog.json", input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    assertEquals(InteractionEventType.ON_ENTITY_INTERACT, result.value().get().event());
    assertInstanceOf(InteractionBinding.EntityBinding.class, result.value().get().binding());
    assertEquals(1, result.value().get().actions().size());
  }

  @Test
  void happyPathBlock() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "event": "on_block_interact",
          "binding": {
            "kind": "block",
            "dimension": "minecraft:overworld",
            "x": 10,
            "y": 64,
            "z": -5
          }
        }
        """);

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertInstanceOf(InteractionBinding.BlockBinding.class, result.value().get().binding());
    InteractionBinding.BlockBinding block =
        (InteractionBinding.BlockBinding) result.value().get().binding();
    assertEquals(10, block.pos().getX());
    assertEquals(64, block.pos().getY());
    assertEquals(-5, block.pos().getZ());
  }

  @Test
  void parsesExampleOpenHelloDialog() {
    JsonObject input =
        loadJson(
            "data/dialog_quest_and_story_engine_examples/dqse/interactions/open_hello_dialog.json");

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(EXAMPLES_OPEN_HELLO_ID, "open_hello_dialog.json", input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    assertEquals(InteractionEventType.ON_ENTITY_INTERACT, result.value().get().event());
    assertInstanceOf(InteractionBinding.UnboundBinding.class, result.value().get().binding());
    assertEquals(1, result.value().get().actions().size());
  }

  @Test
  void unknownEvent() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "event": "on_magic_spell"
        }
        """);

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.UNKNOWN_INTERACTION_EVENT));
  }

  @Test
  void invalidUuid() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "event": "on_entity_interact",
          "binding": {
            "kind": "entity",
            "target_id": "not-a-uuid",
            "dimension": "minecraft:overworld"
          }
        }
        """);

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_UUID));
  }

  @Test
  void unknownBindingKind() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "event": "on_entity_interact",
          "binding": {
            "kind": "spaceship"
          }
        }
        """);

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.UNKNOWN_BINDING_KIND));
  }

  @Test
  void conditionsAndActionsPreserved() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "event": "on_entity_interact",
          "conditions": [
            { "type": "is_daytime" }
          ],
          "actions": [
            { "type": "dialog.open", "dialog": "test:hello" }
          ]
        }
        """);

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertEquals(1, result.value().get().conditions().members().size());
    assertEquals(1, result.value().get().actions().size());
    assertEquals(
        "dialog.open",
        result.value().get().actions().get(0).jsonObject().get("type").getAsString());
  }

  @Test
  void invalidBlockPos() {
    JsonObject input =
        json(
            """
        {
          "schema": 1,
          "event": "on_block_interact",
          "binding": {
            "kind": "block",
            "dimension": "minecraft:overworld"
          }
        }
        """);

    ParseResult<InteractionDefinition> result =
        InteractionContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_BLOCK_POS));
  }
}
