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

package de.markusbordihn.dialogqueststoryengine.debug;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ExecutionTraceTest {

  private static ExecutionTraceEntry entry(long timestamp) {
    return new ExecutionTraceEntry(
        timestamp,
        UUID.randomUUID(),
        null,
        TraceEventType.SESSION_OPENED,
        ConditionResult.NONE,
        List.of(),
        Optional.empty(),
        Optional.empty());
  }

  @Test
  void ringBufferEvictsOldestWhenFull() {
    ExecutionTrace trace = new ExecutionTrace(3);
    trace.add(entry(1));
    trace.add(entry(2));
    trace.add(entry(3));
    trace.add(entry(4));

    assertEquals(3, trace.size());
    assertEquals(2, trace.entries().get(0).timestamp());
    assertEquals(4, trace.entries().get(2).timestamp());
  }

  @Test
  void entriesAreFifoOrder() {
    ExecutionTrace trace = new ExecutionTrace(5);
    trace.add(entry(10));
    trace.add(entry(20));
    trace.add(entry(30));

    List<ExecutionTraceEntry> entries = trace.entries();
    assertEquals(3, entries.size());
    assertEquals(10, entries.get(0).timestamp());
    assertEquals(20, entries.get(1).timestamp());
    assertEquals(30, entries.get(2).timestamp());
  }

  @Test
  void clearResetsSize() {
    ExecutionTrace trace = new ExecutionTrace();
    trace.add(entry(1));
    trace.add(entry(2));
    trace.clear();

    assertEquals(0, trace.size());
  }

  @Test
  void defaultCapacityIs50() {
    ExecutionTrace trace = new ExecutionTrace();
    for (int i = 0; i < 51; i++) {
      trace.add(entry(i));
    }

    assertEquals(50, trace.size());
  }
}
