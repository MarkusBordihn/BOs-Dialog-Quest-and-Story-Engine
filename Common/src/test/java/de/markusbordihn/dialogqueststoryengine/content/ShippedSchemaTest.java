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

package de.markusbordihn.dialogqueststoryengine.content;

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.Set;
import org.junit.jupiter.api.Test;

class ShippedSchemaTest {

  private static final Set<SchemaFixture> FIXTURES =
      Set.of(
          new SchemaFixture(
              "schemas/dialog/dialog.schema.json", "data/dqse_example/dqse/dialogs/hello.json"),
          new SchemaFixture(
              "schemas/quest/quest.schema.json", "data/dqse_example/dqse/quests/first_quest.json"),
          new SchemaFixture(
              "schemas/interaction/interaction.schema.json",
              "data/dqse_example/dqse/interactions/open_hello_dialog.json"),
          new SchemaFixture(
              "schemas/story/interactive_story.schema.json",
              "data/dqse_example/dqse/interactive_story/village_intro.json"),
          new SchemaFixture(
              "schemas/story/story_entry.schema.json",
              "assets/dqse_example/dqse/story_entries/builtin_intro.json"),
          new SchemaFixture(
              "schemas/theme/theme.schema.json",
              "assets/dqse_example/dqse/themes/builtin_holopad.json"));

  private static JsonObject load(String classpathPath) {
    try (InputStream stream =
        ShippedSchemaTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
      Objects.requireNonNull(stream, "Missing resource: " + classpathPath);
      return new Gson()
          .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  @Test
  void schemasAreWellFormedAndStrict() {
    for (SchemaFixture fixture : FIXTURES) {
      JsonObject schema = load(fixture.schemaPath());
      assertTrue(
          schema.has("$schema") && schema.get("$schema").getAsString().contains("draft-07"),
          () -> fixture.schemaPath() + " must declare the draft-07 meta-schema");
      assertTrue(
          schema.has("additionalProperties") && !schema.get("additionalProperties").getAsBoolean(),
          () -> fixture.schemaPath() + " must set additionalProperties: false");
      assertTrue(
          schema.has("properties") && schema.get("properties").isJsonObject(),
          () -> fixture.schemaPath() + " must declare properties");
    }
  }

  @Test
  void everyExampleFieldIsDeclaredInItsSchema() {
    for (SchemaFixture fixture : FIXTURES) {
      Set<String> declared = load(fixture.schemaPath()).getAsJsonObject("properties").keySet();
      JsonObject example = load(fixture.examplePath());
      for (String key : example.keySet()) {
        assertTrue(
            declared.contains(key),
            () ->
                fixture.examplePath()
                    + " uses top-level field '"
                    + key
                    + "' that is not declared in "
                    + fixture.schemaPath());
      }
    }
  }

  private record SchemaFixture(String schemaPath, String examplePath) {}
}
