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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.QuestStateCondition;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ConditionEditorSpecsTest {

  private static final ResourceLocation ID = new ResourceLocation("test", "condition_editor");

  private static JsonObject build(ConditionEditorSpecs.Kind kind, String value1, String value2) {
    String json = ConditionEditorSpecs.get(kind).build().apply(value1, value2);
    return ConditionEditorSpecs.parse(json);
  }

  @Test
  void noneBuildsEmptyAndClassifiesAsNone() {
    assertEquals(
        "", ConditionEditorSpecs.get(ConditionEditorSpecs.Kind.NONE).build().apply("", ""));
    assertEquals(ConditionEditorSpecs.Kind.NONE, ConditionEditorSpecs.kindOf(""));
    assertEquals(ConditionEditorSpecs.Kind.NONE, ConditionEditorSpecs.kindOf("not json"));
  }

  @Test
  void incompleteFieldsBuildEmpty() {
    assertEquals(
        "", ConditionEditorSpecs.get(ConditionEditorSpecs.Kind.FACT_EQUALS).build().apply("", "x"));
    assertEquals(
        "", ConditionEditorSpecs.get(ConditionEditorSpecs.Kind.STORY_READ).build().apply("  ", ""));
  }

  @Test
  void everyBuildableKindRoundTripsThroughClassifyAndRead() {
    for (ConditionEditorSpecs.Kind kind : ConditionEditorSpecs.Kind.values()) {
      if (kind == ConditionEditorSpecs.Kind.NONE) {
        continue;
      }
      ConditionEditorSpecs.Spec spec = ConditionEditorSpecs.get(kind);
      String value1 =
          kind == ConditionEditorSpecs.Kind.HAS_ITEM ? "minecraft:diamond" : "dqse:sample";
      String value2 = spec.field2() != null ? "3" : "";
      if (kind == ConditionEditorSpecs.Kind.FACT_EQUALS) {
        value2 = "true";
      }

      JsonObject json = build(kind, value1, value2);
      assertEquals(kind, ConditionEditorSpecs.kindOf(json.toString()), "classify " + kind);
      assertEquals(value1, spec.read1().apply(json), "read1 " + kind);
      if (spec.field2() != null) {
        assertEquals(value2, spec.read2().apply(json), "read2 " + kind);
      }
    }
  }

  @Test
  void factEqualsJsonParsesWithoutIssuesAndEvaluates() {
    JsonObject json = build(ConditionEditorSpecs.Kind.FACT_EQUALS, "dqse:my_fact", "true");
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition =
        FactEqualsCondition.parse(json, ContentType.INTERACTION, ID, "test", issues);
    assertTrue(issues.isEmpty(), "field names must match the parser: " + issues);

    PlayerState matching = new PlayerState(UUID.randomUUID());
    matching.setFact(FactScope.PLAYER, "dqse:my_fact", FactValue.of("true"));
    assertTrue(condition.evaluate(ConditionContext.ofTest(matching)));

    PlayerState other = new PlayerState(UUID.randomUUID());
    other.setFact(FactScope.PLAYER, "dqse:my_fact", FactValue.of("false"));
    assertFalse(condition.evaluate(ConditionContext.ofTest(other)));
  }

  @Test
  void questCompletedJsonParsesWithoutIssues() {
    JsonObject json = build(ConditionEditorSpecs.Kind.QUEST_COMPLETED, "dqse:first_quest", "");
    assertEquals("completed", json.get("state").getAsString());
    List<ContentIssue> issues = new ArrayList<>();
    QuestStateCondition.parse(json, ContentType.INTERACTION, ID, "test", issues);
    assertTrue(issues.isEmpty(), "field names must match the parser: " + issues);
  }

  @Test
  void actionRoundTripsPriorityAndCondition() {
    String conditionJson =
        ConditionEditorSpecs.get(ConditionEditorSpecs.Kind.FACT_EXISTS).build().apply("dqse:x", "");
    ActionDataEntry action =
        ActionDataEntry.startQuest(new ResourceLocation("dqse", "q"))
            .withConditionAndPriority(conditionJson, 5);

    CompoundTag saved = action.save();
    ActionDataEntry reloaded = ActionDataEntry.load(saved);

    assertEquals(5, reloaded.priority());
    assertEquals(conditionJson, reloaded.condition());
    assertEquals(ActionType.START_QUEST, reloaded.type());
  }
}
