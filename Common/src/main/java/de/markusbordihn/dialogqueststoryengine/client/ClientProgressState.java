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

package de.markusbordihn.dialogqueststoryengine.client;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestDisplayCatalogEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestState;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardClaimState;
import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDisplayCatalogRemovePacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDisplayCatalogResetPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.QuestDisplayCatalogUpsertPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.StoryDeltaPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.TrackedQuestPacket;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ClientProgressState {

  private static final Map<ResourceLocation, ClientQuestProgress> quests = new HashMap<>();
  private static final Map<ResourceLocation, QuestDisplayCatalogEntry> catalog = new HashMap<>();
  private static final Set<ResourceLocation> unlockedStories = new HashSet<>();
  private static final Set<ResourceLocation> readStories = new HashSet<>();
  private static Optional<ResourceLocation> trackedQuestId = Optional.empty();

  private ClientProgressState() {}

  public static void apply(QuestDeltaPacket packet) {
    ClientQuestProgress current = quests.get(packet.questId());
    if (current != null && packet.revision() <= current.revision()) {
      return;
    }
    Map<String, StepProgress> steps =
        current == null ? new HashMap<>() : new HashMap<>(current.steps());
    steps.putAll(packet.changedSteps());
    quests.put(
        packet.questId(),
        new ClientQuestProgress(
            packet.questState(), Map.copyOf(steps), packet.rewardClaimState(), packet.revision()));
  }

  public static void apply(StoryDeltaPacket packet) {
    unlockedStories.addAll(packet.unlockedStoryIds());
    readStories.addAll(packet.readStoryIds());
  }

  public static void apply(TrackedQuestPacket packet) {
    trackedQuestId = packet.trackedQuestId();
  }

  public static void apply(QuestDisplayCatalogResetPacket packet) {
    catalog.clear();
    for (QuestDisplayCatalogEntry entry : packet.entries()) {
      catalog.put(entry.questId(), entry);
    }
  }

  public static void apply(QuestDisplayCatalogUpsertPacket packet) {
    catalog.put(packet.entry().questId(), packet.entry());
  }

  public static void apply(QuestDisplayCatalogRemovePacket packet) {
    catalog.remove(packet.questId());
  }

  public static Map<ResourceLocation, QuestDisplayCatalogEntry> catalog() {
    return Map.copyOf(catalog);
  }

  public static Optional<ResourceLocation> trackedQuestId() {
    return trackedQuestId;
  }

  public static Map<ResourceLocation, ClientQuestProgress> quests() {
    return Map.copyOf(quests);
  }

  public static Set<ResourceLocation> unlockedStories() {
    return Set.copyOf(unlockedStories);
  }

  public static Set<ResourceLocation> readStories() {
    return Set.copyOf(readStories);
  }

  public static void clear() {
    quests.clear();
    catalog.clear();
    unlockedStories.clear();
    readStories.clear();
    trackedQuestId = Optional.empty();
  }

  public record ClientQuestProgress(
      QuestState state,
      Map<String, StepProgress> steps,
      RewardClaimState rewardClaimState,
      int revision) {}
}
