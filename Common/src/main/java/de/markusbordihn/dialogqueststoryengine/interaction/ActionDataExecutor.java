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

package de.markusbordihn.dialogqueststoryengine.interaction;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.config.SecurityConfig;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.AdvanceQuestStepAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.CompleteQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.FailQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveExperienceAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveItemAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.MarkStoryReadAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenDialogAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenStoryAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RemoveFactAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunCommandAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunFunctionAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.SendMessageAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.SetFactAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.StartQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.UnlockStoryAction;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionParser;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.story.OpenClientStoryPacket;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionDataExecutor {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ActionDataExecutor() {}

  public static void execute(
      ActionDataSet actionDataSet, ServerPlayer player, MinecraftServer server) {
    execute(actionDataSet, player, server, null);
  }

  public static void execute(
      ActionDataSet actionDataSet, ServerPlayer player, MinecraftServer server, UUID targetId) {
    if (actionDataSet.isEmpty()) {
      return;
    }

    PlayerState playerState = PlayerStateService.get(player.getUUID()).orElse(null);

    List<ActionDataEntry> ordered = new ArrayList<>(actionDataSet.entries());
    ordered.sort(Comparator.comparingInt(ActionDataEntry::priority).reversed());

    for (ActionDataEntry action : ordered) {
      if (!conditionAllows(action, player, playerState, server, targetId)) {
        continue;
      }
      executeAction(action, player, server, playerState, targetId);
    }
  }

  private static boolean conditionAllows(
      ActionDataEntry action,
      ServerPlayer player,
      PlayerState playerState,
      MinecraftServer server,
      UUID targetId) {
    if (action.condition().isEmpty()) {
      return true;
    }
    if (playerState == null) {
      fail(targetId, player, action.type(), "player state not loaded (condition)");
      return false;
    }
    try {
      JsonElement element = JsonParser.parseString(action.condition());
      Condition condition =
          ConditionParser.parse(
              element,
              ContentType.INTERACTION,
              new ResourceLocation(Constants.MOD_NAMESPACE, "interaction_action"),
              "interaction-action-" + action.id(),
              new ArrayList<>());
      return condition.evaluate(new ConditionContext(player, playerState, server));
    } catch (RuntimeException exception) {
      fail(targetId, player, action.type(), "invalid condition: " + exception.getMessage());
      return false;
    }
  }

  private static void executeAction(
      ActionDataEntry action,
      ServerPlayer player,
      MinecraftServer server,
      PlayerState playerState,
      UUID targetId) {
    if (action.type() == ActionType.OPEN_STORY) {
      ResourceLocation storyId = action.storyId();
      if (storyId == null) {
        fail(targetId, player, action.type(), "missing story id");
        return;
      }
      NetworkHandlerManager.sendToPlayer(
          player, new OpenClientStoryPacket(storyId, action.themeOverrideId()));
      return;
    }

    if (action.type() == ActionType.SEND_MESSAGE) {
      String message = action.message();
      if (message.isEmpty()) {
        fail(targetId, player, action.type(), "empty message");
        return;
      }
      Optional<String> speaker = resolveMessageSpeaker(action.speaker(), player, targetId);
      new SendMessageAction(message, speaker)
          .execute(
              new ActionContext(
                  player,
                  playerState,
                  server,
                  "interaction-action-" + action.id(),
                  Optional.empty()));
      return;
    }

    if (action.type() == ActionType.RUN_COMMAND || action.type() == ActionType.RUN_FUNCTION) {
      if (!SecurityConfig.isCommandActionsEnabled()) {
        fail(
            targetId,
            player,
            action.type(),
            "command actions are disabled (enable_command_actions=false)");
        return;
      }
      if (action.type() == ActionType.RUN_COMMAND
          && !SecurityConfig.isCommandAllowed(action.command())) {
        fail(targetId, player, action.type(), "command not whitelisted: " + action.command());
        return;
      }
    }

    Action canonical = toCanonicalAction(action);
    if (canonical == null) {
      fail(targetId, player, action.type(), "incomplete action (missing id or field)");
      return;
    }
    if (playerState == null) {
      fail(targetId, player, action.type(), "player state not loaded");
      return;
    }

    canonical.execute(
        new ActionContext(
            player, playerState, server, "interaction-action-" + action.id(), Optional.empty()));
  }

  private static void fail(UUID targetId, ServerPlayer player, ActionType type, String reason) {
    ActionDiagnostics.record(targetId, type, reason);
    log.warn(
        "{} ActionDataExecutor: {} skipped for {} - {}",
        Constants.LOG_PREFIX,
        type,
        player.getName().getString(),
        reason);
    if (player.hasPermissions(2)) {
      player.sendSystemMessage(
          Component.literal("⚠ " + type + ": " + reason).withStyle(ChatFormatting.YELLOW));
    }
  }

  private static Action toCanonicalAction(ActionDataEntry action) {
    return switch (action.type()) {
      case OPEN_INTERACTIVE_STORY -> mapResource(action.storyId(), OpenStoryAction::new);
      case OPEN_DIALOG -> mapResource(action.dialogId(), OpenDialogAction::new);
      case START_QUEST -> mapResource(action.questId(), StartQuestAction::new);
      case COMPLETE_QUEST -> mapResource(action.questId(), CompleteQuestAction::new);
      case FAIL_QUEST -> mapResource(action.questId(), FailQuestAction::new);
      case ADVANCE_QUEST_STEP -> {
        ResourceLocation questId = action.questId();
        String stepId = action.stepId();
        yield questId == null || stepId == null || stepId.isEmpty()
            ? null
            : new AdvanceQuestStepAction(questId, stepId, 1);
      }
      case UNLOCK_STORY -> mapResource(action.storyId(), UnlockStoryAction::new);
      case MARK_STORY_READ -> mapResource(action.storyId(), MarkStoryReadAction::new);
      case SET_FACT -> {
        ResourceLocation factId = action.factId();
        yield factId == null || action.factValue().isEmpty()
            ? null
            : new SetFactAction(
                FactScope.PLAYER, factId.toString(), FactValue.of(action.factValue()));
      }
      case REMOVE_FACT -> {
        ResourceLocation factId = action.factId();
        yield factId == null ? null : new RemoveFactAction(FactScope.PLAYER, factId.toString());
      }
      case GIVE_ITEM -> {
        ResourceLocation itemId = action.itemId();
        yield itemId == null ? null : new GiveItemAction(itemId, Math.max(1, action.count()));
      }
      case GIVE_EXPERIENCE ->
          action.amount() > 0 ? new GiveExperienceAction(action.amount()) : null;
      case RUN_COMMAND ->
          action.command().isEmpty()
              ? null
              : new RunCommandAction(
                  action.command(), SecurityConfig.getDefaultCommandPermissionLevel());
      case RUN_FUNCTION ->
          mapResource(
              action.functionId(),
              functionId ->
                  new RunFunctionAction(
                      functionId, SecurityConfig.getDefaultCommandPermissionLevel()));
      default -> null;
    };
  }

  private static Action mapResource(
      ResourceLocation value, Function<ResourceLocation, Action> factory) {
    return value == null ? null : factory.apply(value);
  }

  private static Optional<String> resolveMessageSpeaker(
      String rawSpeaker, ServerPlayer player, UUID targetId) {
    if (rawSpeaker == null || rawSpeaker.isBlank()) {
      return Optional.empty();
    }
    if (rawSpeaker.equals("@entity")) {
      if (targetId == null) {
        return Optional.empty();
      }
      Entity entity = player.serverLevel().getEntity(targetId);
      return entity != null && !(entity instanceof Player)
          ? Optional.of(entity.getName().getString())
          : Optional.empty();
    }
    return Optional.of(rawSpeaker);
  }
}
