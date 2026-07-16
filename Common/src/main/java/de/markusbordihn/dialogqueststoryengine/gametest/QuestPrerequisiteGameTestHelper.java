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

import com.google.gson.JsonObject;
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
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgressSync;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestPrerequisiteGameTestHelper {

  private static final ResourceLocation MANUAL = new ResourceLocation("dqse", "manual");
  private static final ResourceLocation CHAIN = new ResourceLocation("test", "chain_quest");
  private static final ResourceLocation ANY = new ResourceLocation("test", "any_quest");

  private QuestPrerequisiteGameTestHelper() {}

  public static void dependentStepActivatesInDelta(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(testQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      network.clear();

      QuestService.startQuest(player, CHAIN);
      QuestDeltaPacket startDelta = network.last(QuestDeltaPacket.class);
      GameTestHelpers.assertEquals(
          helper,
          "step_a starts ACTIVE",
          StepState.ACTIVE,
          startDelta.changedSteps().get("step_a").state());
      GameTestHelpers.assertEquals(
          helper,
          "step_b starts LOCKED",
          StepState.LOCKED,
          startDelta.changedSteps().get("step_b").state());

      network.clear();
      QuestService.progressStep(playerUuid, CHAIN, "step_a", 1);
      QuestDeltaPacket delta = network.last(QuestDeltaPacket.class);
      GameTestHelpers.assertEquals(
          helper,
          "step_a completes",
          StepState.COMPLETED,
          delta.changedSteps().get("step_a").state());
      GameTestHelpers.assertEquals(
          helper,
          "dependent step_b activates in the same delta",
          StepState.ACTIVE,
          delta.changedSteps().get("step_b").state());
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  public static void anyStepSkipsRemainingInDelta(GameTestHelper helper) {
    ServerPlayer player = GameTestHelpers.mockConnectedServerPlayer(helper);
    UUID playerUuid = player.getUUID();
    CapturingNetworkTestHandler network = installEnv();
    Map<ResourceLocation, QuestDefinition> previous =
        QuestRegistryTestSupport.install(testQuests());
    try {
      PlayerStateService.onPlayerDataLoaded(playerUuid, new CompoundTag());
      QuestService.startQuest(player, ANY);
      network.clear();

      QuestService.progressStep(playerUuid, ANY, "step_x", 1);
      QuestDeltaPacket delta = network.last(QuestDeltaPacket.class);
      GameTestHelpers.assertEquals(
          helper,
          "completed step is COMPLETED",
          StepState.COMPLETED,
          delta.changedSteps().get("step_x").state());
      GameTestHelpers.assertEquals(
          helper,
          "remaining step is SKIPPED",
          StepState.SKIPPED,
          delta.changedSteps().get("step_y").state());
    } finally {
      teardownEnv(playerUuid, network, previous);
    }
  }

  private static CapturingNetworkTestHandler installEnv() {
    CapturingNetworkTestHandler network = CapturingNetworkTestHandler.install();
    PlayerStateEvents.clearAll();
    QuestProgressSync.register();
    return network;
  }

  private static void teardownEnv(
      UUID playerUuid,
      CapturingNetworkTestHandler network,
      Map<ResourceLocation, QuestDefinition> previous) {
    QuestRegistryTestSupport.restore(previous);
    network.restore();
    PlayerStateService.onPlayerLoggedOut(playerUuid);
    PlayerStateEvents.clearAll();
  }

  private static Map<ResourceLocation, QuestDefinition> testQuests() {
    Map<ResourceLocation, QuestDefinition> quests = new LinkedHashMap<>();
    quests.put(
        CHAIN,
        quest(CHAIN, steps(step("step_a"), step("step_b", "step_a")), CompletionPolicy.ALL_STEPS));
    quests.put(ANY, quest(ANY, steps(step("step_x"), step("step_y")), CompletionPolicy.ANY_STEP));
    return quests;
  }

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
      ResourceLocation id, Map<String, RawQuestStep> steps, CompletionPolicy policy) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(Optional.empty(), QuestPrerequisites.NONE, steps, policy, true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }
}
