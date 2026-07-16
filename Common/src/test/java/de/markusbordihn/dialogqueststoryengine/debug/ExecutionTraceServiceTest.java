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
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.debug.ConditionResult;
import de.markusbordihn.dialogqueststoryengine.data.debug.ExecutionTraceEntry;
import de.markusbordihn.dialogqueststoryengine.data.debug.TraceEventType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class ExecutionTraceServiceTest {

  private static final UUID PLAYER_A = UUID.fromString("00000000-0000-0000-0000-000000000001");
  private static final UUID PLAYER_B = UUID.fromString("00000000-0000-0000-0000-000000000002");

  private static ExecutionTraceEntry entry(TraceEventType type) {
    return new ExecutionTraceEntry(
        System.currentTimeMillis(),
        PLAYER_A,
        null,
        type,
        ConditionResult.NONE,
        List.of(),
        Optional.empty(),
        Optional.empty());
  }

  @AfterEach
  void clearAll() {
    ExecutionTraceService.clearAll();
  }

  @Test
  void recordToPlayerADoesNotAffectPlayerB() {
    ExecutionTraceService.record(PLAYER_A, entry(TraceEventType.SESSION_OPENED));

    assertEquals(1, ExecutionTraceService.entries(PLAYER_A).size());
    assertTrue(ExecutionTraceService.entries(PLAYER_B).isEmpty());
  }

  @Test
  void clearPlayerRemovesTheirTrace() {
    ExecutionTraceService.record(PLAYER_A, entry(TraceEventType.SESSION_OPENED));
    ExecutionTraceService.clear(PLAYER_A);

    assertTrue(ExecutionTraceService.entries(PLAYER_A).isEmpty());
  }

  @Test
  void entriesForUnknownPlayerReturnsEmptyList() {
    assertTrue(ExecutionTraceService.entries(UUID.randomUUID()).isEmpty());
  }

  @Test
  void multipleEntriesAreOrdered() {
    ExecutionTraceService.record(PLAYER_A, entry(TraceEventType.SESSION_OPENED));
    ExecutionTraceService.record(PLAYER_A, entry(TraceEventType.CHOICE_SUBMITTED));
    ExecutionTraceService.record(PLAYER_A, entry(TraceEventType.SESSION_CLOSED));

    List<ExecutionTraceEntry> entries = ExecutionTraceService.entries(PLAYER_A);
    assertEquals(3, entries.size());
    assertEquals(TraceEventType.SESSION_OPENED, entries.get(0).eventType());
    assertEquals(TraceEventType.SESSION_CLOSED, entries.get(2).eventType());
  }
}
