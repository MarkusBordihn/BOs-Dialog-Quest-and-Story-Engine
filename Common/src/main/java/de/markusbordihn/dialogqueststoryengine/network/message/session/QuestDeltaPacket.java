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
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import de.markusbordihn.dialogqueststoryengine.state.QuestState;
import de.markusbordihn.dialogqueststoryengine.state.StepProgress;
import de.markusbordihn.dialogqueststoryengine.state.StepState;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record QuestDeltaPacket(
    ResourceLocation questId,
    QuestState questState,
    Map<String, StepProgress> changedSteps,
    int revision)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":quest_delta");

  public static QuestDeltaPacket create(FriendlyByteBuf buffer) {
    ResourceLocation questId = buffer.readResourceLocation();
    QuestState questState = buffer.readEnum(QuestState.class);
    int stepCount = buffer.readInt();
    Map<String, StepProgress> changedSteps = new LinkedHashMap<>(stepCount);
    for (int i = 0; i < stepCount; i++) {
      String stepId = buffer.readUtf();
      StepState stepState = buffer.readEnum(StepState.class);
      int progress = buffer.readInt();
      int required = buffer.readInt();
      changedSteps.put(stepId, new StepProgress(stepState, progress, required));
    }
    int revision = buffer.readInt();

    return new QuestDeltaPacket(questId, questState, changedSteps, revision);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.questId);
    buffer.writeEnum(this.questState);
    buffer.writeInt(this.changedSteps.size());
    this.changedSteps.forEach(
        (stepId, stepProgress) -> {
          buffer.writeUtf(stepId);
          buffer.writeEnum(stepProgress.state());
          buffer.writeInt(stepProgress.progress());
          buffer.writeInt(stepProgress.required());
        });
    buffer.writeInt(this.revision);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }
}
