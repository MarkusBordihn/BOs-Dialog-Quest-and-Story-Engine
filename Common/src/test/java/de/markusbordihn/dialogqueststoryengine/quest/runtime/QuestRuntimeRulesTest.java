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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestTestFixtures;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestAvailability;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepState;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.CompletionPolicy;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.LogicSection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.PrerequisiteMode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestPrerequisites;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import java.util.ArrayList;
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

class QuestRuntimeRulesTest {

  private static final UUID PLAYER = UUID.fromString("55555555-5555-5555-5555-555555555555");
  private static final ResourceLocation MANUAL = new ResourceLocation("dqse", "manual");
  private static final ResourceLocation CHAIN = new ResourceLocation("test", "chain");
  private static final ResourceLocation ANY = new ResourceLocation("test", "any_step_quest");
  private static final ResourceLocation GATE_A = new ResourceLocation("test", "gate_a");
  private static final ResourceLocation GATE_B = new ResourceLocation("test", "gate_b");
  private static final ResourceLocation GATE_ANY = new ResourceLocation("test", "gate_any");
  private static final ResourceLocation RESTART_FALSE =
      new ResourceLocation("test", "restart_false");
  private static final ResourceLocation RESTART_TRUE = new ResourceLocation("test", "restart_true");
  private final List<QuestChangeResult> changes = new ArrayList<>();
  private PlayerState playerState;

  private static RawQuestStep step(String id, String... requires) {
    return new RawQuestStep(id, MANUAL, Optional.empty(), List.of(requires), new JsonObject());
  }

  private static Map<String, RawQuestStep> steps(RawQuestStep... steps) {
    Map<String, RawQuestStep> map = new LinkedHashMap<>();
    for (RawQuestStep step : steps) {
      map.put(step.id(), step);
    }
    return map;
  }

