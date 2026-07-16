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
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestRewardClaimReason;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.util.Optional;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record QuestRewardClaimResultPacket(
    ResourceLocation questId, Optional<QuestRewardClaimReason> reason)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      new ResourceLocation(Constants.MOD_ID, "quest_reward_claim_result");

  public static QuestRewardClaimResultPacket success(ResourceLocation questId) {
    return new QuestRewardClaimResultPacket(questId, Optional.empty());
  }

  public static QuestRewardClaimResultPacket rejected(
      ResourceLocation questId, QuestRewardClaimReason reason) {
    return new QuestRewardClaimResultPacket(questId, Optional.of(reason));
  }

  public static QuestRewardClaimResultPacket create(FriendlyByteBuf buffer) {
    ResourceLocation questId = buffer.readResourceLocation();
    Optional<QuestRewardClaimReason> reason =
        buffer.readOptional(buf -> buf.readEnum(QuestRewardClaimReason.class));
    return new QuestRewardClaimResultPacket(questId, reason);
  }

  public boolean isSuccess() {
    return this.reason.isEmpty();
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.questId);
    buffer.writeOptional(this.reason, FriendlyByteBuf::writeEnum);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }
}
