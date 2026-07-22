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

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.client.screen.ActionEditorSpecs.RefSource;
import java.util.EnumMap;
import java.util.Map;
import java.util.function.BiFunction;
import java.util.function.Function;
import net.minecraft.resources.ResourceLocation;

public final class ConditionEditorSpecs {

  private static final String TYPE = "type";
  private static final Map<Kind, Spec> SPECS = new EnumMap<>(Kind.class);

  static {
    SPECS.put(
        Kind.NONE,
        new Spec(null, null, (value1, value2) -> "", json -> "", json -> "", json -> ""));

    SPECS.put(
        Kind.FACT_EQUALS,
        new Spec(
            id("field.fact_id", "dqse:my_fact"),
            text("field.fact_value", "true", 128),
            (fact, value) ->
                fact.isBlank() || value.isBlank()
                    ? ""
                    : factObject("dqse:fact_equals", fact, value),
            json -> stringValue(json, "fact"),
            json -> stringValue(json, "value"),
            json -> stringValue(json, "fact") + " = " + stringValue(json, "value")));

    SPECS.put(
        Kind.FACT_EXISTS,
        new Spec(
            id("field.fact_id", "dqse:my_fact"),
            null,
            (fact, unused) -> fact.isBlank() ? "" : factObject("dqse:fact_exists", fact, null),
            json -> stringValue(json, "fact"),
            json -> "",
            json -> "has " + stringValue(json, "fact")));

    SPECS.put(
        Kind.HAS_ITEM,
        new Spec(
            ref("field.item_id", RefSource.ITEM),
            text("field.count", "1", 8),
            (item, count) -> {
              ResourceLocation itemId = parseId(item);
              if (itemId == null) {
                return "";
              }
              JsonObject json = typed("dqse:has_item");
              json.addProperty("item", itemId.toString());
              json.addProperty("count", Math.max(1, parseCount(count, 1)));
              return json.toString();
            },
            json -> stringValue(json, "item"),
            json -> json.has("count") ? String.valueOf(json.get("count").getAsInt()) : "1",
            json ->
                stringValue(json, "item")
                    + " x"
                    + (json.has("count") ? json.get("count").getAsInt() : 1)));

    SPECS.put(Kind.QUEST_COMPLETED, questState("completed"));
    SPECS.put(Kind.QUEST_ACTIVE, questState("active"));

    SPECS.put(
        Kind.STORY_READ,
        new Spec(
            ref("field.story_id", RefSource.STORY),
            null,
            (story, unused) -> {
              ResourceLocation storyId = parseId(story);
              if (storyId == null) {
                return "";
              }
              JsonObject json = typed("dqse:story_read");
              json.addProperty("story", storyId.toString());
              return json.toString();
            },
            json -> stringValue(json, "story"),
            json -> "",
            json -> "read " + stringValue(json, "story")));
  }

  private ConditionEditorSpecs() {}

  public static Spec get(Kind kind) {
    return SPECS.get(kind);
  }

  public static Kind kindOf(String conditionJson) {
    JsonObject json = parse(conditionJson);
    if (json == null || !json.has(TYPE) || !json.get(TYPE).isJsonPrimitive()) {
      return Kind.NONE;
    }

    return switch (json.get(TYPE).getAsString()) {
      case "dqse:fact_equals" -> Kind.FACT_EQUALS;
      case "dqse:fact_exists" -> Kind.FACT_EXISTS;
      case "dqse:has_item" -> Kind.HAS_ITEM;
      case "dqse:story_read" -> Kind.STORY_READ;
      case "dqse:quest_state" ->
          "completed".equalsIgnoreCase(stringValue(json, "state"))
              ? Kind.QUEST_COMPLETED
              : Kind.QUEST_ACTIVE;
      default -> Kind.NONE;
    };
  }

  public static JsonObject parse(String conditionJson) {
    if (conditionJson == null || conditionJson.isBlank()) {
      return null;
    }

    try {
      return JsonParser.parseString(conditionJson).getAsJsonObject();
    } catch (RuntimeException exception) {
      return null;
    }
  }

  private static Spec questState(String state) {
    return new Spec(
        ref("field.quest_id", RefSource.QUEST),
        null,
        (quest, unused) -> {
          ResourceLocation questId = parseId(quest);
          if (questId == null) {
            return "";
          }
          JsonObject json = typed("dqse:quest_state");
          json.addProperty("quest", questId.toString());
          json.addProperty("state", state);
          return json.toString();
        },
        json -> stringValue(json, "quest"),
        json -> "",
        json -> stringValue(json, "quest") + " " + state);
  }

  private static JsonObject typed(String type) {
    JsonObject json = new JsonObject();
    json.addProperty(TYPE, type);
    return json;
  }

  private static String factObject(String type, String fact, String value) {
    JsonObject json = typed(type);
    json.addProperty("scope", "player");
    json.addProperty("fact", fact.trim());
    if (value != null) {
      json.addProperty("value", value.trim());
    }
    return json.toString();
  }

  private static String stringValue(JsonObject json, String field) {
    return json != null && json.has(field) && json.get(field).isJsonPrimitive()
        ? json.get(field).getAsString()
        : "";
  }

  private static ResourceLocation parseId(String value) {
    String trimmed = value.trim();
    return trimmed.isEmpty() ? null : ResourceLocation.tryParse(trimmed);
  }

  private static int parseCount(String value, int fallback) {
    try {
      return Integer.parseInt(value.trim());
    } catch (NumberFormatException exception) {
      return fallback;
    }
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

  public enum Kind {
    NONE("condition.none"),
    FACT_EQUALS("condition.fact_equals"),
    FACT_EXISTS("condition.fact_exists"),
    HAS_ITEM("condition.has_item"),
    QUEST_COMPLETED("condition.quest_completed"),
    QUEST_ACTIVE("condition.quest_active"),
    STORY_READ("condition.story_read");

    private final String labelKey;

    Kind(String labelKey) {
      this.labelKey = labelKey;
    }

    public String labelKey() {
      return this.labelKey;
    }
  }

  public record Field(String labelKey, String suggestion, int maxLength, RefSource ref) {}

  public record Spec(
      Field field1,
      Field field2,
      BiFunction<String, String, String> build,
      Function<JsonObject, String> read1,
      Function<JsonObject, String> read2,
      Function<JsonObject, String> summary) {}
}
