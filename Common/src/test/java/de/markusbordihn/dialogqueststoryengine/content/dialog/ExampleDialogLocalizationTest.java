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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogNodeDefinition;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ExampleDialogLocalizationTest {

  private static final String[] EXAMPLE_DIALOGS = {"hello", "merchant", "gatekeeper"};
  private static final String DIALOG_DIR = "data/dqse_example/dqse/dialogs/";
  private static final String LANG_DIR = "assets/dqse_example/lang/";

  private static JsonObject loadJson(String classpathPath) {
    try (InputStream stream =
        ExampleDialogLocalizationTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
      Objects.requireNonNull(stream, "Missing resource: " + classpathPath);
      return new Gson()
          .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static Set<String> referencedKeys() {
    Set<String> keys = new LinkedHashSet<>();
    for (String id : EXAMPLE_DIALOGS) {
      ResourceLocation contentId = new ResourceLocation("dqse_example", id);
      ParseResult<DialogDefinition> result =
          DialogContentParser.parse(contentId, id + ".json", loadJson(DIALOG_DIR + id + ".json"));
      DialogDefinition definition = result.value().orElseThrow();
      for (DialogNodeDefinition node : definition.nodes().values()) {
        keys.add(node.speakerKey());
        keys.add(node.textKey());
        node.choices().forEach(choice -> keys.add(choice.labelKey()));
      }
    }
    return keys;
  }

  private void assertAllKeysPresent(String langFile) {
    JsonObject lang = loadJson(LANG_DIR + langFile);
    for (String key : referencedKeys()) {
      assertTrue(lang.has(key), "Missing '" + key + "' in " + langFile);
    }
  }

  @Test
  void allReferencedKeysExistInEnUs() {
    assertAllKeysPresent("en_us.json");
  }

  @Test
  void allReferencedKeysExistInDeDe() {
    assertAllKeysPresent("de_de.json");
  }
}
