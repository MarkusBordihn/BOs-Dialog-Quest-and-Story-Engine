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
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.saveddata.SavedData;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractionData extends SavedData {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String DATA_NAME = Constants.MOD_ID + "_interaction_data";

  private static InteractionData instance;

  private final InteractionDataSet dataSet;
  private long syncVersion;

  public InteractionData() {
    this.dataSet = new InteractionDataSet();
  }

  public InteractionData(CompoundTag tag) {
    this.dataSet = new InteractionDataSet(tag);
  }

  public static void init(MinecraftServer server) {
    instance =
        server
            .overworld()
            .getDataStorage()
            .computeIfAbsent(InteractionData::new, InteractionData::new, DATA_NAME);
    log.info("Initialized interaction data with {} mapping(s).", instance.dataSet.size());
  }

  public static InteractionData get() {
    return instance;
  }

  public static void reset() {
    instance = null;
  }

  public void register(InteractionDataEntry entry) {
    dataSet.register(entry);
    syncVersion++;
    setDirty();
  }

  public boolean unregister(UUID targetId, InteractionType type) {
    boolean result = dataSet.unregister(targetId, type);
    if (result) {
      syncVersion++;
      setDirty();
    }
    return result;
  }

  public int unregisterAll(UUID targetId) {
    int count = dataSet.unregisterAll(targetId);
    if (count > 0) {
      syncVersion++;
      setDirty();
    }
    return count;
  }

  public boolean hasInteraction(UUID targetId, InteractionType type) {
    return dataSet.hasInteraction(targetId, type);
  }

  public InteractionDataEntry getInteraction(UUID targetId, InteractionType type) {
    return dataSet.getInteraction(targetId, type);
  }

  public InteractionDataEntry getFirstInteraction(UUID targetId) {
    return dataSet.getFirstInteraction(targetId);
  }

  public List<InteractionDataEntry> getInteractions(UUID targetId) {
    return dataSet.getInteractions(targetId);
  }

  public List<InteractionDataEntry> getAllEntries() {
    return dataSet.getAllEntries();
  }

  public int size() {
    return dataSet.size();
  }

  public boolean hasStepOnInteractions() {
    return dataSet.hasStepOnInteractions();
  }

  public void clearAll() {
    dataSet.clearAll();
    syncVersion++;
    setDirty();
  }

  public long getSyncVersion() {
    return syncVersion;
  }

  @Override
  public CompoundTag save(CompoundTag tag) {
    tag.merge(dataSet.save());
    return tag;
  }
}
