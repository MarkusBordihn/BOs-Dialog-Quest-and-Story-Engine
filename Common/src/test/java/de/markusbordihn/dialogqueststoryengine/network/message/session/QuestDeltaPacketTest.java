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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardClaimState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import io.netty.buffer.Unpooled;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class QuestDeltaPacketTest {

  @Test
  void roundTripPreservesOnlyChangedSteps() {
    ResourceLocation questId = new ResourceLocation("test", "quest");
    QuestDeltaPacket original =
        new QuestDeltaPacket(
            questId,
            QuestState.ACTIVE,
            Map.of("collect", StepProgress.active(5).withProgress(2)),
            RewardClaimState.AVAILABLE,
            7);

    FriendlyByteBuf buffer = new FriendlyByteBuf(Unpooled.buffer());
    original.write(buffer);
    QuestDeltaPacket decoded = QuestDeltaPacket.create(buffer);

    assertEquals(questId, decoded.questId());
    assertEquals(QuestState.ACTIVE, decoded.questState());
    assertEquals(RewardClaimState.AVAILABLE, decoded.rewardClaimState());
    assertEquals(7, decoded.revision());
    assertEquals(1, decoded.changedSteps().size());
    assertEquals(2, decoded.changedSteps().get("collect").progress());
    assertFalse(decoded.changedSteps().containsKey("talk"));
  }
}
