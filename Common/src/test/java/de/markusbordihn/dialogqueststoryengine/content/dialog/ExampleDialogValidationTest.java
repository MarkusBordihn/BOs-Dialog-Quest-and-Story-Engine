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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.validation.DialogNodeReferenceValidator;
import de.markusbordihn.dialogqueststoryengine.validation.DialogReachabilityValidator;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ExampleDialogValidationTest {

  private static final String DIALOG_DIR = "data/dqse_example/dqse/dialogs/";

  private static JsonObject loadJson(String classpathPath) {
    try (InputStream stream =
        ExampleDialogValidationTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
      Objects.requireNonNull(stream, "Missing example resource: " + classpathPath);
      return new Gson()
          .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static DialogDefinition parse(String id) {
    ResourceLocation contentId = new ResourceLocation("dqse_example", id);
    ParseResult<DialogDefinition> result =
        DialogContentParser.parse(contentId, id + ".json", loadJson(DIALOG_DIR + id + ".json"));
    assertTrue(result.isSuccess(), "Example dialog '" + id + "' should parse");
    return result.value().orElseThrow();
  }

  @AfterEach
  void clearRegistry() {
    DialogContentRegistry.clear();
  }

  @Test
  void exampleDialogsHaveNoValidatorIssues() {
    Map<ResourceLocation, DialogDefinition> dialogs = new HashMap<>();
    for (String id : new String[] {"hello", "merchant", "gatekeeper"}) {
      DialogDefinition definition = parse(id);
      dialogs.put(definition.id(), definition);
    }
    DialogContentRegistry.replaceAll(dialogs);

    assertEquals(
        java.util.List.of(),
        new DialogNodeReferenceValidator().validate(),
        "Node reference validator should report no issues for example dialogs");
    assertEquals(
        java.util.List.of(),
        new DialogReachabilityValidator().validate(),
        "Reachability validator should report no issues for example dialogs");
  }
}
