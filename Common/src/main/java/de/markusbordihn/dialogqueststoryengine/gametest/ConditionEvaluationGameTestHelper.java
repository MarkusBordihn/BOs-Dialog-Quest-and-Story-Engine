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

package de.markusbordihn.dialogqueststoryengine.gametest;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import de.markusbordihn.dialogqueststoryengine.logic.condition.GroupOperator;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.QuestStateCondition;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.List;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class ConditionEvaluationGameTestHelper {

  private static final ResourceLocation TEST_QUEST =
      new ResourceLocation("dqse_example", "first_quest");

  private ConditionEvaluationGameTestHelper() {}

  public static void testFactEqualsGatesDialogChoiceByPlayerState(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();

      Condition condition =
          new FactEqualsCondition(FactScope.PLAYER, "quest_unlocked", FactValue.of(true));
      ConditionContext contextBefore = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper, "Choice should be hidden before fact is set", !condition.evaluate(contextBefore));

      PlayerStateService.setFact(
          playerUuid, FactScope.PLAYER, "quest_unlocked", FactValue.of(true));
      ConditionContext contextAfter = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper, "Choice should be visible after fact is set", condition.evaluate(contextAfter));
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testQuestStateConditionInLiveContext(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();

      Condition condition = new QuestStateCondition(TEST_QUEST, QuestState.ACTIVE);
      ConditionContext contextNotStarted = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "Quest state condition should be false before quest started",
          !condition.evaluate(contextNotStarted));

      PlayerStateService.startQuest(playerUuid, TEST_QUEST);
      ConditionContext contextActive = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "Quest state condition should be true when quest is ACTIVE",
          condition.evaluate(contextActive));
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testAllGroupRequiresBothConditionsTrue(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "a", FactValue.of(1L));

      Condition conditionA = new FactEqualsCondition(FactScope.PLAYER, "a", FactValue.of(1L));
      Condition conditionB = new FactEqualsCondition(FactScope.PLAYER, "b", FactValue.of(2L));
      ConditionGroup allGroup =
          new ConditionGroup(GroupOperator.ALL, List.of(conditionA, conditionB));
      ConditionContext context = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "ALL group should be false when only one condition is true",
          !allGroup.evaluate(context));

      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "b", FactValue.of(2L));
      ConditionContext contextBothSet = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "ALL group should be true when both conditions are true",
          allGroup.evaluate(contextBothSet));
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testAnyGroupTrueWhenOnlyOneConditionIsTrue(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "unlocked", FactValue.of(true));

      Condition conditionTrue =
          new FactEqualsCondition(FactScope.PLAYER, "unlocked", FactValue.of(true));
      Condition conditionFalse =
          new FactEqualsCondition(FactScope.PLAYER, "missing", FactValue.of(true));
      ConditionGroup anyGroup =
          new ConditionGroup(GroupOperator.ANY, List.of(conditionTrue, conditionFalse));
      ConditionContext context = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "ANY group should be true when at least one condition is true",
          anyGroup.evaluate(context));
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testNestedGroupsTwoLevelsDeep(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "f1", FactValue.of(1L));
      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "f2", FactValue.of(2L));

      ConditionGroup inner1 =
          new ConditionGroup(
              GroupOperator.ALL,
              List.of(
                  new FactEqualsCondition(FactScope.PLAYER, "f1", FactValue.of(1L)),
                  new FactEqualsCondition(FactScope.PLAYER, "f2", FactValue.of(2L))));
      ConditionGroup inner2 =
          new ConditionGroup(
              GroupOperator.ALL,
              List.of(
                  new FactEqualsCondition(FactScope.PLAYER, "f1", FactValue.of(1L)),
                  new FactEqualsCondition(FactScope.PLAYER, "missing", FactValue.of(99L))));
      ConditionGroup outer = new ConditionGroup(GroupOperator.ANY, List.of(inner1, inner2));
      ConditionContext context = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "Nested ANY[ALL, ALL] should evaluate correctly two levels deep",
          outer.evaluate(context));
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testUnknownConditionTypeNeverCrashes(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();
      ConditionContext context = ConditionContext.ofTest(playerState);

      GameTestHelpers.assertTrue(
          helper,
          "Condition.NEVER should evaluate to false without throwing",
          !Condition.NEVER.evaluate(context));
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }
}
