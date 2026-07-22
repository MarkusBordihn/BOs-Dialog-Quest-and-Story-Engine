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

package de.markusbordihn.dialogqueststoryengine.content.text;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.text.LocalizedTextSource;
import de.markusbordihn.dialogqueststoryengine.logic.context.BuiltinContextValueProviders;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class LocalizedTextParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");
  private static final String TEST_FILE = "test.json";

  @BeforeAll
  static void registerProviders() {
    BuiltinContextValueProviders.register();
  }

  private static JsonObject json(String raw) {
    return GSON.fromJson(raw, JsonObject.class);
  }

  private static Optional<LocalizedTextSource> parse(JsonObject input, List<ContentIssue> issues) {
    return LocalizedTextParser.parse(
        input, "text", true, ContentType.DIALOG, TEST_ID, TEST_FILE, issues);
  }

  @Test
  void parsesLiteralText() {
    List<ContentIssue> issues = new ArrayList<>();
    Optional<LocalizedTextSource> result = parse(json("{\"text\": \"Hello traveler!\"}"), issues);

    assertTrue(issues.isEmpty());
    assertTrue(result.isPresent());
    assertTrue(result.get().literal());
    assertEquals("Hello traveler!", result.get().value());
  }

  @Test
  void parsesKeyWithoutArguments() {
    List<ContentIssue> issues = new ArrayList<>();
    Optional<LocalizedTextSource> result =
        parse(json("{\"text_key\": \"dialog.example\"}"), issues);

    assertTrue(issues.isEmpty());
    assertTrue(result.isPresent());
    assertFalse(result.get().literal());
    assertEquals("dialog.example", result.get().value());
    assertTrue(result.get().arguments().isEmpty());
  }

  @Test
  void parsesKeyWithContextAndFactArguments() {
    List<ContentIssue> issues = new ArrayList<>();
    Optional<LocalizedTextSource> result =
        parse(
            json(
                """
            {
              "text_key": "dialog.example.guard.warning",
              "text_args": [
                { "type": "dqse:context", "key": "player_name" },
                { "type": "dqse:fact", "scope": "player", "fact": "example:alert_level" }
              ]
            }
            """),
            issues);

    assertTrue(issues.isEmpty(), () -> "Unexpected issues: " + issues);
    assertTrue(result.isPresent());
    assertEquals(2, result.get().arguments().size());
    assertEquals(new ResourceLocation("dqse", "context"), result.get().arguments().get(0).type());
    assertEquals("example:alert_level", result.get().arguments().get(1).parameter("fact"));
  }

  @Test
  void literalAndKeyAreMutuallyExclusive() {
    List<ContentIssue> issues = new ArrayList<>();
    Optional<LocalizedTextSource> result =
        parse(json("{\"text\": \"Hi\", \"text_key\": \"dialog.example\"}"), issues);

    assertTrue(result.isEmpty());
    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.LOCALIZED_TEXT_CONFLICT));
  }

  @Test
  void argumentsRequireKeyForm() {
    List<ContentIssue> issues = new ArrayList<>();
    parse(
        json(
            """
        {
          "text": "Hi",
          "text_args": [ { "type": "dqse:context", "key": "player_name" } ]
        }
        """),
        issues);

    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.LOCALIZED_ARGS_WITHOUT_KEY));
  }

  @Test
  void unknownProviderIsReported() {
    List<ContentIssue> issues = new ArrayList<>();
    parse(
        json(
            """
        {
          "text_key": "dialog.example",
          "text_args": [ { "type": "other_mod:custom" } ]
        }
        """),
        issues);

    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.UNKNOWN_CONTEXT_PROVIDER));
  }

  @Test
  void invalidContextKeyIsReported() {
    List<ContentIssue> issues = new ArrayList<>();
    parse(
        json(
            """
        {
          "text_key": "dialog.example",
          "text_args": [ { "type": "dqse:context", "key": "nonexistent" } ]
        }
        """),
        issues);

    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_CONTEXT_ARGUMENT));
  }

  @Test
  void missingRequiredFieldIsReported() {
    List<ContentIssue> issues = new ArrayList<>();
    Optional<LocalizedTextSource> result = parse(json("{}"), issues);

    assertTrue(result.isEmpty());
    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
  }

  @Test
  void optionalAbsentFieldProducesNoIssue() {
    List<ContentIssue> issues = new ArrayList<>();
    Optional<LocalizedTextSource> result =
        LocalizedTextParser.parse(
            json("{}"), "text", false, ContentType.DIALOG, TEST_ID, TEST_FILE, issues);

    assertTrue(result.isEmpty());
    assertTrue(issues.isEmpty());
  }
}
