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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import org.junit.jupiter.api.Test;

class ActionEditorSpecsTest {

  @Test
  void everyActionTypeExceptNoneIsEditable() {
    for (ActionType type : ActionType.values()) {
      if (type == ActionType.NONE) {
        continue;
      }
      assertNotNull(
          ActionEditorSpecs.get(type),
          "Action type has no editor spec (GUI cannot configure it): " + type);
    }
  }

  @Test
  void singleFieldActionRoundTrips() {
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(ActionType.START_QUEST);
    ActionDataEntry action = spec.build().apply("dqse:my_quest", "");

    assertNotNull(action);
    assertEquals(ActionType.START_QUEST, action.type());
    assertEquals("dqse:my_quest", action.questId().toString());
    assertEquals("dqse:my_quest", spec.read1().apply(action));
  }

  @Test
  void twoFieldActionRoundTrips() {
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(ActionType.GIVE_ITEM);
    ActionDataEntry action = spec.build().apply("minecraft:emerald", "4");

    assertNotNull(action);
    assertEquals("minecraft:emerald", action.itemId().toString());
    assertEquals(4, action.count());
    assertEquals("minecraft:emerald", spec.read1().apply(action));
    assertEquals("4", spec.read2().apply(action));
  }

  @Test
  void invalidInputYieldsNoAction() {
    ActionEditorSpecs.Spec spec = ActionEditorSpecs.get(ActionType.SET_FACT);
    assertNull(spec.build().apply("dqse:fact", ""), "set_fact needs a value");
    assertNull(spec.build().apply("", "true"), "set_fact needs a fact id");
  }
}
