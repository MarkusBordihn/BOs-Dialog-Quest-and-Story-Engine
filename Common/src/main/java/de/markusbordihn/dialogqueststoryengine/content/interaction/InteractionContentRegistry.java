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

package de.markusbordihn.dialogqueststoryengine.content.interaction;

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.content.InteractionDefinition;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class InteractionContentRegistry {

  private static volatile Map<ResourceLocation, InteractionDefinition> entries = Map.of();

  private InteractionContentRegistry() {}

  static void replaceAll(Map<ResourceLocation, InteractionDefinition> newEntries) {
    entries = Map.copyOf(newEntries);
  }

  public static Optional<InteractionDefinition> get(ResourceLocation id) {
    return Optional.ofNullable(entries.get(id));
  }

  public static Collection<InteractionDefinition> all() {
    return entries.values();
  }

  public static List<InteractionDefinition> allForEvent(InteractionEventType eventType) {
    return entries.values().stream().filter(definition -> definition.event() == eventType).toList();
  }

  public static int size() {
    return entries.size();
  }

  public static void clear() {
    entries = Map.of();
  }
}
