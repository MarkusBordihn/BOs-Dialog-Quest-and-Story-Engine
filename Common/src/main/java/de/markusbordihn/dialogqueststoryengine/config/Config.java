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

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.io.IOException;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public abstract class Config {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  protected Config() {}

  protected static Properties readConfigFile(Path configFile) {
    Properties properties = new Properties();
    if (!Files.isRegularFile(configFile)) {
      return properties;
    }

    try (Reader reader = Files.newBufferedReader(configFile)) {
      properties.load(reader);
    } catch (IOException exception) {
      log.error(
          "{} Failed to read config file {}: {}",
          Constants.LOG_PREFIX,
          configFile,
          exception.getMessage());
    }
    return properties;
  }

  protected static void updateConfigFileIfChanged(
      Path configFile, String header, Properties properties, Properties unmodifiedProperties) {
    if (properties.equals(unmodifiedProperties)) {
      return;
    }

    try {
      Path parent = configFile.getParent();
      if (parent != null) {
        Files.createDirectories(parent);
      }
      try (Writer writer = Files.newBufferedWriter(configFile)) {
        properties.store(writer, header);
      }
      log.info(
          "{} Wrote config file {} ({} entries)",
          Constants.LOG_PREFIX,
          configFile,
          properties.size());
    } catch (IOException exception) {
      log.error(
          "{} Failed to write config file {}: {}",
          Constants.LOG_PREFIX,
          configFile,
          exception.getMessage());
    }
  }

  protected static boolean parseConfigValue(
      Properties properties, String key, boolean defaultValue) {
    if (properties.containsKey(key)) {
      return Boolean.parseBoolean(properties.getProperty(key).trim());
    }

    properties.setProperty(key, Boolean.toString(defaultValue));
    return defaultValue;
  }

  protected static int parseConfigValue(Properties properties, String key, int defaultValue) {
    if (properties.containsKey(key)) {
      try {
        return Integer.parseInt(properties.getProperty(key).trim());
      } catch (NumberFormatException exception) {
        log.error(
            "{} Failed to parse integer for key {}, using default.", Constants.LOG_PREFIX, key);
      }
    }
    properties.setProperty(key, Integer.toString(defaultValue));
    return defaultValue;
  }

  protected static List<String> parseConfigValue(
      Properties properties, String key, List<String> defaultValue) {
    if (properties.containsKey(key)) {
      String value = properties.getProperty(key).trim();
      if (value.isEmpty()) {
        return List.of();
      }
      return Arrays.stream(value.split(","))
          .map(String::trim)
          .filter(entry -> !entry.isEmpty())
          .toList();
    }

    properties.setProperty(key, String.join(",", defaultValue));
    return defaultValue;
  }
}
