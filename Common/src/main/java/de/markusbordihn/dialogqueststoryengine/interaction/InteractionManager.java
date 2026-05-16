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

package de.markusbordihn.dialogqueststoryengine.interaction;

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionSource;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionStore;
import de.markusbordihn.dialogqueststoryengine.data.saveddata.InteractionSavedData;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import net.minecraft.server.MinecraftServer;

public final class InteractionManager {

  static final InteractionStore DATAPACK_STORE = new InteractionStore();
  static final InteractionStore API_STORE = new InteractionStore();

  private InteractionManager() {}

  public static void registerDatapackInteraction(InteractionEntry entry) {
    DATAPACK_STORE.register(entry);
  }

  public static boolean unregisterDatapackInteraction(
      UUID targetId, InteractionEventType eventType) {
    return DATAPACK_STORE.unregister(targetId, eventType);
  }

  public static void registerApiInteraction(InteractionEntry entry) {
    API_STORE.register(entry);
  }

  public static boolean unregisterApiInteraction(UUID targetId, InteractionEventType eventType) {
    return API_STORE.unregister(targetId, eventType);
  }

  public static List<InteractionEntry> allForTarget(MinecraftServer server, UUID targetId) {
    return Stream.of(
            InteractionSavedData.get(server).getInteractionsForTarget(targetId),
            DATAPACK_STORE.getInteractionsForTarget(targetId),
            API_STORE.getInteractionsForTarget(targetId))
        .flatMap(List::stream)
        .toList();
  }

  public static List<InteractionEntry> allForTarget(
      MinecraftServer server, UUID targetId, InteractionSource source) {
    return switch (source) {
      case WAND -> InteractionSavedData.get(server).getInteractionsForTarget(targetId);
      case DATAPACK -> DATAPACK_STORE.getInteractionsForTarget(targetId);
      case API -> API_STORE.getInteractionsForTarget(targetId);
    };
  }

  public static List<InteractionEntry> allByEventType(
      MinecraftServer server, InteractionEventType eventType) {
    return Stream.of(
            InteractionSavedData.get(server).getAllEntries(),
            DATAPACK_STORE.getAllEntries(),
            API_STORE.getAllEntries())
        .flatMap(List::stream)
        .filter(entry -> entry.eventType() == eventType)
        .toList();
  }

  public static boolean hasStepOnInteractions(MinecraftServer server) {
    return InteractionSavedData.get(server).hasStepOnInteractions()
        || DATAPACK_STORE.hasStepOnInteractions()
        || API_STORE.hasStepOnInteractions();
  }

  public static void clearDatapackInteractions() {
    DATAPACK_STORE.clearAll();
  }

  public static void onServerStopping() {
    DATAPACK_STORE.clearAll();
    API_STORE.clearAll();
  }
}
