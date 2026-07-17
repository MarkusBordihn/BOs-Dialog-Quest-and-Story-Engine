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

import com.google.gson.JsonObject;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

public final class SecurityConfig extends Config {

  private static final String CONFIG_FILE = "dialog_quest_and_story_engine-security.properties";
  private static final String CONFIG_HEADER =
      """
      Dialog, Quest and Story Engine - Security Configuration

      enable_command_actions: Master switch for command and function actions triggered by dialogs
        and quests. Disabled by default for safety. (default: false)
      command_action_whitelist: Comma-separated list of allowed command roots (e.g. "say, give").
        Use "*" to allow all commands. An empty list blocks every command. (default: empty)
      default_command_permission_level: Minecraft permission level (0-4) used when a command or
        function action does not specify its own. (default: 2)
      """;
  private static final String KEY_ENABLE_COMMAND_ACTIONS = "enable_command_actions";
  private static final String KEY_COMMAND_ACTION_WHITELIST = "command_action_whitelist";
  private static final String KEY_DEFAULT_COMMAND_PERMISSION_LEVEL =
      "default_command_permission_level";
  private static final boolean DEFAULT_ENABLE_COMMAND_ACTIONS = false;
  private static final List<String> DEFAULT_COMMAND_ACTION_WHITELIST = List.of();
  private static final int DEFAULT_COMMAND_PERMISSION_LEVEL = 2;

  private static volatile boolean enableCommandActions = DEFAULT_ENABLE_COMMAND_ACTIONS;
  private static volatile List<String> commandActionWhitelist = DEFAULT_COMMAND_ACTION_WHITELIST;
  private static volatile int defaultCommandPermissionLevel = DEFAULT_COMMAND_PERMISSION_LEVEL;

  private SecurityConfig() {}

  public static void load(Path configDirectory) {
    Path configFile = configDirectory.resolve(CONFIG_FILE);
    Properties properties = readConfigFile(configFile);
    Properties unmodifiedProperties = (Properties) properties.clone();

    configure(
        parseConfigValue(properties, KEY_ENABLE_COMMAND_ACTIONS, DEFAULT_ENABLE_COMMAND_ACTIONS),
        parseConfigValue(
            properties, KEY_COMMAND_ACTION_WHITELIST, DEFAULT_COMMAND_ACTION_WHITELIST),
        parseConfigValue(
            properties, KEY_DEFAULT_COMMAND_PERMISSION_LEVEL, DEFAULT_COMMAND_PERMISSION_LEVEL));

    updateConfigFileIfChanged(configFile, CONFIG_HEADER, properties, unmodifiedProperties);
  }

  public static void configure(
      boolean enableCommandActions, List<String> whitelist, int defaultPermissionLevel) {
    SecurityConfig.enableCommandActions = enableCommandActions;
    SecurityConfig.commandActionWhitelist = List.copyOf(whitelist);
    SecurityConfig.defaultCommandPermissionLevel = Math.max(0, Math.min(4, defaultPermissionLevel));
  }

  public static boolean isCommandActionsEnabled() {
    return enableCommandActions;
  }

  public static List<String> getCommandActionWhitelist() {
    return commandActionWhitelist;
  }

  public static int getDefaultCommandPermissionLevel() {
    return defaultCommandPermissionLevel;
  }

  public static int parsePermissionLevel(JsonObject jsonObject) {
    String field = jsonObject.has("permission_level") ? "permission_level" : "permission";
    if (jsonObject.has(field) && jsonObject.get(field).isJsonPrimitive()) {
      return Math.max(0, Math.min(4, jsonObject.get(field).getAsInt()));
    }

    return defaultCommandPermissionLevel;
  }

  public static boolean isCommandAllowed(String command) {
    String normalizedCommand = normalizeCommand(command);
    return !commandActionWhitelist.isEmpty()
        && commandActionWhitelist.stream()
            .map(SecurityConfig::normalizeCommand)
            .anyMatch(
                allowed ->
                    allowed.equals("*")
                        || normalizedCommand.equals(allowed)
                        || normalizedCommand.startsWith(allowed + " "));
  }

  private static String normalizeCommand(String command) {
    String normalized = command.trim().toLowerCase(Locale.ROOT);
    return normalized.startsWith("/") ? normalized.substring(1) : normalized;
  }
}
