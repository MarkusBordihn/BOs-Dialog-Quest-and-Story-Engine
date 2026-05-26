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

package de.markusbordihn.dialogqueststoryengine.commands.manager;

import com.mojang.brigadier.CommandDispatcher;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.server.commands.BindCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.ClearCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.DebugCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.DialogCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.FactCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.HolopadCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.InteractionsCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.ListCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.QuestCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.ReloadCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.StoryCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.UnbindCommand;
import de.markusbordihn.dialogqueststoryengine.server.commands.ValidateCommand;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class CommandManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private CommandManager() {}

  public static void registerCommands(CommandDispatcher<CommandSourceStack> dispatcher) {
    log.info("{} Commands ...", Constants.LOG_REGISTER_PREFIX);
    dispatcher.register(
        Commands.literal(Constants.MOD_COMMAND)
            .then(BindCommand.register())
            .then(UnbindCommand.register())
            .then(ListCommand.register())
            .then(ClearCommand.register())
            .then(InteractionsCommand.register())
            .then(ReloadCommand.register())
            .then(ValidateCommand.register())
            .then(StoryCommand.register())
            .then(DialogCommand.register())
            .then(QuestCommand.register())
            .then(FactCommand.register())
            .then(HolopadCommand.register())
            .then(DebugCommand.register()));
  }
}
