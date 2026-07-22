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

public class InteractionStore {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String TAG_ENTRIES = "Entries";

  private final Map<UUID, List<InteractionEntry>> entries = new HashMap<>();
  private List<InteractionEntry> cachedAllEntries;
  private int totalSize;
  private boolean hasStepOnInteractions;

  public InteractionStore() {}

  public InteractionStore(CompoundTag tag) {
    this.load(tag);
  }

  public void register(InteractionEntry entry) {
    List<InteractionEntry> list =
        this.entries.computeIfAbsent(entry.targetId(), targetId -> new ArrayList<>());
    list.removeIf(existing -> existing.eventType().equals(entry.eventType()));
    list.add(entry);
    this.invalidateCache();
    log.info("Registered interaction: {}", entry);
  }

  public boolean unregister(UUID targetId, InteractionEventType eventType) {
    List<InteractionEntry> list = this.entries.get(targetId);
    if (list == null) {
      return false;
    }
    boolean removed = list.removeIf(entry -> entry.eventType().equals(eventType));
    if (list.isEmpty()) {
      this.entries.remove(targetId);
    }
    if (removed) {
      this.invalidateCache();
      log.info("Unregistered interaction {} for target {}", eventType, targetId);
    }
    return removed;
  }

  public int unregisterAll(UUID targetId) {
    List<InteractionEntry> list = this.entries.remove(targetId);
    if (list == null || list.isEmpty()) {
      return 0;
    }
    int count = list.size();
    this.invalidateCache();
    log.info("Unregistered all {} interaction(s) for target {}", count, targetId);
    return count;
  }

  public boolean hasInteraction(UUID targetId, InteractionEventType eventType) {
    return this.getInteraction(targetId, eventType) != null;
  }

  public InteractionEntry getInteraction(UUID targetId, InteractionEventType eventType) {
    List<InteractionEntry> list = this.entries.get(targetId);
    if (list == null) {
      return null;
    }
    for (InteractionEntry entry : list) {
      if (entry.eventType().equals(eventType)) {
        return entry;
      }
    }
    return null;
  }

  public InteractionEntry getFirstInteraction(UUID targetId) {
    List<InteractionEntry> list = this.entries.get(targetId);
    if (list == null || list.isEmpty()) {
      return null;
    }
    return list.get(0);
  }

  public List<InteractionEntry> getInteractionsForTarget(UUID targetId) {
    List<InteractionEntry> list = this.entries.get(targetId);
    if (list == null) {
      return Collections.emptyList();
    }
    return Collections.unmodifiableList(list);
  }

  public List<InteractionEntry> getAllEntries() {
    if (this.cachedAllEntries == null) {
      List<InteractionEntry> all = new ArrayList<>(this.totalSize);
      for (List<InteractionEntry> list : this.entries.values()) {
        all.addAll(list);
      }
      this.cachedAllEntries = all;
    }
    return this.cachedAllEntries;
  }

  public int size() {
    return this.totalSize;
  }

  public boolean hasStepOnInteractions() {
    return this.hasStepOnInteractions;
  }

  public void clearAll() {
    this.entries.clear();
    this.invalidateCache();
    log.info("Cleared all interaction mappings.");
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    ListTag listTag = new ListTag();
    for (List<InteractionEntry> list : this.entries.values()) {
      for (InteractionEntry entry : list) {
        listTag.add(entry.save());
      }
    }
    tag.put(TAG_ENTRIES, listTag);
    return tag;
  }

  public void load(CompoundTag tag) {
    this.entries.clear();
    if (tag.contains(TAG_ENTRIES, Tag.TAG_LIST)) {
      ListTag listTag = tag.getList(TAG_ENTRIES, Tag.TAG_COMPOUND);
      for (int i = 0; i < listTag.size(); i++) {
        InteractionEntry entry = InteractionEntry.load(listTag.getCompound(i));
        if (entry != null) {
          this.entries.computeIfAbsent(entry.targetId(), targetId -> new ArrayList<>()).add(entry);
        }
      }
    }
    this.invalidateCache();
    log.info("Loaded {} interaction mapping(s).", this.totalSize);
  }

  private void invalidateCache() {
    this.cachedAllEntries = null;
    this.totalSize = 0;
    this.hasStepOnInteractions = false;
    for (List<InteractionEntry> list : this.entries.values()) {
      this.totalSize += list.size();
      if (!this.hasStepOnInteractions) {
        for (InteractionEntry entry : list) {
          if (entry.eventType() == InteractionEventType.ON_STEP_ON) {
            this.hasStepOnInteractions = true;
            break;
          }
        }
      }
    }
  }
}
