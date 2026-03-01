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

package de.markusbordihn.dialogqueststoryengine.data.interaction;

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractionDataSet {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String TAG_ENTRIES = "Entries";

  private final Map<UUID, List<InteractionDataEntry>> entries = new HashMap<>();
  private List<InteractionDataEntry> cachedAllEntries;
  private int totalSize;
  private boolean hasStepOnInteractions;

  public InteractionDataSet() {}

  public InteractionDataSet(CompoundTag tag) {
    load(tag);
  }

  public void register(InteractionDataEntry entry) {
    List<InteractionDataEntry> list =
        entries.computeIfAbsent(entry.targetId(), k -> new ArrayList<>());
    list.removeIf(existing -> existing.type() == entry.type());
    list.add(entry);
    invalidateCache();
    log.info("Registered interaction: {}", entry);
  }

  public boolean unregister(UUID targetId, InteractionType type) {
    List<InteractionDataEntry> list = entries.get(targetId);
    if (list == null) {
      return false;
    }
    boolean removed = list.removeIf(e -> e.type() == type);
    if (list.isEmpty()) {
      entries.remove(targetId);
    }
    if (removed) {
      invalidateCache();
      log.info("Unregistered interaction {} for target {}", type, targetId);
    }
    return removed;
  }

  public int unregisterAll(UUID targetId) {
    List<InteractionDataEntry> list = entries.remove(targetId);
    if (list == null || list.isEmpty()) {
      return 0;
    }
    int count = list.size();
    invalidateCache();
    log.info("Unregistered all {} interaction(s) for target {}", count, targetId);
    return count;
  }

  public boolean hasInteraction(UUID targetId, InteractionType type) {
    return getInteraction(targetId, type) != null;
  }

  public InteractionDataEntry getInteraction(UUID targetId, InteractionType type) {
    List<InteractionDataEntry> list = entries.get(targetId);
    if (list == null) {
      return null;
    }
    for (InteractionDataEntry entry : list) {
      if (entry.type() == type) {
        return entry;
      }
    }
    return null;
  }

  public InteractionDataEntry getFirstInteraction(UUID targetId) {
    List<InteractionDataEntry> list = entries.get(targetId);
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  public List<InteractionDataEntry> getInteractions(UUID targetId) {
    List<InteractionDataEntry> list = entries.get(targetId);
    if (list == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(list);
  }

  public List<InteractionDataEntry> getAllEntries() {
    if (cachedAllEntries == null) {
      List<InteractionDataEntry> all = new ArrayList<>(totalSize);
      for (List<InteractionDataEntry> list : entries.values()) {
        all.addAll(list);
      }
      cachedAllEntries = all;
    }
    return cachedAllEntries;
  }

  public int size() {
    return totalSize;
  }

  public boolean hasStepOnInteractions() {
    return hasStepOnInteractions;
  }

  public void clearAll() {
    entries.clear();
    invalidateCache();
    log.info("Cleared all interaction mappings.");
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    ListTag listTag = new ListTag();
    for (List<InteractionDataEntry> list : entries.values()) {
      for (InteractionDataEntry entry : list) {
        listTag.add(entry.save());
      }
    }
    tag.put(TAG_ENTRIES, listTag);
    return tag;
  }

  public void load(CompoundTag tag) {
    entries.clear();
    if (tag.contains(TAG_ENTRIES, Tag.TAG_LIST)) {
      ListTag listTag = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
      for (int i = 0; i < listTag.size(); i++) {
        InteractionDataEntry entry = InteractionDataEntry.load(listTag.getCompound(i));
        if (entry != null) {
          entries.computeIfAbsent(entry.targetId(), k -> new ArrayList<>()).add(entry);
        }
      }
    }
    invalidateCache();
    log.info("Loaded {} interaction mapping(s).", totalSize);
  }

  private void invalidateCache() {
    cachedAllEntries = null;
    totalSize = 0;
    hasStepOnInteractions = false;
    for (List<InteractionDataEntry> list : entries.values()) {
      totalSize += list.size();
      if (!hasStepOnInteractions) {
        for (InteractionDataEntry entry : list) {
          if (entry.type() == InteractionType.STEP_ON) {
            hasStepOnInteractions = true;
            break;
          }
        }
      }
    }
  }
}
