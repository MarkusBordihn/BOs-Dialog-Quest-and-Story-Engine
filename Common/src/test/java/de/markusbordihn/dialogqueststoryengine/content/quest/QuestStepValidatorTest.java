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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.quest.step.BuiltinQuestSteps;
import de.markusbordihn.dialogqueststoryengine.validation.QuestStepValidator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QuestStepValidatorTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  @BeforeAll
  static void registerStepTypes() {
    BuiltinQuestSteps.register();
  }

  private static void installQuest(RawQuestStep... steps) {
    Map<String, RawQuestStep> stepMap = new LinkedHashMap<>();
    for (RawQuestStep step : steps) {
      stepMap.put(step.id(), step);
    }
    QuestContentRegistry.replaceAll(
        Map.of(
            QUEST_ID,
            new QuestDefinition(
                QUEST_ID,
                1,
                NarrativeMetadata.EMPTY,
                new DisplaySection(
                    "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
                new LogicSection(
                    Optional.empty(),
                    QuestPrerequisites.NONE,
                    stepMap,
                    CompletionPolicy.ALL_STEPS,
                    true),
                ActionList.EMPTY,
                RewardSection.EMPTY)));
  }

  private static RawQuestStep step(String id, String type, String extraFields) {
    String suffix = extraFields.isEmpty() ? "" : ", " + extraFields;
    return new RawQuestStep(
        id,
        new ResourceLocation(type),
        JsonParser.parseString("{\"type\":\"" + type + "\"" + suffix + "}").getAsJsonObject());
  }

  @AfterEach
  void clearRegistry() {
    QuestContentRegistry.clear();
  }

  @Test
  void validStepsProduceNoIssues() {
    installQuest(
        step("collect", "dqse:collect_item", "\"item\": \"minecraft:oak_log\", \"count\": 5"),
        step("talk", "dqse:interact_entity", "\"target\": \"minecraft:villager\""),
        step("hunt", "dqse:kill_entity", "\"entity\": \"minecraft:zombie\""),
        step("done", "dqse:manual", ""));

    List<ContentIssue> issues = new QuestStepValidator().validate();

    assertTrue(issues.isEmpty(), () -> "Unexpected issues: " + issues);
  }

  @Test
  void unknownStepTypeIsReported() {
    installQuest(step("custom", "other_mod:custom_step", ""));

    List<ContentIssue> issues = new QuestStepValidator().validate();

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_STEP_TYPE, issues.get(0).code());
    assertEquals(QUEST_ID, issues.get(0).id());
  }

  @Test
  void reservedLocationStepTypeIsReportedAsUnsupported() {
    installQuest(step("goto", "dqse:location", ""));

    List<ContentIssue> issues = new QuestStepValidator().validate();

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNSUPPORTED_STEP_TYPE, issues.get(0).code());
  }

  @Test
  void handlerFieldValidationIsApplied() {
    installQuest(step("collect", "dqse:collect_item", "\"count\": 5"));

    List<ContentIssue> issues = new QuestStepValidator().validate();

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }
}
