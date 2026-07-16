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

import de.markusbordihn.dialogqueststoryengine.data.quest.content.QuestDefinition;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class QuestContentRegistry {

  private static volatile Map<ResourceLocation, QuestDefinition> entries = Map.of();
  private static volatile Map<ResourceLocation, Set<ResourceLocation>> dependents = Map.of();

  private QuestContentRegistry() {}

  static void replaceAll(Map<ResourceLocation, QuestDefinition> newEntries) {
    entries = Map.copyOf(newEntries);
    dependents = buildDependents(entries);
  }

  public static Optional<QuestDefinition> get(ResourceLocation id) {
    return Optional.ofNullable(entries.get(id));
  }

  public static Collection<QuestDefinition> all() {
    return entries.values();
  }

  public static Set<ResourceLocation> dependentsOf(ResourceLocation questId) {
    return dependents.getOrDefault(questId, Set.of());
  }

  public static int size() {
    return entries.size();
  }

  public static void clear() {
    entries = Map.of();
    dependents = Map.of();
  }

  private static Map<ResourceLocation, Set<ResourceLocation>> buildDependents(
      Map<ResourceLocation, QuestDefinition> definitions) {
    Map<ResourceLocation, Set<ResourceLocation>> index = new HashMap<>();
    for (QuestDefinition definition : definitions.values()) {
      for (ResourceLocation prerequisite : definition.logic().prerequisites().quests()) {
        index.computeIfAbsent(prerequisite, ignored -> new HashSet<>()).add(definition.id());
      }
    }
    Map<ResourceLocation, Set<ResourceLocation>> immutable = new HashMap<>();
    index.forEach((prerequisite, ids) -> immutable.put(prerequisite, Set.copyOf(ids)));
    return Map.copyOf(immutable);
  }
}
