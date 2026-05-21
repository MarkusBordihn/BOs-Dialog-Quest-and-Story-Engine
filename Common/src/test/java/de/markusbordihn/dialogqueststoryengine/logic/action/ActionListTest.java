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

package de.markusbordihn.dialogqueststoryengine.logic.action;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;

class ActionListTest {

  @Test
  void empty_isSafe() {
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));
    ActionList.EMPTY.execute(ctx);
    assertTrue(ActionList.EMPTY.isEmpty());
  }

  @Test
  void execute_actionsRunInOrder() {
    AtomicInteger counter = new AtomicInteger(0);
    ActionList list =
        new ActionList(List.of(ctx -> counter.set(1), ctx -> counter.set(counter.get() + 10)));
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));

    list.execute(ctx);

    assertEquals(11, counter.get());
  }

  @Test
  void execute_throwingActionDoesNotAbortRemainingActions() {
    AtomicInteger counter = new AtomicInteger(0);
    ActionList list =
        new ActionList(
            List.of(
                ctx -> {
                  throw new RuntimeException("intentional test error");
                },
                ctx -> counter.incrementAndGet()));
    ActionContext ctx = ActionContext.ofTest(new PlayerState(UUID.randomUUID()));

    list.execute(ctx);

    assertEquals(1, counter.get());
  }

  @Test
  void isEmpty_withActions_returnsFalse() {
    ActionList list = new ActionList(List.of(Action.NOOP));
    assertFalse(list.isEmpty());
  }

  @Test
  void constructor_defensivelyCopiesInput() {
    Action first = Action.NOOP;
    ActionList list = new ActionList(List.of(first));
    assertEquals(1, list.actions().size());
  }
}
