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
import de.markusbordihn.dialogqueststoryengine.network.C2SRateLimiter;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestTrackingService;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public record TrackQuestPacket(Optional<ResourceLocation> questId) implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      new ResourceLocation(Constants.MOD_ID, "track_quest");

  public static TrackQuestPacket create(FriendlyByteBuf buffer) {
    return new TrackQuestPacket(buffer.readOptional(FriendlyByteBuf::readResourceLocation));
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeOptional(this.questId, FriendlyByteBuf::writeResourceLocation);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleServer(ServerPlayer serverPlayer) {
    if (!C2SRateLimiter.allow(serverPlayer.getUUID())) {
      return;
    }

    QuestTrackingService.handleTrackRequest(serverPlayer, this.questId);
  }
}
