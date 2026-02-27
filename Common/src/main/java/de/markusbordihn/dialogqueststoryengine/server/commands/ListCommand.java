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
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionData;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ListCommand extends Command {

  private ListCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("list")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .executes(context -> executeList(context.getSource()));
  }

  private static int executeList(CommandSourceStack source) {
    InteractionData data = InteractionData.get();
    if (data == null) {
      sendFailureMessage(source, "Interaction data not available.");
      return 0;
    }
    List<InteractionDataEntry> entries = data.getAllEntries();
    if (entries.isEmpty()) {
      sendInfoMessage(source, "No interaction mappings registered.");
      return 1;
    }
    sendInfoMessage(source, "Interaction mappings (" + entries.size() + "):");
    for (InteractionDataEntry entry : entries) {
      sendInfoMessage(source, "  " + entry);
    }
    return 1;
  }
}
