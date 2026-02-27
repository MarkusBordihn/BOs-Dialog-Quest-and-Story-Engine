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
import de.markusbordihn.dialogqueststoryengine.client.InteractionClientData;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record SyncInteractionDataMessage(List<InteractionDataEntry> entries)
    implements NetworkMessageRecord {

  public static final ResourceLocation MESSAGE_ID =
      ResourceLocation.tryParse(Constants.MOD_ID + ":sync_interaction_data");

  public SyncInteractionDataMessage(List<InteractionDataEntry> entries) {
    this.entries = entries != null ? entries : Collections.emptyList();
  }

  public static SyncInteractionDataMessage create(FriendlyByteBuf buffer) {
    int count = buffer.readInt();
    List<InteractionDataEntry> entries = new ArrayList<>(count);
    for (int i = 0; i < count; i++) {
      entries.add(InteractionDataEntry.readFromBuf(buffer));
    }
    return new SyncInteractionDataMessage(entries);
  }

  @Override
  public void write(FriendlyByteBuf buffer) {
    buffer.writeInt(entries.size());
    for (InteractionDataEntry entry : entries) {
      entry.writeToBuf(buffer);
    }
  }

  @Override
  public ResourceLocation id() {
    return MESSAGE_ID;
  }

  @Override
  public void handleClient() {
    InteractionClientData.setEntries(entries);
  }
}
