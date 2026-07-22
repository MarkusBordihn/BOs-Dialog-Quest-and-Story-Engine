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

package de.markusbordihn.dialogqueststoryengine.interaction;

import de.markusbordihn.dialogqueststoryengine.data.interaction.ActionType;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class ActionDiagnostics {

  private static final int MAX_PER_TARGET = 10;
  private static final Map<UUID, Deque<Entry>> BY_TARGET = new ConcurrentHashMap<>();

  private ActionDiagnostics() {}

  public static void record(UUID targetId, ActionType type, String reason) {
    if (targetId == null) {
      return;
    }

    Deque<Entry> entries = BY_TARGET.computeIfAbsent(targetId, key -> new ArrayDeque<>());
    synchronized (entries) {
      entries.addFirst(new Entry(System.currentTimeMillis(), type, reason));
      while (entries.size() > MAX_PER_TARGET) {
        entries.removeLast();
      }
    }
  }

  public static List<String> formatFor(UUID targetId) {
    Deque<Entry> entries = BY_TARGET.get(targetId);
    if (entries == null) {
      return List.of();
    }

    List<String> lines = new ArrayList<>();
    synchronized (entries) {
      for (Entry entry : entries) {
        lines.add(entry.type() + ": " + entry.reason());
      }
    }
    return lines;
  }

  public static void clear(UUID targetId) {
    BY_TARGET.remove(targetId);
  }

  public static void clearAll() {
    BY_TARGET.clear();
  }

  public record Entry(long timeMillis, ActionType type, String reason) {}
}