  private static QuestDefinition quest(
      ResourceLocation id,
      Map<String, RawQuestStep> steps,
      CompletionPolicy policy,
      QuestPrerequisites prerequisites,
      boolean restartAfterFailure) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(Optional.empty(), prerequisites, steps, policy, restartAfterFailure),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }

  @BeforeEach
  void setUp() {
    Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    quests.put(
        CHAIN,
        quest(
            CHAIN,
            steps(step("step_a"), step("step_b", "step_a")),
            CompletionPolicy.ALL_STEPS,
            QuestPrerequisites.NONE,
            true));
    quests.put(
        ANY,
        quest(
            ANY,
            steps(step("step_x"), step("step_y")),
            CompletionPolicy.ANY_STEP,
            QuestPrerequisites.NONE,
            true));
    quests.put(
        GATE_A, quest(GATE_A, Map.of(), CompletionPolicy.ALL_STEPS, QuestPrerequisites.NONE, true));
    quests.put(
        GATE_B,
        quest(
            GATE_B,
            Map.of(),
            CompletionPolicy.ALL_STEPS,
            new QuestPrerequisites(PrerequisiteMode.ALL, List.of(GATE_A)),
            true));
    quests.put(
        GATE_ANY,
        quest(
            GATE_ANY,
            Map.of(),
            CompletionPolicy.ALL_STEPS,
            new QuestPrerequisites(
                PrerequisiteMode.ANY, List.of(GATE_A, new ResourceLocation("test", "missing"))),
            true));
    quests.put(
        RESTART_FALSE,
        quest(RESTART_FALSE, Map.of(), CompletionPolicy.ALL_STEPS, QuestPrerequisites.NONE, false));
    quests.put(
        RESTART_TRUE,
        quest(
            RESTART_TRUE,
            steps(step("only")),
            CompletionPolicy.ALL_STEPS,
            QuestPrerequisites.NONE,
            true));
    QuestTestFixtures.installDefinitions(quests);

    PlayerStateService.onPlayerDataLoaded(PLAYER, new CompoundTag());
    playerState = PlayerStateService.get(PLAYER).orElseThrow();
    PlayerStateEvents.addQuestChangedListener((uuid, change) -> changes.add(change));
  }

  @AfterEach
  void tearDown() {
    PlayerStateService.markPlayerDataSaved(PLAYER);
    PlayerStateService.onPlayerLoggedOut(PLAYER);
    PlayerStateEvents.clearAll();
    QuestTestFixtures.clear();
    changes.clear();
  }

  @Test
  void stepWithRequiresStartsLocked() {
    QuestService.startQuest(PLAYER, CHAIN);

    QuestProgress quest = playerState.getQuest(CHAIN);
    assertEquals(StepState.ACTIVE, quest.steps().get("step_a").state());
    assertEquals(StepState.LOCKED, quest.steps().get("step_b").state());
  }

  @Test
  void dependentActivatesWhenRequirementCompletesInOneDelta() {
    QuestService.startQuest(PLAYER, CHAIN);
    changes.clear();

    QuestService.progressStep(PLAYER, CHAIN, "step_a", 1);

    QuestProgress quest = playerState.getQuest(CHAIN);
    assertEquals(StepState.COMPLETED, quest.steps().get("step_a").state());
    assertEquals(StepState.ACTIVE, quest.steps().get("step_b").state());
    assertEquals(QuestState.ACTIVE, quest.state());

    assertEquals(1, changes.size());
    Map<String, StepProgress> delta = changes.get(0).changedSteps();
    assertTrue(delta.containsKey("step_a"), "delta should include the completed step");
    assertTrue(delta.containsKey("step_b"), "delta should include the newly activated dependent");
  }

  @Test
  void chainCompletesAfterAllStepsDone() {
    QuestService.startQuest(PLAYER, CHAIN);
    QuestService.progressStep(PLAYER, CHAIN, "step_a", 1);
    QuestService.progressStep(PLAYER, CHAIN, "step_b", 1);

    assertEquals(QuestState.COMPLETED, playerState.getQuest(CHAIN).state());
  }

  @Test
  void anyStepCompletionSkipsRemainingSteps() {
    QuestService.startQuest(PLAYER, ANY);
    changes.clear();

    QuestService.progressStep(PLAYER, ANY, "step_x", 1);

    QuestProgress quest = playerState.getQuest(ANY);
    assertEquals(QuestState.COMPLETED, quest.state());
    assertEquals(StepState.COMPLETED, quest.steps().get("step_x").state());
    assertEquals(StepState.SKIPPED, quest.steps().get("step_y").state());
    assertTrue(
        changes.get(0).changedSteps().containsKey("step_y"), "SKIPPED step must be in delta");
  }

  @Test
  void startRejectedWhenPrerequisiteUnmet() {
    Optional<QuestChangeResult> result = QuestService.startQuest(PLAYER, GATE_B);

    assertTrue(result.isEmpty());
    assertFalse(playerState.hasQuest(GATE_B));
  }

  @Test
  void startAllowedAfterPrerequisiteCompleted() {
    QuestService.startQuest(PLAYER, GATE_A);
    QuestService.completeQuest(PLAYER, GATE_A);

    Optional<QuestChangeResult> result = QuestService.startQuest(PLAYER, GATE_B);

    assertTrue(result.isPresent());
    assertEquals(QuestState.ACTIVE, playerState.getQuest(GATE_B).state());
  }

  @Test
  void anyModePrerequisiteSatisfiedByOneCompletion() {
    QuestService.startQuest(PLAYER, GATE_A);
    QuestService.completeQuest(PLAYER, GATE_A);

    assertTrue(QuestService.startQuest(PLAYER, GATE_ANY).isPresent());
  }

  @Test
  void restartAfterFailureFalseRejectsRestartButForceBypasses() {
    QuestService.startQuest(PLAYER, RESTART_FALSE);
    QuestService.failQuest(PLAYER, RESTART_FALSE);

    assertTrue(QuestService.startQuest(PLAYER, RESTART_FALSE).isEmpty());
    assertEquals(QuestState.FAILED, playerState.getQuest(RESTART_FALSE).state());

    assertTrue(QuestService.forceStartQuest(PLAYER, RESTART_FALSE).isPresent());
    assertEquals(QuestState.ACTIVE, playerState.getQuest(RESTART_FALSE).state());
  }

  @Test
  void restartAfterFailureTrueDiscardsPreviousProgress() {
    QuestService.startQuest(PLAYER, RESTART_TRUE);
    QuestService.progressStep(PLAYER, RESTART_TRUE, "only", 1);
    assertEquals(QuestState.COMPLETED, playerState.getQuest(RESTART_TRUE).state());

    playerState.putQuestDirect(RESTART_TRUE, new QuestProgress(QuestState.FAILED));

    assertTrue(QuestService.startQuest(PLAYER, RESTART_TRUE).isPresent());
    QuestProgress restarted = playerState.getQuest(RESTART_TRUE);
    assertEquals(QuestState.ACTIVE, restarted.state());
    assertEquals(0, restarted.steps().get("only").progress());
  }

  @Test
  void availabilityDerivesLockedThenAvailable() {
    assertEquals(
        QuestAvailability.LOCKED, QuestAvailabilityService.availabilityFor(playerState, GATE_B));

    QuestService.startQuest(PLAYER, GATE_A);
    QuestService.completeQuest(PLAYER, GATE_A);

    assertEquals(
        QuestAvailability.AVAILABLE, QuestAvailabilityService.availabilityFor(playerState, GATE_B));
    assertEquals(
        QuestAvailability.COMPLETED, QuestAvailabilityService.availabilityFor(playerState, GATE_A));
  }
}
