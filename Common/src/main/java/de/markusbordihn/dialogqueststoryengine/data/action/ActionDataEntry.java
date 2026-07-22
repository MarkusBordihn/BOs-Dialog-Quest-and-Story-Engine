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

public record ActionDataEntry(
    UUID id, ActionType type, CompoundTag data, int priority, String condition) {

  private static final String TAG_ID = "Id";
  private static final String TAG_TYPE = "Type";
  private static final String TAG_DATA = "Data";
  private static final String TAG_PRIORITY = "Priority";
  private static final String TAG_CONDITION = "Condition";
  private static final String DATA_STORY_ID = "StoryId";
  private static final String DATA_THEME_OVERRIDE_ID = "ThemeOverrideId";
  private static final String DATA_DIALOG_ID = "DialogId";
  private static final String DATA_QUEST_ID = "QuestId";
  private static final String DATA_STEP_ID = "StepId";
  private static final String DATA_COMMAND = "Command";
  private static final String DATA_FUNCTION_ID = "FunctionId";
  private static final String DATA_FACT_ID = "FactId";
  private static final String DATA_FACT_VALUE = "FactValue";
  private static final String DATA_ITEM_ID = "ItemId";
  private static final String DATA_COUNT = "Count";
  private static final String DATA_AMOUNT = "Amount";
  private static final String DATA_MESSAGE = "Message";
  private static final String DATA_SPEAKER = "Speaker";

  public ActionDataEntry(UUID id, ActionType type, CompoundTag data) {
    this(id, type, data, 0, "");
  }

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

  public static ActionDataEntry openDialog(ResourceLocation dialogId) {
    return withResource(ActionType.OPEN_DIALOG, DATA_DIALOG_ID, dialogId);
  }

  public static ActionDataEntry startQuest(ResourceLocation questId) {
    return withResource(ActionType.START_QUEST, DATA_QUEST_ID, questId);
  }

  public static ActionDataEntry completeQuest(ResourceLocation questId) {
    return withResource(ActionType.COMPLETE_QUEST, DATA_QUEST_ID, questId);
  }

  public static ActionDataEntry failQuest(ResourceLocation questId) {
    return withResource(ActionType.FAIL_QUEST, DATA_QUEST_ID, questId);
  }

  public static ActionDataEntry advanceQuestStep(ResourceLocation questId, String stepId) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_QUEST_ID, questId.toString());
    data.putString(DATA_STEP_ID, stepId);
    return new ActionDataEntry(UUID.randomUUID(), ActionType.ADVANCE_QUEST_STEP, data);
  }

  public static ActionDataEntry unlockStory(ResourceLocation storyId) {
    return withResource(ActionType.UNLOCK_STORY, DATA_STORY_ID, storyId);
  }

  public static ActionDataEntry markStoryRead(ResourceLocation storyId) {
    return withResource(ActionType.MARK_STORY_READ, DATA_STORY_ID, storyId);
  }

  public static ActionDataEntry removeFact(ResourceLocation factId) {
    return withResource(ActionType.REMOVE_FACT, DATA_FACT_ID, factId);
  }

  public static ActionDataEntry giveItem(ResourceLocation itemId, int count) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_ITEM_ID, itemId.toString());
    data.putInt(DATA_COUNT, count);
    return new ActionDataEntry(UUID.randomUUID(), ActionType.GIVE_ITEM, data);
  }

  public static ActionDataEntry giveExperience(int amount) {
    CompoundTag data = new CompoundTag();
    data.putInt(DATA_AMOUNT, amount);
    return new ActionDataEntry(UUID.randomUUID(), ActionType.GIVE_EXPERIENCE, data);
  }

  public static ActionDataEntry runFunction(ResourceLocation functionId) {
    return withResource(ActionType.RUN_FUNCTION, DATA_FUNCTION_ID, functionId);
  }

  private static ActionDataEntry withResource(ActionType type, String key, ResourceLocation value) {
    CompoundTag data = new CompoundTag();
    data.putString(key, value.toString());
    return new ActionDataEntry(UUID.randomUUID(), type, data);
  }

  public static ActionDataEntry runCommand(String command) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_COMMAND, command);
    return new ActionDataEntry(UUID.randomUUID(), ActionType.RUN_COMMAND, data);
  }

  public static ActionDataEntry sendMessage(String message, String speaker) {
    CompoundTag data = new CompoundTag();
    data.putString(DATA_MESSAGE, message);
    if (speaker != null && !speaker.isBlank()) {
      data.putString(DATA_SPEAKER, speaker.trim());
    }
    return new ActionDataEntry(UUID.randomUUID(), ActionType.SEND_MESSAGE, data);
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
    CompoundTag data = tag.contains(TAG_DATA) ? tag.getCompound(TAG_DATA) : new CompoundTag();
    return new ActionDataEntry(
        id, type, data, tag.getInt(TAG_PRIORITY), tag.getString(TAG_CONDITION));
  }

  public static ActionDataEntry readFromBuffer(FriendlyByteBuf buffer) {
    UUID id = buffer.readUUID();
    ActionType type = buffer.readEnum(ActionType.class);
    CompoundTag data = buffer.readNbt();
    int priority = buffer.readInt();
    String condition = buffer.readUtf();
    return new ActionDataEntry(
        id, type, data != null ? data : new CompoundTag(), priority, condition);
  }

  public ResourceLocation storyId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_STORY_ID));
  }

  public ResourceLocation themeOverrideId() {
    String raw = this.data.getString(DATA_THEME_OVERRIDE_ID);
    return raw.isEmpty() ? null : ResourceLocation.tryParse(raw);
  }

  public ResourceLocation dialogId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_DIALOG_ID));
  }

  public ResourceLocation questId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_QUEST_ID));
  }

  public String stepId() {
    return this.data.getString(DATA_STEP_ID);
  }

  public ResourceLocation functionId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_FUNCTION_ID));
  }

  public ResourceLocation itemId() {
    return ResourceLocation.tryParse(this.data.getString(DATA_ITEM_ID));
  }

  public int count() {
    return this.data.contains(DATA_COUNT) ? this.data.getInt(DATA_COUNT) : 1;
  }

  public int amount() {
    return this.data.getInt(DATA_AMOUNT);
  }

  public String command() {
    return this.data.getString(DATA_COMMAND);
  }

  public String message() {
    return this.data.getString(DATA_MESSAGE);
  }

  public String speaker() {
    return this.data.getString(DATA_SPEAKER);
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
    if (this.priority != 0) {
      tag.putInt(TAG_PRIORITY, this.priority);
    }
    if (!this.condition.isEmpty()) {
      tag.putString(TAG_CONDITION, this.condition);
    }
    return tag;
  }

  public void writeToBuffer(FriendlyByteBuf buffer) {
    buffer.writeUUID(this.id);
    buffer.writeEnum(this.type);
    buffer.writeNbt(this.data);
    buffer.writeInt(this.priority);
    buffer.writeUtf(this.condition);
  }

  public ActionDataEntry withData(CompoundTag updatedData) {
    return new ActionDataEntry(this.id, this.type, updatedData, this.priority, this.condition);
  }

  public ActionDataEntry withType(ActionType updatedType) {
    return new ActionDataEntry(
        this.id, updatedType, new CompoundTag(), this.priority, this.condition);
  }

  public ActionDataEntry withConditionAndPriority(String updatedCondition, int updatedPriority) {
    return new ActionDataEntry(
        this.id,
        this.type,
        this.data,
        updatedPriority,
        updatedCondition == null ? "" : updatedCondition);
  }
}
