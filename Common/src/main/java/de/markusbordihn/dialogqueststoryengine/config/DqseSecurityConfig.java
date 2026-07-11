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
import de.markusbordihn.dialogqueststoryengine.Constants;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class DqseSecurityConfig {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final String CONFIG_FILE = "dialog_quest_and_story_engine-security.properties";
  private static volatile boolean enableCommandActions = false;
  private static volatile List<String> commandActionWhitelist = List.of();
  private static volatile int defaultCommandPermissionLevel = 2;

  private DqseSecurityConfig() {}

  public static void load(Path configDirectory) {
    configure(false, List.of(), 2);
    Path configFile = configDirectory.resolve(CONFIG_FILE);
    if (!Files.isRegularFile(configFile)) {
      log.info(
          "{} Security config {} not found, using safe defaults.",
          Constants.LOG_PREFIX,
          configFile);
      return;
    }

    Properties properties = new Properties();
    try (Reader reader = Files.newBufferedReader(configFile)) {
      properties.load(reader);
      configure(
          Boolean.parseBoolean(properties.getProperty("enable_command_actions", "false")),
          parseWhitelist(properties.getProperty("command_action_whitelist", "")),
          parsePermissionLevel(properties.getProperty("default_command_permission_level", "2")));
    } catch (IOException | IllegalArgumentException exception) {
      log.error(
          "{} Failed to load security config {}: {}",
          Constants.LOG_PREFIX,
          configFile,
          exception.getMessage());
    }
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
            .map(DqseSecurityConfig::normalizeCommand)
            .anyMatch(
                allowed ->
                    allowed.equals("*")
                        || normalizedCommand.equals(allowed)
                        || normalizedCommand.startsWith(allowed + " "));
  }

  public static void configure(
      boolean enableCommandActions, List<String> whitelist, int defaultPermissionLevel) {
    DqseSecurityConfig.enableCommandActions = enableCommandActions;
    DqseSecurityConfig.commandActionWhitelist = List.copyOf(whitelist);
    DqseSecurityConfig.defaultCommandPermissionLevel =
        Math.max(0, Math.min(4, defaultPermissionLevel));
  }

  private static List<String> parseWhitelist(String value) {
    if (value.isBlank()) {
      return List.of();
    }
    return Arrays.stream(value.split(","))
        .map(String::trim)
        .filter(entry -> !entry.isEmpty())
        .toList();
  }

  private static int parsePermissionLevel(String value) {
    return Integer.parseInt(value.trim());
  }

  private static String normalizeCommand(String command) {
    String normalized = command.trim().toLowerCase(Locale.ROOT);
    return normalized.startsWith("/") ? normalized.substring(1) : normalized;
  }
}
