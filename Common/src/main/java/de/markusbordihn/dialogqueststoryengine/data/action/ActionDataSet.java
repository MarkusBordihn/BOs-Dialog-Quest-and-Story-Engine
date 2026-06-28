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

package de.markusbordihn.dialogqueststoryengine.data.action;

import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.FriendlyByteBuf;

public class ActionDataSet {

  private static final String TAG_ACTIONS = "Actions";

  private final LinkedHashSet<ActionDataEntry> entries;

  public ActionDataSet() {
    this.entries = new LinkedHashSet<>();
  }

  private ActionDataSet(LinkedHashSet<ActionDataEntry> entries) {
    this.entries = entries;
  }

  public static ActionDataSet load(CompoundTag tag) {
    LinkedHashSet<ActionDataEntry> loaded = new LinkedHashSet<>();
    if (tag.contains(TAG_ACTIONS, Tag.TAG_LIST)) {
      ListTag list = tag.getList(TAG_ACTIONS, Tag.TAG_COMPOUND);
      for (int i = 0; i < list.size(); i++) {
        loaded.add(ActionDataEntry.load(list.getCompound(i)));
      }
    }
    return new ActionDataSet(loaded);
  }

  public static ActionDataSet readFromBuf(FriendlyByteBuf buf) {
    int count = buf.readInt();
    LinkedHashSet<ActionDataEntry> loaded = new LinkedHashSet<>(count);
    for (int i = 0; i < count; i++) {
      loaded.add(ActionDataEntry.readFromBuf(buf));
    }
    return new ActionDataSet(loaded);
  }

  public void add(ActionDataEntry entry) {
    this.entries.add(entry);
  }

  public void remove(UUID entryId) {
    this.entries.removeIf(entry -> entry.id().equals(entryId));
  }

  public void replace(ActionDataEntry updated) {
    this.entries.removeIf(entry -> entry.id().equals(updated.id()));
    this.entries.add(updated);
  }

  public boolean isEmpty() {
    return this.entries.isEmpty();
  }

  public Set<ActionDataEntry> entries() {
    return Collections.unmodifiableSet(this.entries);
  }

  public ActionDataEntry getOrDefault(UUID entryId) {
    return this.entries.stream()
        .filter(entry -> entry.id().equals(entryId))
        .findFirst()
        .orElse(ActionDataEntry.empty(ActionType.NONE));
  }

  public boolean hasStoryAction() {
    for (ActionDataEntry entry : this.entries) {
      if (entry.type() == ActionType.OPEN_STORY
          || entry.type() == ActionType.OPEN_INTERACTIVE_STORY) {
        return true;
      }
    }
    return false;
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    ListTag list = new ListTag();
    for (ActionDataEntry entry : this.entries) {
      list.add(entry.save());
    }
    tag.put(TAG_ACTIONS, list);
    return tag;
  }

  public void writeToBuf(FriendlyByteBuf buf) {
    buf.writeInt(this.entries.size());
    for (ActionDataEntry entry : this.entries) {
      entry.writeToBuf(buf);
    }
  }

  public ActionDataSet copy() {
    return ActionDataSet.load(this.save());
  }
}
