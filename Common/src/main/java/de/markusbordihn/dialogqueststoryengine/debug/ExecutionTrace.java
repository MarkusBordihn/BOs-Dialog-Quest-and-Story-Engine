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

import de.markusbordihn.dialogqueststoryengine.data.debug.ExecutionTraceEntry;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.List;

public final class ExecutionTrace {

  public static final int DEFAULT_CAPACITY = 50;

  private final int capacity;
  private final ArrayDeque<ExecutionTraceEntry> buffer;

  public ExecutionTrace() {
    this(DEFAULT_CAPACITY);
  }

  public ExecutionTrace(int capacity) {
    this.capacity = capacity;
    this.buffer = new ArrayDeque<>(capacity);
  }

  public synchronized void add(ExecutionTraceEntry entry) {
    if (this.buffer.size() >= this.capacity) {
      this.buffer.pollFirst();
    }

    this.buffer.addLast(entry);
  }

  public synchronized List<ExecutionTraceEntry> entries() {
    return Collections.unmodifiableList(List.copyOf(this.buffer));
  }

  public synchronized void clear() {
    this.buffer.clear();
  }

  public synchronized int size() {
    return this.buffer.size();
  }

  public int capacity() {
    return this.capacity;
  }
}
