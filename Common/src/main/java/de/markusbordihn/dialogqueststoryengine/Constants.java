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

package de.markusbordihn.dialogqueststoryengine;

import java.nio.file.Path;
import java.nio.file.Paths;

public final class Constants {

  public static final String MOD_ID = "dialog_quest_and_story_engine";
  public static final String MOD_NAMESPACE = "dqse";
  public static final String MOD_NAME = "Dialog, Quest and Story Engine";
  public static final String MOD_COMMAND = "dqs";
  public static final String MOD_PREFIX = MOD_ID + ".";
  public static final String GUI_PREFIX = "gui." + MOD_ID + ".";
  public static final String LOG_NAME = MOD_NAME;
  public static final String LOG_PREFIX = "[DQSE]";
  public static final String LOG_REGISTER_PREFIX = "Register " + MOD_NAME;

  public static final String INTERACTION_WAND = "interaction_wand";
  public static final String HOLOPAD = "holopad";

  public static Path GAME_DIR = Paths.get("").toAbsolutePath();
  public static Path CONFIG_DIR = GAME_DIR.resolve("config");

  private Constants() {}
}
