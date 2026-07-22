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

package de.markusbordihn.dialogqueststoryengine.quest.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestChainScenarioTest {

  private static final UUID PLAYER = UUID.randomUUID();
  private static final ResourceLocation QUEST = new ResourceLocation("test", "obedience_chain");
  private static final ResourceLocation MANUAL = new ResourceLocation("dqse", "manual");

  private PlayerState playerState;

  private static RawQuestStep step(String id, String... requires) {
    return new RawQuestStep(id, MANUAL, Optional.empty(), List.of(requires), new JsonObject());
  }

  @BeforeEach
  void setUp() {
    Map<String, RawQuestStep> steps = new LinkedHashMap<>();
    for (RawQuestStep step :
        new RawQuestStep[] {
          step("break_five"),
          step("return_one", "break_five"),
          step("break_ten", "return_one"),
          step("return_two", "break_ten"),
          step("obey", "return_two")
        }) {
      steps.put(step.id(), step);
    }

    QuestDefinition definition =
        new QuestDefinition(
            QUEST,
            1,
            NarrativeMetadata.EMPTY,
            new DisplaySection(
                "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
            new LogicSection(
                Optional.empty(), QuestPrerequisites.NONE, steps, CompletionPolicy.ALL_STEPS, true),
            ActionList.EMPTY,
            RewardSection.EMPTY);
    QuestTestFixtures.installDefinitions(Map.of(QUEST, definition));

    PlayerStateService.onPlayerDataLoaded(PLAYER, new CompoundTag());
    this.playerState = PlayerStateService.get(PLAYER).orElseThrow();
  }

  @AfterEach
  void tearDown() {
    PlayerStateService.markPlayerDataSaved(PLAYER);
    PlayerStateService.onPlayerLoggedOut(PLAYER);
    PlayerStateEvents.clearAll();
    QuestTestFixtures.clear();
  }

  @Test
  void onlyTheFirstStageIsActiveAtStart() {
    QuestService.startQuest(PLAYER, QUEST);

    QuestProgress quest = this.playerState.getQuest(QUEST);
    assertEquals(StepState.ACTIVE, quest.steps().get("break_five").state());
    assertEquals(StepState.LOCKED, quest.steps().get("return_one").state());
    assertEquals(StepState.LOCKED, quest.steps().get("break_ten").state());
    assertEquals(StepState.LOCKED, quest.steps().get("return_two").state());
    assertEquals(StepState.LOCKED, quest.steps().get("obey").state());
  }

  @Test
  void progressOnALockedStageIsIgnored() {
    QuestService.startQuest(PLAYER, QUEST);

    QuestService.progressStep(PLAYER, QUEST, "obey", 1);

    QuestProgress quest = this.playerState.getQuest(QUEST);
    assertEquals(StepState.LOCKED, quest.steps().get("obey").state());
    assertEquals(QuestState.ACTIVE, quest.state());
  }

  @Test
  void stagesUnlockInOrderAndQuestCompletesOnlyAtTheEnd() {
    QuestService.startQuest(PLAYER, QUEST);

    QuestService.progressStep(PLAYER, QUEST, "break_five", 1);
    assertEquals(StepState.ACTIVE, this.stage("return_one"));

    QuestService.progressStep(PLAYER, QUEST, "return_one", 1);
    assertEquals(StepState.ACTIVE, this.stage("break_ten"));

    QuestService.progressStep(PLAYER, QUEST, "break_ten", 1);
    assertEquals(StepState.ACTIVE, this.stage("return_two"));

    QuestService.progressStep(PLAYER, QUEST, "return_two", 1);
    assertEquals(StepState.ACTIVE, this.stage("obey"));
    assertEquals(
        QuestState.ACTIVE,
        this.playerState.getQuest(QUEST).state(),
        "quest must not complete before the final stage");

    QuestService.progressStep(PLAYER, QUEST, "obey", 1);
    assertEquals(QuestState.COMPLETED, this.playerState.getQuest(QUEST).state());
  }

  private StepState stage(String id) {
    return this.playerState.getQuest(QUEST).steps().get(id).state();
  }
}
