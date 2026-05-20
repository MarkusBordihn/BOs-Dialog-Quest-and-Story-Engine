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

package de.markusbordihn.dialogqueststoryengine.state;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.PlayerStateSchema;
import de.markusbordihn.dialogqueststoryengine.migration.PlayerStateMigrationRegistry;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class PlayerStateCodec {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private static final String TAG_ROOT = Constants.MOD_ID;
  private static final String TAG_SCHEMA = "schema";
  private static final String TAG_PLAYER_UUID = "player_uuid";
  private static final String TAG_FACTS = "facts";
  private static final String TAG_SCOPE = "scope";
  private static final String TAG_KEY = "key";
  private static final String TAG_QUESTS = "quests";
  private static final String TAG_QUEST_ID = "quest_id";
  private static final String TAG_QUEST_STATE = "quest_state";
  private static final String TAG_QUEST_REVISION = "quest_revision";
  private static final String TAG_STEPS = "steps";
  private static final String TAG_STEP_ID = "step_id";
  private static final String TAG_STEP_STATE = "step_state";
  private static final String TAG_STEP_PROGRESS = "step_progress";
  private static final String TAG_STEP_REQUIRED = "step_required";
  private static final String TAG_STORY_ID = "story_id";
  private static final String TAG_STORIES = "stories";
  private static final String TAG_UNLOCKED = "unlocked";
  private static final String TAG_READ = "read";

  private PlayerStateCodec() {}

  public static CompoundTag toNbt(PlayerState playerState) {
    CompoundTag root = new CompoundTag();
    CompoundTag data = new CompoundTag();

    data.putInt(TAG_SCHEMA, PlayerStateSchema.CURRENT);
    data.putString(TAG_PLAYER_UUID, playerState.playerUuid().toString());
    data.put(TAG_FACTS, encodeFacts(playerState));
    data.put(TAG_QUESTS, encodeQuests(playerState));
    data.put(TAG_STORIES, encodeStories(playerState));

    root.put(TAG_ROOT, data);
    return root;
  }

  public static PlayerState fromNbt(CompoundTag nbt, UUID playerUuid) {
    try {
      CompoundTag data = resolveDataTag(nbt);
      if (data == null) {
        return new PlayerState(playerUuid);
      }

      int schema = data.getInt(TAG_SCHEMA);
      CompoundTag migrated = applyMigrations(data, schema);

      PlayerState playerState = new PlayerState(playerUuid);
      decodeFacts(migrated, playerState);
      decodeQuests(migrated, playerState);
      decodeStories(migrated, playerState);
      playerState.clearDirty();
      return playerState;
    } catch (Exception exception) {
      log.warn(
          "{} Corrupt player state for UUID {} — resetting to empty state. Cause: {}",
          Constants.LOG_PREFIX,
          playerUuid,
          exception.getMessage());
      return new PlayerState(playerUuid);
    }
  }

  private static CompoundTag resolveDataTag(CompoundTag nbt) {
    if (nbt == null) {
      return null;
    }
    if (nbt.contains(TAG_ROOT, Tag.TAG_COMPOUND)) {
      return nbt.getCompound(TAG_ROOT);
    }
    if (nbt.contains(TAG_SCHEMA)) {
      return nbt;
    }
    return null;
  }

  private static CompoundTag applyMigrations(CompoundTag data, int schema) {
    if (schema >= PlayerStateSchema.CURRENT) {
      return data;
    }
    for (var migration : PlayerStateMigrationRegistry.INSTANCE.getMigrations()) {
      if (migration.sourceSchema() == schema) {
        data = nbtFromJson(migration.migrate(jsonFromNbt(data)));
        schema = migration.targetSchema();
      }
    }
    return data;
  }

  private static ListTag encodeFacts(PlayerState playerState) {
    ListTag factsList = new ListTag();
    for (FactScope scope : FactScope.values()) {
      for (Map.Entry<String, FactValue> entry : playerState.factsForScope(scope).entrySet()) {
        CompoundTag factTag = new CompoundTag();
        factTag.putString(TAG_SCOPE, scope.name());
        factTag.putString(TAG_KEY, entry.getKey());
        factTag.put(FactValue.TAG_VALUE, FactValue.toNbt(entry.getValue()));
        factsList.add(factTag);
      }
    }
    return factsList;
  }

  private static void decodeFacts(CompoundTag data, PlayerState playerState) {
    if (!data.contains(TAG_FACTS, Tag.TAG_LIST)) {
      return;
    }
    ListTag factsList = data.getList(TAG_FACTS, Tag.TAG_COMPOUND);
    for (int i = 0; i < factsList.size(); i++) {
      CompoundTag factTag = factsList.getCompound(i);
      FactScope scope = FactScope.fromName(factTag.getString(TAG_SCOPE));
      if (scope == null) {
        continue;
      }
      String key = factTag.getString(TAG_KEY);
      if (key.isEmpty() || !factTag.contains(FactValue.TAG_VALUE, Tag.TAG_COMPOUND)) {
        continue;
      }
      FactValue factValue = FactValue.fromNbt(factTag.getCompound(FactValue.TAG_VALUE));
      if (factValue != null) {
        playerState.setFact(scope, key, factValue);
      }
    }
  }

  private static ListTag encodeQuests(PlayerState playerState) {
    ListTag questsList = new ListTag();
    for (Map.Entry<ResourceLocation, QuestProgress> entry : playerState.allQuests().entrySet()) {
      CompoundTag questTag = new CompoundTag();
      questTag.putString(TAG_QUEST_ID, entry.getKey().toString());
      QuestProgress questProgress = entry.getValue();
      questTag.putString(TAG_QUEST_STATE, questProgress.state().name());
      questTag.putInt(TAG_QUEST_REVISION, questProgress.revision());
      questTag.put(TAG_STEPS, encodeSteps(questProgress));
      questsList.add(questTag);
    }
    return questsList;
  }

  private static ListTag encodeSteps(QuestProgress questProgress) {
    ListTag stepsList = new ListTag();
    for (Map.Entry<String, StepProgress> entry : questProgress.steps().entrySet()) {
      CompoundTag stepTag = new CompoundTag();
      stepTag.putString(TAG_STEP_ID, entry.getKey());
      StepProgress stepProgress = entry.getValue();
      stepTag.putString(TAG_STEP_STATE, stepProgress.state().name());
      stepTag.putInt(TAG_STEP_PROGRESS, stepProgress.progress());
      stepTag.putInt(TAG_STEP_REQUIRED, stepProgress.required());
      stepsList.add(stepTag);
    }
    return stepsList;
  }

  private static void decodeQuests(CompoundTag data, PlayerState playerState) {
    if (!data.contains(TAG_QUESTS, Tag.TAG_LIST)) {
      return;
    }
    ListTag questsList = data.getList(TAG_QUESTS, Tag.TAG_COMPOUND);
    for (int i = 0; i < questsList.size(); i++) {
      CompoundTag questTag = questsList.getCompound(i);
      ResourceLocation questId = ResourceLocation.tryParse(questTag.getString(TAG_QUEST_ID));
      if (questId == null) {
        continue;
      }
      QuestState questState = QuestState.fromName(questTag.getString(TAG_QUEST_STATE));
      if (questState == null) {
        questState = QuestState.NOT_STARTED;
      }
      int revision = questTag.getInt(TAG_QUEST_REVISION);
      Map<String, StepProgress> steps = decodeSteps(questTag);
      playerState.putQuestFromCodec(questId, new QuestProgress(questState, revision, steps));
    }
  }

  private static Map<String, StepProgress> decodeSteps(CompoundTag questTag) {
    Map<String, StepProgress> steps = new LinkedHashMap<>();
    if (!questTag.contains(TAG_STEPS, Tag.TAG_LIST)) {
      return steps;
    }
    ListTag stepsList = questTag.getList(TAG_STEPS, Tag.TAG_COMPOUND);
    for (int i = 0; i < stepsList.size(); i++) {
      CompoundTag stepTag = stepsList.getCompound(i);
      String stepId = stepTag.getString(TAG_STEP_ID);
      if (stepId.isEmpty()) {
        continue;
      }
      StepState stepState = StepState.fromName(stepTag.getString(TAG_STEP_STATE));
      if (stepState == null) {
        stepState = StepState.LOCKED;
      }
      int progress = stepTag.getInt(TAG_STEP_PROGRESS);
      int required = stepTag.getInt(TAG_STEP_REQUIRED);
      steps.put(stepId, new StepProgress(stepState, progress, required));
    }
    return steps;
  }

  private static CompoundTag encodeStories(PlayerState playerState) {
    CompoundTag storiesTag = new CompoundTag();
    ListTag unlockedList = new ListTag();
    for (ResourceLocation storyId : playerState.stories().unlockedIds()) {
      CompoundTag entry = new CompoundTag();
      entry.putString(TAG_STORY_ID, storyId.toString());
      unlockedList.add(entry);
    }
    storiesTag.put(TAG_UNLOCKED, unlockedList);

    ListTag readList = new ListTag();
    for (ResourceLocation storyId : playerState.stories().readIds()) {
      CompoundTag entry = new CompoundTag();
      entry.putString(TAG_STORY_ID, storyId.toString());
      readList.add(entry);
    }
    storiesTag.put(TAG_READ, readList);
    return storiesTag;
  }

  private static void decodeStories(CompoundTag data, PlayerState playerState) {
    if (!data.contains(TAG_STORIES, Tag.TAG_COMPOUND)) {
      return;
    }
    CompoundTag storiesTag = data.getCompound(TAG_STORIES);

    if (storiesTag.contains(TAG_UNLOCKED, Tag.TAG_LIST)) {
      ListTag unlockedList = storiesTag.getList(TAG_UNLOCKED, Tag.TAG_COMPOUND);
      for (int i = 0; i < unlockedList.size(); i++) {
        ResourceLocation storyId =
            ResourceLocation.tryParse(unlockedList.getCompound(i).getString(TAG_STORY_ID));
        if (storyId != null) {
          playerState.unlockStory(storyId);
        }
      }
    }

    if (storiesTag.contains(TAG_READ, Tag.TAG_LIST)) {
      ListTag readList = storiesTag.getList(TAG_READ, Tag.TAG_COMPOUND);
      for (int i = 0; i < readList.size(); i++) {
        ResourceLocation storyId =
            ResourceLocation.tryParse(readList.getCompound(i).getString(TAG_STORY_ID));
        if (storyId != null) {
          playerState.markStoryRead(storyId);
        }
      }
    }
  }

  private static com.google.gson.JsonObject jsonFromNbt(CompoundTag tag) {
    com.google.gson.JsonObject jsonObject = new com.google.gson.JsonObject();
    for (String key : tag.getAllKeys()) {
      Tag nbtValue = tag.get(key);
      if (nbtValue instanceof net.minecraft.nbt.StringTag stringTag) {
        jsonObject.addProperty(key, stringTag.getAsString());
      } else if (nbtValue instanceof net.minecraft.nbt.IntTag intTag) {
        jsonObject.addProperty(key, intTag.getAsInt());
      }
    }
    return jsonObject;
  }

  private static CompoundTag nbtFromJson(com.google.gson.JsonObject jsonObject) {
    CompoundTag tag = new CompoundTag();
    for (Map.Entry<String, com.google.gson.JsonElement> entry : jsonObject.entrySet()) {
      com.google.gson.JsonElement element = entry.getValue();
      if (element.isJsonPrimitive()) {
        com.google.gson.JsonPrimitive primitive = element.getAsJsonPrimitive();
        if (primitive.isString()) {
          tag.putString(entry.getKey(), primitive.getAsString());
        } else if (primitive.isNumber()) {
          tag.putInt(entry.getKey(), primitive.getAsInt());
        }
      }
    }
    return tag;
  }
}
