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

package de.markusbordihn.dialogqueststoryengine.network;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class C2SRateLimiter {

  private static final long WINDOW_MS = 1000L;
  private static final int MAX_PER_WINDOW = 30;
  private static final Map<UUID, Deque<Long>> requestTimestamps = new ConcurrentHashMap<>();

  private C2SRateLimiter() {}

  public static boolean allow(UUID playerUuid) {
    long now = System.currentTimeMillis();
    Deque<Long> window =
        requestTimestamps.computeIfAbsent(playerUuid, ignored -> new ArrayDeque<>());
    synchronized (window) {
      while (!window.isEmpty() && now - window.peekFirst() > WINDOW_MS) {
        window.pollFirst();
      }
      if (window.size() >= MAX_PER_WINDOW) {
        return false;
      }

      window.addLast(now);
      return true;
    }
  }

  public static void clear(UUID playerUuid) {
    requestTimestamps.remove(playerUuid);
  }

  public static void clearAll() {
    requestTimestamps.clear();
  }
}
