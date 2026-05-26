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

package de.markusbordihn.dialogqueststoryengine.network.message.story;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.client.holopad.ClientStoryOpener;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record OpenClientStoryPacket(
    ResourceLocation storyId, ResourceLocation themeOverrideId)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":open_client_story");

  public static OpenClientStoryPacket create(FriendlyByteBuf buffer) {
    ResourceLocation storyId = buffer.readResourceLocation();
    ResourceLocation themeOverrideId =
        buffer.readBoolean() ? buffer.readResourceLocation() : null;
    return new OpenClientStoryPacket(storyId, themeOverrideId);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeResourceLocation(this.storyId);
    buffer.writeBoolean(this.themeOverrideId != null);
    if (this.themeOverrideId != null) {
      buffer.writeResourceLocation(this.themeOverrideId);
    }
  }

  @Override
  public void handleClient() {
    ClientStoryOpener.open(this.storyId, this.themeOverrideId);
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }
}
