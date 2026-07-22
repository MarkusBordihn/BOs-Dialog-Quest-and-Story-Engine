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
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.story.OpenClientStoryPacket;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class StoryCommand extends Command {

  private StoryCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("story")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(
            Commands.literal("open")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .suggests(ContentIdSuggestions.STORY_ENTRIES)
                        .executes(
                            context -> {
                              sendInfoMessage(
                                  context.getSource(),
                                  "story open: use 'story preview <id>' for Resource Pack stories or create an interactive_story data file.");
                              return 1;
                            })))
        .then(
            Commands.literal("preview")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .suggests(ContentIdSuggestions.STORY_ENTRIES)
                        .executes(
                            context -> {
                              ResourceLocation storyId =
                                  ResourceLocationArgument.getId(context, "id");
                              ServerPlayer player = context.getSource().getPlayerOrException();
                              NetworkHandlerManager.sendToPlayer(
                                  player, new OpenClientStoryPacket(storyId, null));
                              sendSuccessMessage(
                                  context.getSource(), "Opening story preview: " + storyId);
                              return 1;
                            })));
  }
}
