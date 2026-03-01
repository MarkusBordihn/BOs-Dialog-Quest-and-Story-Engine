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
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionData;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import net.minecraft.ChatFormatting;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record RemoveInteractionMessage(InteractionDataEntry entry) implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":remove_interaction");
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static RemoveInteractionMessage create(FriendlyByteBuf buffer) {
    InteractionDataEntry entry = InteractionDataEntry.readFromBuf(buffer);
    return new RemoveInteractionMessage(entry);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    entry.writeToBuf(buffer);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleServer(ServerPlayer serverPlayer) {
    if (!serverPlayer.hasPermissions(2)) {
      log.warn("Player {} lacks permission to remove interaction.", serverPlayer.getName());
      return;
    }
    InteractionData data = InteractionData.get();
    if (data == null) {
      log.warn("InteractionData not available.");
      return;
    }
    if (data.hasInteraction(entry.targetId(), entry.type())) {
      data.unregister(entry.targetId(), entry.type());
      serverPlayer.sendSystemMessage(
          Component.literal("✖ Removed interaction '" + entry.label() + "'.")
              .withStyle(ChatFormatting.YELLOW));
      log.info("Player {} removed interaction: {}", serverPlayer.getName().getString(), entry);
    }
  }
}
