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
import de.markusbordihn.dialogqueststoryengine.commands.suggestion.ContentIdSuggestions;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionSavedData;
import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.UUID;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;

public class HolopadCommand extends Command {

  private HolopadCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("holopad")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(buildSetSubCommand())
        .then(buildClearSubCommand())
        .then(buildInfoSubCommand());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildSetSubCommand() {
    return Commands.literal("set")
        .then(
            Commands.literal("story")
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("storyId", ResourceLocationArgument.id())
                                .suggests(ContentIdSuggestions.STORY_ENTRIES)
                                .executes(
                                    context -> {
                                      BlockPos blockPosition =
                                          BlockPosArgument.getLoadedBlockPos(context, "pos");
                                      ResourceLocation storyId =
                                          ResourceLocationArgument.getId(context, "storyId");
                                      return executeSetStory(
                                          context.getSource(), blockPosition, storyId, null);
                                    })
                                .then(
                                    Commands.argument("themeId", ResourceLocationArgument.id())
                                        .suggests(ContentIdSuggestions.THEMES)
                                        .executes(
                                            context -> {
                                              BlockPos blockPosition =
                                                  BlockPosArgument.getLoadedBlockPos(
                                                      context, "pos");
                                              ResourceLocation storyId =
                                                  ResourceLocationArgument.getId(
                                                      context, "storyId");
                                              ResourceLocation themeId =
                                                  ResourceLocationArgument.getId(
                                                      context, "themeId");
                                              return executeSetStory(
                                                  context.getSource(),
                                                  blockPosition,
                                                  storyId,
                                                  themeId);
                                            })))))
        .then(
            Commands.literal("interactive")
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("storyId", ResourceLocationArgument.id())
                                .suggests(ContentIdSuggestions.STORY_ENTRIES)
                                .executes(
                                    context -> {
                                      BlockPos blockPosition =
                                          BlockPosArgument.getLoadedBlockPos(context, "pos");
                                      ResourceLocation storyId =
                                          ResourceLocationArgument.getId(context, "storyId");
                                      return executeSetInteractive(
                                          context.getSource(), blockPosition, storyId);
                                    }))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildClearSubCommand() {
    return Commands.literal("clear")
        .then(
            Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(
                    context -> {
                      BlockPos blockPosition = BlockPosArgument.getLoadedBlockPos(context, "pos");
                      return executeClear(context.getSource(), blockPosition);
                    }));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildInfoSubCommand() {
    return Commands.literal("info")
        .then(
            Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(
                    context -> {
                      BlockPos blockPosition = BlockPosArgument.getLoadedBlockPos(context, "pos");
                      return executeInfo(context.getSource(), blockPosition);
                    }));
  }

  private static int executeSetStory(
      CommandSourceStack source,
      BlockPos blockPosition,
      ResourceLocation storyId,
      ResourceLocation themeId) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);

    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.openStory(storyId, themeId));

    registerOrUpdate(source, blockId, blockPosition, level, actionDataSet);
    sendSuccessMessage(
        source,
        "Holopad at "
            + blockPosition.toShortString()
            + " configured with story: "
            + storyId
            + (themeId != null ? " (theme: " + themeId + ")" : ""));
    return 1;
  }

  private static int executeSetInteractive(
      CommandSourceStack source, BlockPos blockPosition, ResourceLocation storyId) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);

    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.openInteractiveStory(storyId));

    registerOrUpdate(source, blockId, blockPosition, level, actionDataSet);
    sendSuccessMessage(
        source,
        "Holopad at "
            + blockPosition.toShortString()
            + " configured with interactive story: "
            + storyId);
    return 1;
  }

  private static int executeClear(CommandSourceStack source, BlockPos blockPosition) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);
    InteractionSavedData data = InteractionSavedData.get(source.getServer());

    boolean removed = data.unregister(blockId, InteractionEventType.ON_HOLOPAD_USE);
    if (removed) {
      data.setDirty();
      sendSuccessMessage(source, "Holopad interaction cleared at " + blockPosition.toShortString());
    } else {
      sendInfoMessage(source, "No holopad interaction found at " + blockPosition.toShortString());
    }
    return 1;
  }

  private static int executeInfo(CommandSourceStack source, BlockPos blockPosition) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);
    InteractionSavedData data = InteractionSavedData.get(source.getServer());

    InteractionEntry entry = data.getInteraction(blockId, InteractionEventType.ON_HOLOPAD_USE);
    if (entry == null) {
      sendInfoMessage(
          source, "No holopad interaction registered at " + blockPosition.toShortString());
    } else {
      sendInfoMessage(source, "Holopad at " + blockPosition.toShortString() + ": " + entry);
    }
    return 1;
  }

  private static void registerOrUpdate(
      CommandSourceStack source,
      UUID blockId,
      BlockPos blockPosition,
      ServerLevel level,
      ActionDataSet actionDataSet) {
    InteractionSavedData data = InteractionSavedData.get(source.getServer());

    InteractionEntry existing = data.getInteraction(blockId, InteractionEventType.ON_HOLOPAD_USE);
    if (existing != null) {
      data.unregister(blockId, InteractionEventType.ON_HOLOPAD_USE);
    }

    ResourceLocation dimension = level.dimension().location();
    String label =
        actionDataSet.entries().stream()
            .findFirst()
            .map(entry -> entry.storyId() != null ? entry.storyId().toString() : "Holopad")
            .orElse("Holopad");

    data.register(
        InteractionEntry.forBlockInteract(
                blockId,
                blockPosition,
                TargetKind.BLOCK,
                InteractionType.OPEN_HOLOPAD,
                label,
                dimension)
            .withEdits(InteractionType.OPEN_HOLOPAD, label, actionDataSet));
    data.setDirty();
  }
}
