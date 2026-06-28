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
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;

public record ActionDataEntry(UUID id, ActionType type, CompoundTag data) {

  private static final String TAG_ID = "Id";
  private static final String TAG_TYPE = "Type";
  private static final String TAG_DATA = "Data";
  private static final String DATA_STORY_ID = "StoryId";
  private static final String DATA_THEME_OVERRIDE_ID = "ThemeOverrideId";
  private static final String DATA_COMMAND = "Command";
  private static final String DATA_FACT_ID = "FactId";
  private static final String DATA_FACT_VALUE = "FactValue";

  public static ActionDataEntry openStory(
      ResourceLocation storyId, ResourceLocation themeOverrideId) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_STORY_ID, storyId.toString());
    if (themeOverrideId != null) {
      data.putString(DATA_THEME_OVERRIDE_ID, themeOverrideId.toString());
    }
    return new ActionDataEntry(UUID.randomUUID(), ActionType.OPEN_STORY, data);
  }

  public static ActionDataEntry openInteractiveStory(ResourceLocation storyId) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_STORY_ID, storyId.toString());
    return new ActionDataEntry(UUID.randomUUID(), ActionType.OPEN_INTERACTIVE_STORY, data);
  }

  public static ActionDataEntry runCommand(String command) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_COMMAND, command);
    return new ActionDataEntry(UUID.randomUUID(), ActionType.RUN_COMMAND, data);
  }

  public static ActionDataEntry setFact(ResourceLocation factId, String value) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_FACT_ID, factId.toString());
    data.putString(DATA_FACT_VALUE, value);
    return new ActionDataEntry(UUID.randomUUID(), ActionType.SET_FACT, data);
  }

  public static ActionDataEntry empty(ActionType type) {
    return new ActionDataEntry(UUID.randomUUID(), type, new CompoundTag());
  }

  public static ActionDataEntry load(CompoundTag tag) {
    UUID id = tag.getUUID(TAG_ID);
    ActionType type = ActionType.fromName(tag.getString(TAG_TYPE));
    if (type == null) {
      type = ActionType.NONE;
    }
    CompoundTag data = tag.contains(TAG_DATA) ? tag.getCompound(TAG_DATA) : new CompoundTag();
    return new ActionDataEntry(id, type, data);
  }

  public static ActionDataEntry readFromBuf(FriendlyByteBuf buf) {
    UUID id = buf.readUUID();
    ActionType type = buf.readEnum(ActionType.class);
    CompoundTag data = buf.readNbt();
    return new ActionDataEntry(id, type, data != null ? data : new CompoundTag());
  }

  public ResourceLocation storyId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_STORY_ID));
  }

  public ResourceLocation themeOverrideId() {
    String raw = this.data.getString(DATA_THEME_OVERRIDE_ID);
    return raw.isEmpty() ? null : ResourceLocation.tryParse(raw);
  }

  public String command() {
    return this.data.getString(DATA_COMMAND);
  }

  public ResourceLocation factId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_FACT_ID));
  }

  public String factValue() {
    return this.data.getString(DATA_FACT_VALUE);
  }

  public CompoundTag save() {
    CompoundTag tag = new CompoundTag();
    tag.putUUID(TAG_ID, this.id);
    tag.putString(TAG_TYPE, this.type.name());
    tag.put(TAG_DATA, this.data.copy());
    return tag;
  }

  public void writeToBuf(FriendlyByteBuf buf) {
    buf.writeUUID(this.id);
    buf.writeEnum(this.type);
    buf.writeNbt(this.data);
  }

  public ActionDataEntry withData(CompoundTag updatedData) {
    return new ActionDataEntry(this.id, this.type, updatedData);
  }

  public ActionDataEntry withType(ActionType updatedType) {
    return new ActionDataEntry(this.id, updatedType, new CompoundTag());
  }
}
