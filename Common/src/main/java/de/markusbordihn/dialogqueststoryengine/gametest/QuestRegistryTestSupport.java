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

package de.markusbordihn.dialogqueststoryengine.gametest;

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public final class QuestRegistryTestSupport {

  private QuestRegistryTestSupport() {}

  public static Map<ResourceLocation, QuestDefinition> install(
      Map<ResourceLocation, QuestDefinition> quests) {
    Map<ResourceLocation, QuestDefinition> snapshot = snapshot();
    Map<ResourceLocation, QuestDefinition> merged = new LinkedHashMap<>(snapshot);
    merged.putAll(quests);
    setRegistry(merged);
    return snapshot;
  }

  public static void restore(Map<ResourceLocation, QuestDefinition> snapshot) {
    setRegistry(snapshot);
  }

  private static Map<ResourceLocation, QuestDefinition> snapshot() {
    Map<ResourceLocation, QuestDefinition> snapshot = new LinkedHashMap<>();
    for (QuestDefinition existing : QuestContentRegistry.all()) {
      snapshot.put(existing.id(), existing);
    }
    return snapshot;
  }

  private static void setRegistry(Map<ResourceLocation, QuestDefinition> quests) {
    try {
      Method replaceAll = QuestContentRegistry.class.getDeclaredMethod("replaceAll", Map.class);
      replaceAll.setAccessible(true);
      replaceAll.invoke(null, quests);
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Failed to replace quest registry", exception);
    }
  }
}
