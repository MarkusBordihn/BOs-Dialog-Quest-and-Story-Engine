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
import de.markusbordihn.dialogqueststoryengine.client.ClientProgressState;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record StoryDeltaPacket(
    List<ResourceLocation> unlockedStoryIds, List<ResourceLocation> readStoryIds, int revision)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":story_delta");

  public static StoryDeltaPacket create(FriendlyByteBuf buffer) {
    int unlockedCount = buffer.readInt();
    List<ResourceLocation> unlockedStoryIds = new ArrayList<>(unlockedCount);
    for (int i = 0; i < unlockedCount; i++) {
      unlockedStoryIds.add(buffer.readResourceLocation());
    }
    int readCount = buffer.readInt();
    List<ResourceLocation> readStoryIds = new ArrayList<>(readCount);
    for (int i = 0; i < readCount; i++) {
      readStoryIds.add(buffer.readResourceLocation());
    }
    int revision = buffer.readInt();
    return new StoryDeltaPacket(unlockedStoryIds, readStoryIds, revision);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(this.unlockedStoryIds.size());
    this.unlockedStoryIds.forEach(buffer::writeResourceLocation);
    buffer.writeInt(this.readStoryIds.size());
    this.readStoryIds.forEach(buffer::writeResourceLocation);
    buffer.writeInt(this.revision);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleClient() {
    ClientProgressState.apply(this);
  }
}
