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

package de.markusbordihn.dialogqueststoryengine.data.saveddata;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionStore;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractionSavedData extends SavedData {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String DATA_NAME = Constants.MOD_ID + "_interaction_data";

  private final InteractionStore wandStore;
  private long syncVersion;

  public InteractionSavedData() {
    this.wandStore = new InteractionStore();
  }

  public InteractionSavedData(CompoundTag tag) {
    this.wandStore = new InteractionStore(tag);
  }

  public static InteractionSavedData get(MinecraftServer server) {
    return server
        .overworld()
        .getDataStorage()
        .computeIfAbsent(InteractionSavedData::new, InteractionSavedData::new, DATA_NAME);
  }

  public void register(InteractionEntry entry) {
    this.wandStore.register(entry);
    this.syncVersion++;
    this.setDirty();
  }

  public boolean unregister(UUID targetId, InteractionEventType eventType) {
    boolean removed = this.wandStore.unregister(targetId, eventType);
    if (removed) {
      this.syncVersion++;
      this.setDirty();
    }
    return removed;
  }

  public int unregisterAll(UUID targetId) {
    int count = this.wandStore.unregisterAll(targetId);
    if (count > 0) {
      this.syncVersion++;
      this.setDirty();
    }
    return count;
  }

  public boolean hasInteraction(UUID targetId, InteractionEventType eventType) {
    return this.wandStore.hasInteraction(targetId, eventType);
  }

  public InteractionEntry getInteraction(UUID targetId, InteractionEventType eventType) {
    return this.wandStore.getInteraction(targetId, eventType);
  }

  public InteractionEntry getFirstInteraction(UUID targetId) {
    return this.wandStore.getFirstInteraction(targetId);
  }

  public List<InteractionEntry> getInteractionsForTarget(UUID targetId) {
    return this.wandStore.getInteractionsForTarget(targetId);
  }

  public List<InteractionEntry> getAllEntries() {
    return this.wandStore.getAllEntries();
  }

  public int size() {
    return this.wandStore.size();
  }

  public boolean hasStepOnInteractions() {
    return this.wandStore.hasStepOnInteractions();
  }

  public void clearAll() {
    this.wandStore.clearAll();
    this.syncVersion++;
    this.setDirty();
  }

  public long getSyncVersion() {
    return this.syncVersion;
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    tag.merge(this.wandStore.save());
    return tag;
  }
}
