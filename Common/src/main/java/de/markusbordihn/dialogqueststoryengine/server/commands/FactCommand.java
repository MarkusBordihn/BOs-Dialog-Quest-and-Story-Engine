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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.dialogqueststoryengine.commands.Command;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.Optional;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class FactCommand extends Command {

  private FactCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("fact")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .then(
            Commands.literal("set")
                .then(
                    Commands.argument("key", StringArgumentType.string())
                        .then(
                            Commands.argument("value", StringArgumentType.greedyString())
                                .executes(
                                    context ->
                                        executeSet(
                                            context.getSource(),
                                            StringArgumentType.getString(context, "key"),
                                            StringArgumentType.getString(context, "value"))))))
        .then(
            Commands.literal("get")
                .then(
                    Commands.argument("key", StringArgumentType.string())
                        .executes(
                            context ->
                                executeGet(
                                    context.getSource(),
                                    StringArgumentType.getString(context, "key")))));
  }

  private static int executeSet(CommandSourceStack source, String key, String rawValue)
      throws CommandSyntaxException {
    ServerPlayer player = source.getPlayerOrException();
    FactValue value = parseFactValue(rawValue);
    PlayerStateService.setFact(player.getUUID(), FactScope.PLAYER, key, value);
    sendSuccessMessage(
        source,
        "fact set: " + key + " = " + rawValue + " [" + value.type().name().toLowerCase() + "]");

    return 1;
  }

  private static int executeGet(CommandSourceStack source, String key)
      throws CommandSyntaxException {
    ServerPlayer player = source.getPlayerOrException();
    Optional<FactValue> fact = PlayerStateService.getFact(player.getUUID(), FactScope.PLAYER, key);
    if (fact.isPresent()) {
      sendInfoMessage(source, "fact get: " + key + " = " + formatFactValue(fact.get()));
    } else {
      sendFailureMessage(source, "fact get: key '" + key + "' not found.");
    }

    return 1;
  }

  private static FactValue parseFactValue(String raw) {
    if (raw.equalsIgnoreCase("true")) {
      return FactValue.of(true);
    }

    if (raw.equalsIgnoreCase("false")) {
      return FactValue.of(false);
    }

    try {
      return FactValue.of(Long.parseLong(raw));
    } catch (NumberFormatException ignoredLong) {
      try {
        return FactValue.of(Double.parseDouble(raw));
      } catch (NumberFormatException ignoredDouble) {
        return FactValue.of(raw);
      }
    }
  }

  private static String formatFactValue(FactValue fact) {
    String display;
    if (fact instanceof FactValue.BooleanValue booleanValue) {
      display = String.valueOf(booleanValue.value());
    } else if (fact instanceof FactValue.LongValue longValue) {
      display = String.valueOf(longValue.value());
    } else if (fact instanceof FactValue.DoubleValue doubleValue) {
      display = String.valueOf(doubleValue.value());
    } else if (fact instanceof FactValue.StringValue stringValue) {
      display = stringValue.value();
    } else if (fact instanceof FactValue.ResourceLocationValue resourceLocationValue) {
      display = resourceLocationValue.value().toString();
    } else {
      display = fact.toString();
    }

    return display + " [" + fact.type().name().toLowerCase() + "]";
  }
}
