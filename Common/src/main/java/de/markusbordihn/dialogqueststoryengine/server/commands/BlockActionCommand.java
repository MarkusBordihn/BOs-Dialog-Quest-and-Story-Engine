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

public class BlockActionCommand extends Command {

  private BlockActionCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("block")
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
                                    context ->
                                        executeSetStory(
                                            context.getSource(),
                                            BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                            ResourceLocationArgument.getId(context, "storyId"),
                                            null))
                                .then(
                                    Commands.argument("themeId", ResourceLocationArgument.id())
                                        .suggests(ContentIdSuggestions.THEMES)
                                        .executes(
                                            context ->
                                                executeSetStory(
                                                    context.getSource(),
                                                    BlockPosArgument.getLoadedBlockPos(
                                                        context, "pos"),
                                                    ResourceLocationArgument.getId(
                                                        context, "storyId"),
                                                    ResourceLocationArgument.getId(
                                                        context, "themeId")))))))
        .then(
            Commands.literal("interactive")
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("storyId", ResourceLocationArgument.id())
                                .suggests(ContentIdSuggestions.STORY_ENTRIES)
                                .executes(
                                    context ->
                                        executeSetInteractive(
                                            context.getSource(),
                                            BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                            ResourceLocationArgument.getId(context, "storyId"))))))
        .then(
            Commands.literal("dialog")
                .then(
                    Commands.argument("pos", BlockPosArgument.blockPos())
                        .then(
                            Commands.argument("dialogId", ResourceLocationArgument.id())
                                .suggests(ContentIdSuggestions.DIALOGS)
                                .executes(
                                    context ->
                                        executeSetDialog(
                                            context.getSource(),
                                            BlockPosArgument.getLoadedBlockPos(context, "pos"),
                                            ResourceLocationArgument.getId(
                                                context, "dialogId"))))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildClearSubCommand() {
    return Commands.literal("clear")
        .then(
            Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(
                    context ->
                        executeClear(
                            context.getSource(),
                            BlockPosArgument.getLoadedBlockPos(context, "pos"))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildInfoSubCommand() {
    return Commands.literal("info")
        .then(
            Commands.argument("pos", BlockPosArgument.blockPos())
                .executes(
                    context ->
                        executeInfo(
                            context.getSource(),
                            BlockPosArgument.getLoadedBlockPos(context, "pos"))));
  }

  private static int executeSetStory(
      CommandSourceStack source,
      BlockPos blockPosition,
      ResourceLocation storyId,
      ResourceLocation themeId) {
    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.openStory(storyId, themeId));
    registerOrUpdate(source, blockPosition, actionDataSet, storyId.toString());
    sendSuccessMessage(
        source,
        "Block at "
            + blockPosition.toShortString()
            + " opens story: "
            + storyId
            + (themeId != null ? " (theme: " + themeId + ")" : ""));
    return 1;
  }

  private static int executeSetInteractive(
      CommandSourceStack source, BlockPos blockPosition, ResourceLocation storyId) {
    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.openInteractiveStory(storyId));
    registerOrUpdate(source, blockPosition, actionDataSet, storyId.toString());
    sendSuccessMessage(
        source,
        "Block at " + blockPosition.toShortString() + " opens interactive story: " + storyId);
    return 1;
  }

  private static int executeSetDialog(
      CommandSourceStack source, BlockPos blockPosition, ResourceLocation dialogId) {
    ActionDataSet actionDataSet = new ActionDataSet();
    actionDataSet.add(ActionDataEntry.openDialog(dialogId));
    registerOrUpdate(source, blockPosition, actionDataSet, dialogId.toString());
    sendSuccessMessage(
        source, "Block at " + blockPosition.toShortString() + " opens dialog: " + dialogId);
    return 1;
  }

  private static int executeClear(CommandSourceStack source, BlockPos blockPosition) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);
    InteractionSavedData data = InteractionSavedData.get(source.getServer());

    if (data.unregister(blockId, InteractionEventType.ON_BLOCK_INTERACT)) {
      data.setDirty();
      sendSuccessMessage(source, "Block interaction cleared at " + blockPosition.toShortString());
    } else {
      sendInfoMessage(source, "No block interaction found at " + blockPosition.toShortString());
    }
    return 1;
  }

  private static int executeInfo(CommandSourceStack source, BlockPos blockPosition) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);
    InteractionSavedData data = InteractionSavedData.get(source.getServer());

    InteractionEntry entry = data.getInteraction(blockId, InteractionEventType.ON_BLOCK_INTERACT);
    if (entry == null) {
      sendInfoMessage(
          source, "No block interaction registered at " + blockPosition.toShortString());
    } else {
      sendInfoMessage(source, "Block at " + blockPosition.toShortString() + ": " + entry);
    }
    return 1;
  }

  private static void registerOrUpdate(
      CommandSourceStack source,
      BlockPos blockPosition,
      ActionDataSet actionDataSet,
      String label) {
    ServerLevel level = source.getLevel();
    UUID blockId = BlockUUID.fromBlockPos(level.dimension(), blockPosition);
    InteractionSavedData data = InteractionSavedData.get(source.getServer());

    if (data.getInteraction(blockId, InteractionEventType.ON_BLOCK_INTERACT) != null) {
      data.unregister(blockId, InteractionEventType.ON_BLOCK_INTERACT);
    }

    ResourceLocation dimension = level.dimension().location();
    data.register(
        InteractionEntry.forBlockInteract(
                blockId,
                blockPosition,
                TargetKind.BLOCK,
                InteractionType.RIGHT_CLICK,
                label,
                dimension)
            .withEdits(InteractionType.RIGHT_CLICK, label, actionDataSet));
    data.setDirty();
  }
}
