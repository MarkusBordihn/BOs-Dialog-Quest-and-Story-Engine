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
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentParser;
import de.markusbordihn.dialogqueststoryengine.content.interaction.InteractionContentParser;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentParser;
import de.markusbordihn.dialogqueststoryengine.content.story.InteractiveStoryContentParser;
import de.markusbordihn.dialogqueststoryengine.core.DqseBootstrap;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntryParser;
import de.markusbordihn.dialogqueststoryengine.theme.ThemeParser;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.function.BiFunction;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class BundledExampleLoadTest {

  @BeforeAll
  static void registerBuiltins() {
    DqseBootstrap.initialize();
  }

  private static JsonObject load(String classpathPath) {
    try (InputStream stream =
        BundledExampleLoadTest.class.getClassLoader().getResourceAsStream(classpathPath)) {
      Objects.requireNonNull(stream, "Missing bundled example: " + classpathPath);
      return new Gson()
          .fromJson(new InputStreamReader(stream, StandardCharsets.UTF_8), JsonObject.class);
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static void assertLoadsWithoutIssues(
      String classpathPath, BiFunction<ResourceLocation, JsonObject, ParseResult<?>> parser) {
    ResourceLocation id = new ResourceLocation("dqse_example", "bundled");
    ParseResult<?> result = parser.apply(id, load(classpathPath));
    List<ContentIssue> issues = result.issues();
    assertTrue(result.isSuccess(), () -> classpathPath + " failed to parse: " + issues);
    assertTrue(issues.isEmpty(), () -> classpathPath + " produced issues: " + issues);
  }

  @Test
  void bundledDialogsLoadWithoutIssues() {
    for (String name : List.of("hello", "gatekeeper", "merchant")) {
      assertLoadsWithoutIssues(
          "data/dqse_example/dqse/dialogs/" + name + ".json",
          (id, json) -> DialogContentParser.parse(id, name + ".json", json));
    }
  }

  @Test
  void bundledQuestsLoadWithoutIssues() {
    for (String name : List.of("first_quest", "hunt_quest")) {
      assertLoadsWithoutIssues(
          "data/dqse_example/dqse/quests/" + name + ".json",
          (id, json) -> QuestContentParser.parse(id, name + ".json", json));
    }
  }

  @Test
  void bundledInteractionsLoadWithoutIssues() {
    for (String name : List.of("entity_bound_dialog", "open_hello_dialog")) {
      assertLoadsWithoutIssues(
          "data/dqse_example/dqse/interactions/" + name + ".json",
          (id, json) -> InteractionContentParser.parse(id, name + ".json", json));
    }
  }

  @Test
  void bundledInteractiveStoryLoadsWithoutIssues() {
    assertLoadsWithoutIssues(
        "data/dqse_example/dqse/interactive_story/village_intro.json",
        (id, json) -> InteractiveStoryContentParser.parse(id, "village_intro.json", json));
  }

  @Test
  void bundledStoryEntriesLoadWithoutIssues() {
    assertLoadsWithoutIssues(
        "assets/dqse_example/dqse/story_entries/builtin_intro.json",
        (id, json) -> StoryEntryParser.parse(id, "builtin_intro.json", json));
  }

  @Test
  void bundledThemesLoadWithoutIssues() {
    assertLoadsWithoutIssues(
        "assets/dqse_example/dqse/themes/builtin_holopad.json",
        (id, json) -> ThemeParser.parse(id, "builtin_holopad.json", json));
    for (String name : List.of("default_dialog", "default_holopad")) {
      assertLoadsWithoutIssues(
          "assets/dialog_quest_and_story_engine/dqse/themes/" + name + ".json",
          (id, json) -> ThemeParser.parse(id, name + ".json", json));
    }
  }
}
