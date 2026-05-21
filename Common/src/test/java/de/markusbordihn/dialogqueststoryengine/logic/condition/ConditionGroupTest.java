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

package de.markusbordihn.dialogqueststoryengine.logic.condition;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConditionGroupTest {

  private static ConditionContext emptyContext() {
    return ConditionContext.ofTest(new PlayerState(UUID.randomUUID()));
  }

  @Test
  void allOperator_emptyMembers_returnsTrue() {
    ConditionGroup group = new ConditionGroup(GroupOperator.ALL, List.of());
    assertTrue(group.evaluate(emptyContext()));
  }

  @Test
  void anyOperator_emptyMembers_returnsFalse() {
    ConditionGroup group = new ConditionGroup(GroupOperator.ANY, List.of());
    assertFalse(group.evaluate(emptyContext()));
  }

  @Test
  void allOperator_allTrue_returnsTrue() {
    ConditionGroup group =
        new ConditionGroup(GroupOperator.ALL, List.of(ctx -> true, ctx -> true));
    assertTrue(group.evaluate(emptyContext()));
  }

  @Test
  void allOperator_oneFalse_returnsFalse() {
    ConditionGroup group =
        new ConditionGroup(GroupOperator.ALL, List.of(ctx -> true, ctx -> false, ctx -> true));
    assertFalse(group.evaluate(emptyContext()));
  }

  @Test
  void anyOperator_oneTrue_returnsTrue() {
    ConditionGroup group =
        new ConditionGroup(GroupOperator.ANY, List.of(ctx -> false, ctx -> true, ctx -> false));
    assertTrue(group.evaluate(emptyContext()));
  }

  @Test
  void anyOperator_allFalse_returnsFalse() {
    ConditionGroup group =
        new ConditionGroup(GroupOperator.ANY, List.of(ctx -> false, ctx -> false));
    assertFalse(group.evaluate(emptyContext()));
  }

  @Test
  void nestedGroups_evaluate_correctly() {
    Condition inner = new ConditionGroup(GroupOperator.ANY, List.of(ctx -> false, ctx -> true));
    ConditionGroup outer = new ConditionGroup(GroupOperator.ALL, List.of(ctx -> true, inner));
    assertTrue(outer.evaluate(emptyContext()));
  }

  @Test
  void alwaysTrueConstant_evaluate_returnsTrue() {
    assertTrue(ConditionGroup.ALWAYS_TRUE.evaluate(emptyContext()));
  }

  @Test
  void alwaysFalseConstant_evaluate_returnsFalse() {
    assertFalse(ConditionGroup.ALWAYS_FALSE.evaluate(emptyContext()));
  }
}
