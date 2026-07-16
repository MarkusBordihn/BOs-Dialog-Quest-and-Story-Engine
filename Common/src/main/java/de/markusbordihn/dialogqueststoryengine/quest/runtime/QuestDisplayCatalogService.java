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

package de.markusbordihn.dialogqueststoryengine.quest.runtime;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestDisplayCatalogEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.QuestStepDisplayEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardDisplayEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.DisplaySection;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardSection;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class QuestDisplayCatalogService {

  private QuestDisplayCatalogService() {}

  public static boolean isVisible(
      ServerPlayer player, PlayerState playerState, QuestDefinition definition) {
    if (playerState.getQuest(definition.id()) != null) {
      return true;
    }
    Optional<Condition> visibility = definition.logic().visibilityCondition();
    if (visibility.isEmpty()) {
      return false;
    }
    ConditionContext context =
        new ConditionContext(player, playerState, player == null ? null : player.server);
    return visibility.get().evaluate(context);
  }

  public static List<QuestDisplayCatalogEntry> visibleCatalog(
      ServerPlayer player, PlayerState playerState) {
    List<QuestDisplayCatalogEntry> entries = new ArrayList<>();
    for (QuestDefinition definition : QuestContentRegistry.all()) {
      if (isVisible(player, playerState, definition)) {
        entries.add(buildEntry(player, playerState, definition));
      }
    }
    return entries;
  }

  public static QuestDisplayCatalogEntry buildEntry(
      ServerPlayer player, PlayerState playerState, QuestDefinition definition) {
    DisplaySection display = definition.display();
    ResourceLocation questId = definition.id();

    List<ResourceLocation> visiblePrerequisites = new ArrayList<>();
    for (ResourceLocation prerequisite : definition.logic().prerequisites().quests()) {
      QuestContentRegistry.get(prerequisite)
          .filter(prereq -> isVisible(player, playerState, prereq))
          .ifPresent(prereq -> visiblePrerequisites.add(prerequisite));
    }

    return new QuestDisplayCatalogEntry(
        questId,
        display.titleKey(),
        display.descriptionKey(),
        display.category(),
        categoryKey(display),
        definition.narrative().arc(),
        definition.narrative().arcKey(),
        definition.narrative().arcOrder(),
        definition.narrative().chapter(),
        definition.narrative().chapterKey(),
        definition.narrative().chapterOrder(),
        display.icon(),
        QuestAvailabilityService.availabilityFor(playerState, definition),
        definition.logic().prerequisites().mode(),
        visiblePrerequisites,
        definition.rewards().titleKey(),
        definition.rewards().descriptionKey(),
        rewardDisplayEntries(definition.rewards()),
        display.sortOrder(),
        stepDisplayEntries(definition));
  }

  private static String categoryKey(DisplaySection display) {
    return display
        .categoryKey()
        .orElseGet(
            () ->
                display
                    .category()
                    .map(id -> "quest_category." + id.getNamespace() + "." + id.getPath())
                    .orElse(""));
  }

  private static List<RewardDisplayEntry> rewardDisplayEntries(RewardSection rewards) {
    List<RewardDisplayEntry> entries = new ArrayList<>();
    for (RewardEntry entry : rewards.entries()) {
      Registries.REWARDS
          .get(entry.type())
          .ifPresent(handler -> entries.add(handler.displayEntry(entry)));
    }
    return entries;
  }

  private static List<QuestStepDisplayEntry> stepDisplayEntries(QuestDefinition definition) {
    List<QuestStepDisplayEntry> entries = new ArrayList<>();
    for (RawQuestStep step : definition.logic().steps().values()) {
      String descriptionKey =
          step.descriptionKey().orElseGet(() -> derivedStepKey(definition.id(), step.id()));
      entries.add(new QuestStepDisplayEntry(step.id(), descriptionKey));
    }
    return entries;
  }

  private static String derivedStepKey(ResourceLocation questId, String stepId) {
    return "quest." + questId.getNamespace() + "." + questId.getPath() + ".step." + stepId;
  }
}
