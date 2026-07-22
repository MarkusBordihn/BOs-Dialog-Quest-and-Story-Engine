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

import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.theme.BuiltinLayouts;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class ShippedThemeLoadTest {

  private static final String THEMES = "assets/dialog_quest_and_story_engine/dqse/themes/";
  private static final String EXAMPLE = "assets/dqse_example/dqse/themes/";

  private static final List<String> SHIPPED_THEMES =
      List.of(
          THEMES + "default_dialog.json",
          THEMES + "default_dialog_letterbox.json",
          THEMES + "default_holopad.json",
          THEMES + "default_journal_book.json",
          THEMES + "default_journal_panel.json",
          THEMES + "default_quest_flow.json",
          THEMES + "default_reward_claim.json",
          EXAMPLE + "builtin_holopad.json",
          EXAMPLE + "amber_holopad.json",
          EXAMPLE + "portrait_duo_dialog.json");

  @BeforeAll
  static void registerProviders() {
    if (!Registries.THEMES.contains(BuiltinLayouts.HOLOPAD)) {
      BuiltinThemeProviders.register();
    }
  }

  private static JsonObject load(String classpathPath) {
    try (InputStream stream =
        ShippedThemeLoadTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
      Objects.requireNonNull(stream, "Missing resource: " + classpathPath);
      return new Gson()
          .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    } catch (Exception e) {
      throw new RuntimeException("Failed to load " + classpathPath, e);
    }
  }

  @Test
  void everyShippedThemeParsesWithoutError() {
    for (String path : SHIPPED_THEMES) {
      JsonObject json = load(path);
      ResourceLocation id = new ResourceLocation("test", "shipped");
      ParseResult<Theme> result = ThemeParser.parse(id, path, json);
      assertTrue(result.isSuccess(), () -> path + " failed to parse cleanly: " + result.issues());
    }
  }
}
