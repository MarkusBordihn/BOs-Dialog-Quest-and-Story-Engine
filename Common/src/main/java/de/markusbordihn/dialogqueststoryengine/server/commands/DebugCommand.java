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

import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.commands.Command;
import de.markusbordihn.dialogqueststoryengine.data.debug.ExecutionTraceEntry;
import de.markusbordihn.dialogqueststoryengine.debug.DebugManager;
import de.markusbordihn.dialogqueststoryengine.debug.ExecutionTraceService;
import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

public class DebugCommand extends Command {

  private static final DateTimeFormatter TRACE_TIME_FORMAT =
      DateTimeFormatter.ofPattern("HH:mm:ss").withZone(ZoneId.systemDefault());

  private DebugCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("debug")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(buildCoreSubCommand())
        .then(buildPlayerSubCommand())
        .then(buildTraceSubCommand());
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildCoreSubCommand() {
    return Commands.literal("core")
        .then(
            Commands.argument("enable", BoolArgumentType.bool())
                .executes(
                    context ->
                        setDebug(
                            context.getSource(), BoolArgumentType.getBool(context, "enable"))));
  }

  private static int setDebug(CommandSourceStack source, boolean enable) {
    DebugManager.enableDebugLevel(enable);
    if (enable) {
      sendSuccessMessage(
          source,
          "Enabled debug logging for " + Constants.MOD_NAME + ". See debug.log for output.");
      sendInfoMessage(source, "Use '/" + Constants.MOD_COMMAND + " debug core false' to disable.");
    } else {
      sendSuccessMessage(source, "Disabled debug logging for " + Constants.MOD_NAME + ".");
    }
    return 1;
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildPlayerSubCommand() {
    return Commands.literal("player")
        .executes(
            context -> executePlayerDump(context.getSource(), context.getSource().getPlayer()))
        .then(
            Commands.argument("target", EntityArgument.player())
                .executes(
                    context ->
                        executePlayerDump(
                            context.getSource(), EntityArgument.getPlayer(context, "target"))));
  }

  private static ArgumentBuilder<CommandSourceStack, ?> buildTraceSubCommand() {
    return Commands.literal("trace")
        .executes(context -> executeTrace(context.getSource(), context.getSource().getPlayer()))
        .then(
            Commands.argument("target", EntityArgument.player())
                .executes(
                    context ->
                        executeTrace(
                            context.getSource(), EntityArgument.getPlayer(context, "target"))));
  }

  private static int executePlayerDump(CommandSourceStack source, ServerPlayer player) {
    if (player == null) {
      sendFailureMessage(source, "This sub-command requires a player.");
      return 0;
    }

    sendInfoMessage(source, "debug player: not yet implemented.");
    return 1;
  }

  private static int executeTrace(CommandSourceStack source, ServerPlayer player) {
    if (player == null) {
      sendFailureMessage(source, "This sub-command requires a player.");
      return 0;
    }

    List<ExecutionTraceEntry> entries = ExecutionTraceService.entries(player.getUUID());
    if (entries.isEmpty()) {
      sendInfoMessage(source, "No trace entries for " + player.getName().getString() + ".");
      return 1;
    }

    sendInfoMessage(
        source,
        "Trace for "
            + player.getName().getString()
            + " ("
            + entries.size()
            + " entries, oldest first):");
    for (ExecutionTraceEntry entry : entries) {
      String time = TRACE_TIME_FORMAT.format(Instant.ofEpochMilli(entry.timestamp()));
      String content = entry.contentId() != null ? entry.contentId().toString() : "-";
      String choice = entry.choiceId() != null ? " choice=" + entry.choiceId() : "";
      String error = entry.errorMessage() != null ? " error=" + entry.errorMessage() : "";
      sendInfoMessage(
          source, "  [" + time + "] " + entry.eventType() + " " + content + choice + error);
    }

    return 1;
  }
}
