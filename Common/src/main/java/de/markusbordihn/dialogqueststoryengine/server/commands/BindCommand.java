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

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.markusbordihn.dialogqueststoryengine.commands.BindManager;
import de.markusbordihn.dialogqueststoryengine.commands.Command;
import de.markusbordihn.dialogqueststoryengine.commands.suggestion.InteractionTypeSuggestions;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class BindCommand extends Command {

  private BindCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("bind")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(
            Commands.argument("type", StringArgumentType.word())
                .suggests(InteractionTypeSuggestions.INSTANCE)
                .then(
                    Commands.argument("label", StringArgumentType.greedyString())
                        .executes(
                            context -> {
                              String typeName = StringArgumentType.getString(context, "type");
                              String label = StringArgumentType.getString(context, "label");
                              return executeBind(context.getSource(), typeName, label);
                            })));
  }

  private static int executeBind(CommandSourceStack source, String typeName, String label) {
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      sendFailureMessage(source, "This command can only be used by players.");
      return 0;
    }
    InteractionType type = InteractionType.fromName(typeName);
    if (type == null) {
      sendFailureMessage(source, "Unknown interaction type: " + typeName);
      return 0;
    }
    BindManager.startBind(player, type, label);
    sendSuccessMessage(
        source,
        "Bind mode active for "
            + type.name()
            + " with label '"
            + label
            + "'. Click an entity or block to register.");
    return 1;
  }
}
