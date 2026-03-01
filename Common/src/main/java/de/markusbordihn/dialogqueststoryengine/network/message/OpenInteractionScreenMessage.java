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

package de.markusbordihn.dialogqueststoryengine.network.message;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionData;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.util.UUID;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record OpenInteractionScreenMessage(UUID targetId, InteractionType type)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":open_interaction_screen");
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static OpenInteractionScreenMessage create(FriendlyByteBuf buffer) {
    UUID targetId = buffer.readUUID();
    InteractionType type = buffer.readEnum(InteractionType.class);
    return new OpenInteractionScreenMessage(targetId, type);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeUUID(targetId);
    buffer.writeEnum(type);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleServer(ServerPlayer serverPlayer) {
    if (!serverPlayer.hasPermissions(2)) {
      log.warn("Player {} lacks permission to open interaction screen.", serverPlayer.getName());
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null) {
      log.warn("InteractionData not available.");
      return;
    }
    InteractionDataEntry match = data.getInteraction(targetId, type);
    if (match != null) {
      NetworkHandlerManager.sendToPlayer(
          serverPlayer, new InteractionScreenDataMessage(match, false));
    } else {
      log.warn("No interaction found for target {} with type {}.", targetId, type);
    }
  }
}
