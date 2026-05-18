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

package de.markusbordihn.dialogqueststoryengine.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import org.junit.jupiter.api.Test;

class DataPackReloadNotifierTest {

  @Test
  void registeredListenerIsFired() {
    List<String> called = new ArrayList<>();
    Runnable listener = () -> called.add("fired");

    DataPackReloadNotifier.subscribe(listener);
    try {
      DataPackReloadNotifier.fire();
      assertEquals(1, called.size());
    } finally {
      DataPackReloadNotifier.unsubscribe(listener);
    }
  }

  @Test
  void exceptionInListenerDoesNotStopOthers() {
    AtomicBoolean secondCalled = new AtomicBoolean(false);
    Runnable throwing =
        () -> {
          throw new RuntimeException("simulated error");
        };
    Runnable second = () -> secondCalled.set(true);

    DataPackReloadNotifier.subscribe(throwing);
    DataPackReloadNotifier.subscribe(second);
    try {
      DataPackReloadNotifier.fire();
      assertTrue(secondCalled.get());
    } finally {
      DataPackReloadNotifier.unsubscribe(throwing);
      DataPackReloadNotifier.unsubscribe(second);
    }
  }

  @Test
  void unsubscribedListenerIsNotFired() {
    AtomicBoolean called = new AtomicBoolean(false);
    Runnable listener = () -> called.set(true);

    DataPackReloadNotifier.subscribe(listener);
    DataPackReloadNotifier.unsubscribe(listener);
    DataPackReloadNotifier.fire();

    assertFalse(called.get());
  }
}
