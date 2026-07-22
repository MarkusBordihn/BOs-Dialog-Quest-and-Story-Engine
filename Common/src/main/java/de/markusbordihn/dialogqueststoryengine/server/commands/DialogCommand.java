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
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import java.util.Collection;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class DialogCommand extends Command {

  private DialogCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("dialog")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(
            Commands.literal("open")
                .then(
                    Commands.argument("id", ResourceLocationArgument.id())
                        .suggests(ContentIdSuggestions.DIALOGS)
                        .executes(
                            context -> {
                              ResourceLocation dialogId =
                                  ResourceLocationArgument.getId(context, "id");
                              ServerPlayer player = context.getSource().getPlayerOrException();
                              if (SessionManager.openDialogSession(
                                      player, dialogId, Optional.empty())
                                  == null) {
                                sendFailureMessage(
                                    context.getSource(), "Dialog not found: " + dialogId);
                                return 0;
                              }

                              sendSuccessMessage(
                                  context.getSource(), "Opening dialog: " + dialogId);
                              return 1;
                            })
                        .then(
                            Commands.argument("targets", EntityArgument.players())
                                .executes(
                                    context -> {
                                      ResourceLocation dialogId =
                                          ResourceLocationArgument.getId(context, "id");
                                      Collection<ServerPlayer> targets =
                                          EntityArgument.getPlayers(context, "targets");
                                      int successCount = 0;

                                      for (ServerPlayer target : targets) {
                                        if (SessionManager.openDialogSession(
                                                target, dialogId, Optional.empty())
                                            != null) {
                                          successCount++;
                                        }
                                      }

                                      if (successCount == 0) {
                                        sendFailureMessage(
                                            context.getSource(),
                                            "Dialog not found or no valid targets: " + dialogId);
                                        return 0;
                                      }

                                      sendSuccessMessage(
                                          context.getSource(),
                                          "Opened dialog '"
                                              + dialogId
                                              + "' for "
                                              + successCount
                                              + " player(s)");
                                      return successCount;
                                    }))));
  }
}
