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

import de.markusbordihn.dialogqueststoryengine.data.issue.IssueSeverity;
import java.nio.file.Path;
import java.util.Properties;

public final class ValidationConfig extends Config {

  private static final String CONFIG_FILE = "dialog_quest_and_story_engine-validation.properties";
  private static final String CONFIG_HEADER =
      """
      Dialog, Quest and Story Engine - Validation Configuration

      strict_registry_references: When true, references to unknown registry entries (items,
        entities, ...) in quest and dialog content are reported as errors and block loading.
        When false, they are reported as warnings only. (default: true)
      """;
  private static final String KEY_STRICT_REGISTRY_REFERENCES = "strict_registry_references";
  private static final boolean DEFAULT_STRICT_REGISTRY_REFERENCES = true;

  private static volatile boolean strictRegistryReferences = DEFAULT_STRICT_REGISTRY_REFERENCES;

  private ValidationConfig() {}

  public static void load(Path configDirectory) {
    Path configFile = configDirectory.resolve(CONFIG_FILE);
    Properties properties = readConfigFile(configFile);
    Properties unmodifiedProperties = (Properties) properties.clone();

    configure(
        parseConfigValue(
            properties, KEY_STRICT_REGISTRY_REFERENCES, DEFAULT_STRICT_REGISTRY_REFERENCES));

    updateConfigFileIfChanged(configFile, CONFIG_HEADER, properties, unmodifiedProperties);
  }

  public static void configure(boolean strictRegistryReferences) {
    ValidationConfig.strictRegistryReferences = strictRegistryReferences;
  }

  public static boolean isStrictRegistryReferences() {
    return strictRegistryReferences;
  }

  public static IssueSeverity unknownRegistryReferenceSeverity() {
    return strictRegistryReferences ? IssueSeverity.ERROR : IssueSeverity.WARNING;
  }
}
