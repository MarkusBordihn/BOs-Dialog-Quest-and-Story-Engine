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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.quest.step.BuiltinQuestSteps;
import de.markusbordihn.dialogqueststoryengine.quest.step.types.CollectItemStepType;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class QuestStepLifecycleTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "lifecycle_quest");

  @BeforeAll
  static void registerStepTypes() {
    BuiltinQuestSteps.register();
  }

  private static void installQuest(String stepType, String extraFields) {
    String suffix = extraFields.isEmpty() ? "" : ", " + extraFields;
    RawQuestStep step =
        new RawQuestStep(
            "step",
            new ResourceLocation(stepType),
            JsonParser.parseString("{\"type\":\"" + stepType + "\"" + suffix + "}")
                .getAsJsonObject());
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
                    Map.of("step", step),
                    CompletionPolicy.ALL_STEPS,
                    true),
                ActionList.EMPTY,
                RewardSection.EMPTY)));
  }

  @AfterEach
  void clearRegistry() {
    QuestContentRegistry.clear();
  }

  @Test
  void startWithoutResolvablePlayerSkipsInventorySnapshot() {
    installQuest("dqse:collect_item", "\"item\": \"minecraft:oak_log\", \"count\": 5");
    PlayerState playerState = new PlayerState(UUID.randomUUID());

    QuestService.startQuest(ActionContext.ofTest(playerState), QUEST_ID);

    assertNull(
        playerState.getFact(FactScope.PLAYER, CollectItemStepType.baselineKey(QUEST_ID, "step")));
  }

  @Test
  void questCompletionRemovesBaselineFact() {
    installQuest("dqse:collect_item", "\"item\": \"minecraft:oak_log\", \"count\": 5");
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);
    String baselineKey = CollectItemStepType.baselineKey(QUEST_ID, "step");
    playerState.setFact(FactScope.PLAYER, baselineKey, FactValue.of(3L));

    QuestService.completeQuest(context, QUEST_ID);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
    assertFalse(playerState.hasFact(FactScope.PLAYER, baselineKey));
  }

  @Test
  void autoCompletionAlsoRemovesBaselineFact() {
    installQuest("dqse:collect_item", "\"item\": \"minecraft:oak_log\", \"count\": 5");
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);
    QuestService.startQuest(context, QUEST_ID);
    String baselineKey = CollectItemStepType.baselineKey(QUEST_ID, "step");
    playerState.setFact(FactScope.PLAYER, baselineKey, FactValue.of(3L));

    QuestService.setStepProgress(context, QUEST_ID, "step", 5);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
    assertFalse(playerState.hasFact(FactScope.PLAYER, baselineKey));
  }

  @Test
  void unknownStepTypeDoesNotBreakLifecycle() {
    installQuest("other_mod:custom_step", "");
    PlayerState playerState = new PlayerState(UUID.randomUUID());
    ActionContext context = ActionContext.ofTest(playerState);

    QuestService.startQuest(context, QUEST_ID);
    QuestService.progressStep(context, QUEST_ID, "step", 1);

    assertTrue(playerState.getQuest(QUEST_ID).steps().get("step").complete());
    assertEquals(QuestState.COMPLETED, playerState.getQuest(QUEST_ID).state());
  }
}
