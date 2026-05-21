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

package de.markusbordihn.dialogqueststoryengine.config;

import java.util.List;

public final class DqseSecurityConfig {

  private static volatile boolean enableCommandActions = false;
  private static volatile List<String> commandActionWhitelist = List.of();
  private static volatile int defaultCommandPermissionLevel = 2;

  private DqseSecurityConfig() {}

  public static boolean isCommandActionsEnabled() {
    return enableCommandActions;
  }

  public static List<String> getCommandActionWhitelist() {
    return commandActionWhitelist;
  }

  public static int getDefaultCommandPermissionLevel() {
    return defaultCommandPermissionLevel;
  }

  public static void configure(
      boolean enableCommandActions, List<String> whitelist, int defaultPermissionLevel) {
    DqseSecurityConfig.enableCommandActions = enableCommandActions;
    DqseSecurityConfig.commandActionWhitelist = List.copyOf(whitelist);
    DqseSecurityConfig.defaultCommandPermissionLevel =
        Math.max(0, Math.min(4, defaultPermissionLevel));
  }
}
