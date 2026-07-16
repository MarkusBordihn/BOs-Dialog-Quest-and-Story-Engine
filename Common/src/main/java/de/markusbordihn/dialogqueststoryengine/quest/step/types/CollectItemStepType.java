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

package de.markusbordihn.dialogqueststoryengine.quest.step.types;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.quest.step.QuestStepContext;
import de.markusbordihn.dialogqueststoryengine.registry.QuestStepHandler;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;

public final class CollectItemStepType implements QuestStepHandler {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "collect_item");

  static final String FIELD_ITEM = "item";
  private static final String BASELINE_KEY_PREFIX = "dqse.collect_baseline.";

  public static String baselineKey(ResourceLocation questId, String stepId) {
    return BASELINE_KEY_PREFIX + questId + "." + stepId;
  }

  static int computeProgress(long current, long baseline) {
    return (int) Math.min(Integer.MAX_VALUE, Math.max(0, current - baseline));
  }

  private static Optional<Item> resolveItem(RawQuestStep step) {
    JsonObject jsonObject = step.typeSpecificJson();
    if (!jsonObject.has(FIELD_ITEM) || !jsonObject.get(FIELD_ITEM).isJsonPrimitive()) {
      return Optional.empty();
    }

    ResourceLocation itemId = ResourceLocation.tryParse(jsonObject.get(FIELD_ITEM).getAsString());
    if (itemId == null) {
      return Optional.empty();
    }
    return BuiltInRegistries.ITEM.getOptional(itemId);
  }

  @Override
  public void validate(
      ResourceLocation questId, RawQuestStep step, String filePath, List<ContentIssue> issues) {
    JsonObject jsonObject = step.typeSpecificJson();
    String fieldPath = "logic.steps." + step.id() + "." + FIELD_ITEM;
    if (!jsonObject.has(FIELD_ITEM) || !jsonObject.get(FIELD_ITEM).isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD, ContentType.QUEST, questId, filePath, fieldPath));
      return;
    }

    String item = jsonObject.get(FIELD_ITEM).getAsString();
    if (ResourceLocation.tryParse(item) == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              ContentType.QUEST,
              questId,
              filePath,
              fieldPath,
              Map.of("value", item)));
    }
  }

  @Override
  public void onQuestStarted(QuestStepContext context) {
    Player player = context.player();
    if (player == null) {
      return;
    }

    resolveItem(context.step())
        .ifPresent(
            item ->
                context
                    .playerState()
                    .setFact(
                        FactScope.PLAYER,
                        baselineKey(context.questId(), context.step().id()),
                        FactValue.of((long) player.getInventory().countItem(item))));
  }

  @Override
  public void onQuestCompleted(QuestStepContext context) {
    context
        .playerState()
        .removeFact(FactScope.PLAYER, baselineKey(context.questId(), context.step().id()));
  }

  @Override
  public void onItemPickup(QuestStepContext context) {
    Player player = context.player();
    if (player == null) {
      return;
    }

    resolveItem(context.step())
        .ifPresent(
            item -> {
              long current = player.getInventory().countItem(item);
              long baseline = 0;
              FactValue baselineFact =
                  context
                      .playerState()
                      .getFact(
                          FactScope.PLAYER, baselineKey(context.questId(), context.step().id()));
              if (baselineFact instanceof FactValue.LongValue longValue) {
                baseline = longValue.value();
              }
              context.setProgress(computeProgress(current, baseline));
            });
  }
}
