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

package de.markusbordihn.dialogqueststoryengine.content.quest;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class QuestClientRegistry {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Map<ResourceLocation, QuestDefinition> entries = new LinkedHashMap<>();

  private QuestClientRegistry() {}

  public static void put(QuestDefinition definition) {
    if (entries.containsKey(definition.id())) {
      log.warn(
          "{} Quest registry: duplicate id {} - overwriting.",
          Constants.LOG_PREFIX,
          definition.id());
    }

    entries.put(definition.id(), definition);
  }

  public static Optional<QuestDefinition> get(ResourceLocation id) {
    return Optional.ofNullable(entries.get(id));
  }

  public static int size() {
    return entries.size();
  }

  public static Set<ResourceLocation> ids() {
    return Collections.unmodifiableSet(entries.keySet());
  }

  public static void clear() {
    entries.clear();
  }
}
