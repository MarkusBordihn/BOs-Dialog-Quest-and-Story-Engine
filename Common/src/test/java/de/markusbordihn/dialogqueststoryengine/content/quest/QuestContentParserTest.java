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
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.PrerequisiteMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardClaimMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.BuiltinActions;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class QuestContentParserTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "quest_a");
  private static final String TEST_FILE = "test.json";

  private static final ResourceLocation EXAMPLES_FIRST_QUEST_ID =
      new ResourceLocation("dqse_example", "first_quest");

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
    JsonObject input = loadJson("data/dqse_example/dqse/quests/first_quest.json");

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
  void parsesBureaucraticErrandQuestExample() {
    BuiltinActions.register();
    JsonObject input = loadJson("data/test/dqse/quests/bureaucratic_errand.json");

    ParseResult<QuestDefinition> result =
        QuestContentParser.parse(
            new ResourceLocation("test", "bureaucratic_errand"), "bureaucratic_errand.json", input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    QuestDefinition quest = result.value().get();
    assertEquals(CompletionPolicy.ALL_STEPS, quest.logic().completionPolicy());
    assertEquals(3, quest.logic().steps().size());
    assertFalse(quest.logic().restartAfterFailure());
    assertEquals(
        List.of("find_the_missing_form"),
        quest.logic().steps().get("stamp_it_three_times").requires());
    assertEquals(
        new ResourceLocation("test", "paperwork_saga"), quest.narrative().arc().orElseThrow());
    assertEquals(new ResourceLocation("minecraft", "paper"), quest.display().icon().orElseThrow());
    assertEquals(5, quest.display().sortOrder());
    assertEquals(1, quest.onComplete().size());
    assertEquals(RewardClaimMode.MANUAL, quest.rewards().claimMode());
    assertEquals(2, quest.rewards().entries().size());
    assertEquals(new RewardEntry.Experience(100), quest.rewards().entries().get(1));
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
  void restartAfterFailureDefaultsTrue() {
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
    assertTrue(result.value().get().logic().restartAfterFailure());
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
  void typedRewardEntriesParsed() {
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
          "rewards": {
            "claim_mode": "manual",
            "entries": [
              { "type": "dqse:item", "item": "minecraft:diamond", "count": 2 },
              { "type": "dqse:experience", "amount": 50 }
            ]
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    RewardSection rewards = result.value().get().rewards();
    assertEquals(RewardClaimMode.MANUAL, rewards.claimMode());
    assertEquals(
        new RewardEntry.Item(new ResourceLocation("minecraft", "diamond"), 2),
        rewards.entries().get(0));
    assertEquals(new RewardEntry.Experience(50), rewards.entries().get(1));
  }

  @Test
  void unknownRewardTypeIsReported() {
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
              "step_1": { "type": "dqse:manual" }
            }
          },
          "rewards": {
            "entries": [
              { "type": "dqse:reputation", "amount": 5 }
            ]
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.UNKNOWN_REWARD_TYPE));
    assertTrue(result.value().get().rewards().isEmpty());
  }

  @Test
  void invalidRewardAmountIsReported() {
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
              "step_1": { "type": "dqse:manual" }
            }
          },
          "rewards": {
            "entries": [
              { "type": "dqse:experience", "amount": 0 }
            ]
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(
        result.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.INVALID_REWARD_AMOUNT));
  }

  @Test
  void prerequisitesParsed() {
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
            "prerequisites": {
              "mode": "any",
              "quests": ["test:intro", "test:tutorial"]
            },
            "steps": {
              "step_1": { "type": "dqse:manual" }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    QuestPrerequisites prerequisites = result.value().get().logic().prerequisites();
    assertEquals(PrerequisiteMode.ANY, prerequisites.mode());
    assertEquals(
        List.of(new ResourceLocation("test", "intro"), new ResourceLocation("test", "tutorial")),
        prerequisites.quests());
  }

  @Test
  void stepRequiresAndDescriptionKeyParsed() {
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
              "step_1": { "type": "dqse:collect_item", "item": "minecraft:apple", "count": 1 },
              "step_2": {
                "type": "dqse:manual",
                "description_key": "quest.test.step.step_2",
                "requires": ["step_1"]
              }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(result.issues().isEmpty());
    RawQuestStep firstStep = result.value().get().logic().steps().get("step_1");
    assertEquals(Optional.empty(), firstStep.descriptionKey());
    assertEquals(List.of(), firstStep.requires());
    RawQuestStep secondStep = result.value().get().logic().steps().get("step_2");
    assertEquals(Optional.of("quest.test.step.step_2"), secondStep.descriptionKey());
    assertEquals(List.of("step_1"), secondStep.requires());
  }

  @Test
  void unknownStepPrerequisiteFailsParse() {
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
              "step_1": { "type": "dqse:manual", "requires": ["missing_step"] }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.UNKNOWN_STEP_PREREQUISITE));
  }

  @Test
  void selfStepPrerequisiteFailsParse() {
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
              "step_1": { "type": "dqse:manual", "requires": ["step_1"] }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.SELF_STEP_PREREQUISITE));
  }

  @Test
  void duplicateStepPrerequisiteFailsParse() {
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
              "step_1": { "type": "dqse:manual" },
              "step_2": { "type": "dqse:manual", "requires": ["step_1", "step_1"] }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.DUPLICATE_STEP_PREREQUISITE));
  }

  @Test
  void stepPrerequisiteCycleFailsParse() {
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
              "step_1": { "type": "dqse:manual", "requires": ["step_3"] },
              "step_2": { "type": "dqse:manual", "requires": ["step_1"] },
              "step_3": { "type": "dqse:manual", "requires": ["step_2"] }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertFalse(result.isSuccess());
    assertTrue(
        result.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.STEP_PREREQUISITE_CYCLE));
  }

  @Test
  void invalidRequiresTypeIsIgnoredWithIssue() {
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
              "step_1": { "type": "dqse:manual", "requires": "step_2" },
              "step_2": { "type": "dqse:manual" }
            }
          }
        }
        """);

    ParseResult<QuestDefinition> result = QuestContentParser.parse(TEST_ID, TEST_FILE, input);

    assertTrue(result.isSuccess());
    assertTrue(
        result.issues().stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_FIELD_TYPE));
    assertEquals(List.of(), result.value().get().logic().steps().get("step_1").requires());
  }
}
