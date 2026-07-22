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

package de.markusbordihn.dialogqueststoryengine.client.screen;

import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public final class ActionEditorSpecs {

  private static final Map<ActionType, Spec> SPECS = new EnumMap<>(ActionType.class);

  static {
    SPECS.put(
        ActionType.OPEN_STORY,
        new Spec(
            ref("field.story_id", RefSource.STORY),
            text("field.theme_override", "(optional)", 200),
            (value1, value2) -> {
              ResourceLocation storyId = parseId(value1);
              return storyId == null
                  ? null
                  : ActionDataEntry.openStory(storyId, value2.isBlank() ? null : parseId(value2));
            },
            action -> asString(action.storyId()),
            action -> asString(action.themeOverrideId()),
            action -> asString(action.storyId())));

    SPECS.put(
        ActionType.OPEN_INTERACTIVE_STORY,
        single(
            ref("field.story_id", RefSource.STORY),
            ActionDataEntry::openInteractiveStory,
            ActionDataEntry::storyId));

    SPECS.put(
        ActionType.OPEN_DIALOG,
        single(
            ref("field.dialog_id", RefSource.DIALOG),
            ActionDataEntry::openDialog,
            ActionDataEntry::dialogId));

    SPECS.put(
        ActionType.START_QUEST,
        single(
            ref("field.quest_id", RefSource.QUEST),
            ActionDataEntry::startQuest,
            ActionDataEntry::questId));

    SPECS.put(
        ActionType.COMPLETE_QUEST,
        single(
            ref("field.quest_id", RefSource.QUEST),
            ActionDataEntry::completeQuest,
            ActionDataEntry::questId));

    SPECS.put(
        ActionType.FAIL_QUEST,
        single(
            ref("field.quest_id", RefSource.QUEST),
            ActionDataEntry::failQuest,
            ActionDataEntry::questId));

    SPECS.put(
        ActionType.ADVANCE_QUEST_STEP,
        new Spec(
            ref("field.quest_id", RefSource.QUEST),
            text("field.step_id", "step_a", 128),
            (value1, value2) -> {
              ResourceLocation questId = parseId(value1);
              return questId == null || value2.isBlank()
                  ? null
                  : ActionDataEntry.advanceQuestStep(questId, value2.trim());
            },
            action -> asString(action.questId()),
            ActionDataEntry::stepId,
            action -> asString(action.questId()) + " / " + action.stepId()));

    SPECS.put(
        ActionType.UNLOCK_STORY,
        single(
            ref("field.story_id", RefSource.STORY),
            ActionDataEntry::unlockStory,
            ActionDataEntry::storyId));

    SPECS.put(
        ActionType.MARK_STORY_READ,
        single(
            ref("field.story_id", RefSource.STORY),
            ActionDataEntry::markStoryRead,
            ActionDataEntry::storyId));

    SPECS.put(
        ActionType.SET_FACT,
        new Spec(
            id("field.fact_id", "dqse:my_fact"),
            text("field.fact_value", "true", 128),
            (value1, value2) -> {
              ResourceLocation factId = parseId(value1);
              return factId == null || value2.isBlank()
                  ? null
                  : ActionDataEntry.setFact(factId, value2.trim());
            },
            action -> asString(action.factId()),
            ActionDataEntry::factValue,
            action -> asString(action.factId()) + " = " + action.factValue()));

    SPECS.put(
        ActionType.REMOVE_FACT,
        single(
            id("field.fact_id", "dqse:my_fact"),
            ActionDataEntry::removeFact,
            ActionDataEntry::factId));

    SPECS.put(
        ActionType.GIVE_ITEM,
        new Spec(
            ref("field.item_id", RefSource.ITEM),
            text("field.count", "1", 8),
            (value1, value2) -> {
              ResourceLocation itemId = parseId(value1);
              return itemId == null
                  ? null
                  : ActionDataEntry.giveItem(itemId, Math.max(1, parseCount(value2, 1)));
            },
            action -> asString(action.itemId()),
            action -> String.valueOf(action.count()),
            action -> asString(action.itemId()) + " x" + action.count()));

    SPECS.put(
        ActionType.GIVE_EXPERIENCE,
        new Spec(
            text("field.xp_amount", "100", 8),
            null,
            (value1, value2) -> {
              int amount = parseCount(value1, 0);
              return amount > 0 ? ActionDataEntry.giveExperience(amount) : null;
            },
            action -> String.valueOf(action.amount()),
            action -> "",
            action -> action.amount() + " xp"));

    SPECS.put(
        ActionType.RUN_COMMAND,
        new Spec(
            text("field.command", "/say hello", 256),
            null,
            (value1, value2) -> value1.isBlank() ? null : ActionDataEntry.runCommand(value1.trim()),
            ActionDataEntry::command,
            action -> "",
            ActionDataEntry::command));

    SPECS.put(
        ActionType.RUN_FUNCTION,
        single(
            id("field.function_id", "dqse:my_function"),
            ActionDataEntry::runFunction,
            ActionDataEntry::functionId));

    SPECS.put(
        ActionType.SEND_MESSAGE,
        new Spec(
            text("field.message", "Hello there!", 256),
            text("field.speaker", "(blank=system, @entity, name)", 128),
            (value1, value2) ->
                value1.isBlank() ? null : ActionDataEntry.sendMessage(value1.trim(), value2.trim()),
            ActionDataEntry::message,
            ActionDataEntry::speaker,
            action ->
                action.speaker().isBlank()
                    ? action.message()
                    : "<" + action.speaker() + "> " + action.message()));
  }

  private ActionEditorSpecs() {}

  public static Spec get(ActionType type) {
    return SPECS.get(type);
  }

  private static Field id(String labelKey, String suggestion) {
    return new Field(labelKey, suggestion, 200, RefSource.NONE);
  }

  private static Field ref(String labelKey, RefSource source) {
    return new Field(labelKey, "", 200, source);
  }

  private static Field text(String labelKey, String suggestion, int maxLength) {
    return new Field(labelKey, suggestion, maxLength, RefSource.NONE);
  }

  private static ResourceLocation parseId(String value) {
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : ResourceLocation.tryParse(trimmed);
  }

  private static int parseCount(String value, int fallback) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException e) {
      return fallback;
    }
  }

  private static String asString(ResourceLocation value) {
    return value != null ? value.toString() : "";
  }

  private static Spec single(
      Field field,
      Function<ResourceLocation, ActionDataEntry> factory,
      Function<ActionDataEntry, ResourceLocation> reader) {
    return new Spec(
        field,
        null,
        (value1, value2) -> {
          ResourceLocation value = parseId(value1);
          return value == null ? null : factory.apply(value);
        },
        action -> asString(reader.apply(action)),
        action -> "",
        action -> asString(reader.apply(action)));
  }

  public enum RefSource {
    NONE,
    STORY,
    DIALOG,
    QUEST,
    THEME,
    ITEM
  }

  public record Field(String labelKey, String suggestion, int maxLength, RefSource ref) {}

  public record Spec(
      Field field1,
      Field field2,
      BiFunction<String, String, ActionDataEntry> build,
      Function<ActionDataEntry, String> read1,
      Function<ActionDataEntry, String> read2,
      Function<ActionDataEntry, String> summary) {}
}
