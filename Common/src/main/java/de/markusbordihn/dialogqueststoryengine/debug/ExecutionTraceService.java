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

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ExecutionTraceService {

  private static final ConcurrentHashMap<UUID, ExecutionTrace> traces = new ConcurrentHashMap<>();

  private ExecutionTraceService() {}

  public static ExecutionTrace getOrCreate(UUID playerUuid) {
    return traces.computeIfAbsent(playerUuid, id -> new ExecutionTrace());
  }

  public static void record(UUID playerUuid, ExecutionTraceEntry entry) {
    getOrCreate(playerUuid).add(entry);
  }

  public static List<ExecutionTraceEntry> entries(UUID playerUuid) {
    ExecutionTrace trace = traces.get(playerUuid);
    return trace != null ? trace.entries() : List.of();
  }

  public static void clear(UUID playerUuid) {
    traces.remove(playerUuid);
  }

  public static void clearAll() {
    traces.clear();
  }
}
