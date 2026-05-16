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
import de.markusbordihn.dialogqueststoryengine.client.screen.ScreenType;
import de.markusbordihn.dialogqueststoryengine.commands.Command;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionSavedData;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.OpenOverviewScreenMessage;
import de.markusbordihn.dialogqueststoryengine.network.message.SyncInteractionDataMessage;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerPlayer;

public class InteractionsCommand extends Command {

  private InteractionsCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("interactions")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .executes(context -> executeOpenOverview(context.getSource()));
  }

  private static int executeOpenOverview(CommandSourceStack source) {
    ServerPlayer player = source.getPlayer();
    if (player == null) {
      sendFailureMessage(source, "This command can only be run by a player.");
      return 0;
    }

    List<InteractionEntry> entries = InteractionSavedData.get(source.getServer()).getAllEntries();
    NetworkHandlerManager.sendToPlayer(player, new SyncInteractionDataMessage(entries));
    NetworkHandlerManager.sendToPlayer(
        player, new OpenOverviewScreenMessage(ScreenType.INTERACTIONS));
    return 1;
  }
}
