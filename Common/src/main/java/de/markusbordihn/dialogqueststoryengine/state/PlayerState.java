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

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public final class PlayerState {

  private final UUID playerUuid;
  private final EnumMap<FactScope, Map<String, FactValue>> facts;
  private final Map<ResourceLocation, QuestProgress> quests;
  private final StoryProgress stories;
  private ResourceLocation trackedQuestId;
  private volatile boolean dirty;

  public PlayerState(UUID playerUuid) {
    this.playerUuid = playerUuid;
    this.facts = new EnumMap<>(FactScope.class);
    for (FactScope scope : FactScope.values()) {
      this.facts.put(scope, new HashMap<>());
    }
    this.quests = new LinkedHashMap<>();
    this.stories = new StoryProgress();
    this.dirty = false;
  }

  public UUID playerUuid() {
    return this.playerUuid;
  }

  public StoryProgress stories() {
    return this.stories;
  }

  public boolean isDirty() {
    return this.dirty;
  }

  void clearDirty() {
    this.dirty = false;
  }

  public void setFact(FactScope scope, String key, FactValue value) {
    this.facts.get(scope).put(key, value);
    this.markDirty();
  }

  public FactValue getFact(FactScope scope, String key) {
    return this.facts.get(scope).get(key);
  }

  public void removeFact(FactScope scope, String key) {
    if (this.facts.get(scope).remove(key) != null) {
      this.markDirty();
    }
  }

  public boolean hasFact(FactScope scope, String key) {
    return this.facts.get(scope).containsKey(key);
  }

  public Map<String, FactValue> factsForScope(FactScope scope) {
    return Collections.unmodifiableMap(this.facts.get(scope));
  }

  public EnumMap<FactScope, Map<String, FactValue>> allFacts() {
    EnumMap<FactScope, Map<String, FactValue>> snapshot = new EnumMap<>(FactScope.class);
    for (Map.Entry<FactScope, Map<String, FactValue>> entry : this.facts.entrySet()) {
      snapshot.put(entry.getKey(), Collections.unmodifiableMap(entry.getValue()));
    }
    return snapshot;
  }

  public QuestProgress getOrCreateQuest(ResourceLocation questId, QuestState initialState) {
    return this.quests.computeIfAbsent(
        questId,
        ignored -> {
          this.markDirty();
          return new QuestProgress(initialState);
        });
  }

  public QuestProgress getQuest(ResourceLocation questId) {
    return this.quests.get(questId);
  }

  public boolean hasQuest(ResourceLocation questId) {
    return this.quests.containsKey(questId);
  }

  public Map<ResourceLocation, QuestProgress> allQuests() {
    return Collections.unmodifiableMap(this.quests);
  }

  public void putQuestDirect(ResourceLocation questId, QuestProgress questProgress) {
    this.quests.put(questId, questProgress);
    this.markDirty();
  }

  void putQuestFromCodec(ResourceLocation questId, QuestProgress questProgress) {
    this.quests.put(questId, questProgress);
  }

  public ResourceLocation trackedQuestId() {
    return this.trackedQuestId;
  }

  public void setTrackedQuestId(ResourceLocation trackedQuestId) {
    this.trackedQuestId = trackedQuestId;
    this.markDirty();
  }

  public void restoreTrackedQuestId(ResourceLocation trackedQuestId) {
    this.trackedQuestId = trackedQuestId;
  }

  public void unlockStory(ResourceLocation storyId) {
    this.stories.unlock(storyId);
    this.markDirty();
  }

  public void markStoryRead(ResourceLocation storyId) {
    this.stories.markRead(storyId);
    this.markDirty();
  }

  public void markDirty() {
    this.dirty = true;
  }
}
