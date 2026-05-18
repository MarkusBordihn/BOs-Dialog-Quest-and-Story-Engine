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

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.concurrent.CopyOnWriteArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DataPackReloadNotifier {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final CopyOnWriteArrayList<Runnable> listeners = new CopyOnWriteArrayList<>();

  private DataPackReloadNotifier() {}

  public static void subscribe(Runnable listener) {
    listeners.add(listener);
  }

  public static void unsubscribe(Runnable listener) {
    listeners.remove(listener);
  }

  public static void fire() {
    for (Runnable listener : listeners) {
      try {
        listener.run();
      } catch (Exception exception) {
        log.error(
            "{} DataPackReloadNotifier listener threw an exception: {}",
            Constants.LOG_PREFIX,
            exception.getMessage(),
            exception);
      }
    }
  }
}
