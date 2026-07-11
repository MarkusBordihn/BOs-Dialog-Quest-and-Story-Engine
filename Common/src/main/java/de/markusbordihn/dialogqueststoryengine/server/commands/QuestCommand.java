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

package de.markusbordihn.dialogqueststoryengine.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import de.markusbordihn.dialogqueststoryengine.commands.Command;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgress;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import java.util.Map;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class QuestCommand extends Command {

  private QuestCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("quest")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(
            Commands.literal("start")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .executes(
                            context ->
                                executeStart(
                                    context.getSource(),
                                    ResourceLocationArgument.getId(context, "id"),
                                    context.getSource().getPlayerOrException()))
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes(
                                    context ->
                                        executeStart(
                                            context.getSource(),
                                            ResourceLocationArgument.getId(context, "id"),
                                            EntityArgument.getPlayer(context, "target"))))))
        .then(
            Commands.literal("complete")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .executes(
                            context ->
                                executeComplete(
                                    context.getSource(),
                                    ResourceLocationArgument.getId(context, "id"),
                                    context.getSource().getPlayerOrException()))
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes(
                                    context ->
                                        executeComplete(
                                            context.getSource(),
                                            ResourceLocationArgument.getId(context, "id"),
                                            EntityArgument.getPlayer(context, "target"))))))
        .then(
            Commands.literal("state")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .executes(
                            context ->
                                executeState(
                                    context.getSource(),
                                    ResourceLocationArgument.getId(context, "id"),
                                    context.getSource().getPlayerOrException()))
                        .then(
                            Commands.argument("target", EntityArgument.player())
                                .executes(
                                    context ->
                                        executeState(
                                            context.getSource(),
                                            ResourceLocationArgument.getId(context, "id"),
                                            EntityArgument.getPlayer(context, "target"))))));
  }

  private static int executeStart(
      CommandSourceStack source, ResourceLocation questId, ServerPlayer target) {
    return QuestService.startQuest(target, questId)
        .map(
            result -> {
              sendSuccessMessage(
                  source,
                  "quest start: "
                      + questId
                      + " for "
                      + target.getGameProfile().getName()
                      + " -> "
                      + result.questProgress().state());
              return 1;
            })
        .orElseGet(
            () -> {
              sendFailureMessage(
                  source,
                  PlayerStateService.isLoaded(target.getUUID())
                      ? "quest start: quest definition is not loaded."
                      : "quest start: player state not loaded.");
              return 0;
            });
  }

  private static int executeComplete(
      CommandSourceStack source, ResourceLocation questId, ServerPlayer target) {
    return QuestService.completeQuest(target, questId)
        .map(
            result -> {
              sendSuccessMessage(
                  source,
                  "quest complete: "
                      + questId
                      + " for "
                      + target.getGameProfile().getName()
                      + " -> "
                      + result.questProgress().state());
              return 1;
            })
        .orElseGet(
            () -> {
              sendFailureMessage(source, "quest complete: quest not found in player state.");
              return 0;
            });
  }

  private static int executeState(
      CommandSourceStack source, ResourceLocation questId, ServerPlayer target) {
    return PlayerStateService.get(target.getUUID())
        .map(playerState -> playerState.getQuest(questId))
        .map(
            questProgress -> {
              sendInfoMessage(
                  source,
                  "quest state: "
                      + questId
                      + " for "
                      + target.getGameProfile().getName()
                      + " -> "
                      + formatQuestProgress(questProgress));
              return 1;
            })
        .orElseGet(
            () -> {
              sendInfoMessage(
                  source,
                  "quest state: "
                      + questId
                      + " for "
                      + target.getGameProfile().getName()
                      + " -> NOT_STARTED");
              return 1;
            });
  }

  private static String formatQuestProgress(QuestProgress questProgress) {
    StringBuilder builder =
        new StringBuilder(questProgress.state().name())
            .append(" rev=")
            .append(questProgress.revision());
    for (Map.Entry<String, StepProgress> entry : questProgress.steps().entrySet()) {
      StepProgress stepProgress = entry.getValue();
      builder
          .append(" | ")
          .append(entry.getKey())
          .append("=")
          .append(stepProgress.state())
          .append(" ")
          .append(stepProgress.progress())
          .append("/")
          .append(stepProgress.required());
    }
    return builder.toString();
  }
}
