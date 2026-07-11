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

package de.markusbordihn.dialogqueststoryengine.network.message.session;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.dialog.ClientDialogOpener;
import de.markusbordihn.dialogqueststoryengine.client.holopad.ClientStoryOpener;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.session.SessionCloseReason;
import java.util.Locale;
import java.util.UUID;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public record CloseSessionPacket(UUID sessionId, SessionCloseReason reason)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":close_session");

  public static CloseSessionPacket create(FriendlyByteBuf buffer) {
    UUID sessionId = buffer.readUUID();
    SessionCloseReason reason = buffer.readEnum(SessionCloseReason.class);

    return new CloseSessionPacket(sessionId, reason);
  }

  private static boolean shouldNotify(SessionCloseReason reason) {
    return reason == SessionCloseReason.RELOAD
        || reason == SessionCloseReason.TIMEOUT
        || reason == SessionCloseReason.CONTEXT_CHANGED;
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeUUID(this.sessionId);
    buffer.writeEnum(this.reason);
  }

  @Override
  public void handleClient() {
    ClientStoryOpener.closeSession(this.sessionId);
    ClientDialogOpener.closeSession(this.sessionId);
    if (Minecraft.getInstance().player != null && shouldNotify(this.reason)) {
      Minecraft.getInstance()
          .player
          .displayClientMessage(
              Component.translatable(
                  "message.dialog_quest_and_story_engine.session.closed."
                      + this.reason.name().toLowerCase(Locale.ROOT)),
              false);
    }
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }
}
