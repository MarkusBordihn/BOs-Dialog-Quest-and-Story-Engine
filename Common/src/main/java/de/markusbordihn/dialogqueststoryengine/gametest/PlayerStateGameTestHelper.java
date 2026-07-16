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

import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import de.markusbordihn.dialogqueststoryengine.state.StepState;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public class PlayerStateGameTestHelper {

  private PlayerStateGameTestHelper() {}

  public static void testPlayerStateNotLoadedBeforeLogin(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();

    GameTestHelpers.assertTrue(
        helper,
        "PlayerStateService should not have state before login",
        PlayerStateService.get(playerUuid).isEmpty());
  }

  public static void testPlayerStateLoadedAfterLogin(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      GameTestHelpers.assertTrue(
          helper,
          "PlayerStateService should have state after loading",
          PlayerStateService.isLoaded(playerUuid));

      PlayerState playerState = PlayerStateService.get(playerUuid).orElse(null);
      GameTestHelpers.assertNotNull(helper, "PlayerState should not be null", playerState);
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testFactSetAndGet(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "coins", FactValue.of(42L));

      FactValue result =
          PlayerStateService.getFact(playerUuid, FactScope.PLAYER, "coins").orElse(null);

      GameTestHelpers.assertNotNull(helper, "Fact should be present after set", result);
      GameTestHelpers.assertEquals(
          helper, "Fact value should be 42", 42L, ((FactValue.LongValue) result).value());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testQuestStartedAndActive(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    ResourceLocation questId = new ResourceLocation("dqse_example", "first_quest");
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      QuestProgress questProgress = PlayerStateService.startQuest(playerUuid, questId).orElse(null);

      GameTestHelpers.assertNotNull(helper, "QuestProgress should not be null", questProgress);
      GameTestHelpers.assertEquals(
          helper, "Quest should be ACTIVE", QuestState.ACTIVE, questProgress.state());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testQuestStepProgressed(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    ResourceLocation questId = new ResourceLocation("test", "step_progress");
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();

      QuestProgress questProgress = playerState.getOrCreateQuest(questId, QuestState.ACTIVE);
      questProgress.putStep("kill_wolves", StepProgress.active(5));

      StepProgress result =
          PlayerStateService.progressStep(playerUuid, questId, "kill_wolves", 3).orElse(null);

      GameTestHelpers.assertNotNull(helper, "StepProgress should not be null", result);
      GameTestHelpers.assertEquals(helper, "Step progress should be 3", 3, result.progress());
      GameTestHelpers.assertEquals(
          helper, "Step should still be ACTIVE", StepState.ACTIVE, result.state());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testStepCompletesWhenRequiredReached(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    ResourceLocation questId = new ResourceLocation("test", "step_complete");
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();

      QuestProgress questProgress = playerState.getOrCreateQuest(questId, QuestState.ACTIVE);
      questProgress.putStep("find_item", StepProgress.active(1));

      StepProgress result =
          PlayerStateService.progressStep(playerUuid, questId, "find_item", 1).orElse(null);

      GameTestHelpers.assertNotNull(helper, "StepProgress should not be null", result);
      GameTestHelpers.assertTrue(helper, "Step should be complete", result.complete());
      GameTestHelpers.assertEquals(
          helper, "Step state should be COMPLETED", StepState.COMPLETED, result.state());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testPlayerStateEventFired(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    UUID[] capturedUuid = {null};
    try {
      PlayerStateEvents.addPlayerStateLoadedListener((uuid, state) -> capturedUuid[0] = uuid);

      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());

      GameTestHelpers.assertEquals(
          helper,
          "PlayerStateLoaded event should fire with the correct UUID",
          playerUuid,
          capturedUuid[0]);
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }

  public static void testPlayerStateDirtyAfterMutation(GameTestHelper helper) {
    UUID playerUuid = UUID.randomUUID();
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      PlayerState playerState = PlayerStateService.get(playerUuid).get();

      GameTestHelpers.assertTrue(
          helper, "State should not be dirty after load", !playerState.isDirty());

      PlayerStateService.setFact(playerUuid, FactScope.PLAYER, "key", FactValue.of(true));

      GameTestHelpers.assertTrue(
          helper, "State should be dirty after mutation", playerState.isDirty());
    } finally {
      PlayerStateService.onPlayerLoggedOut(playerUuid);
      PlayerStateEvents.clearAll();
    }
  }
}
