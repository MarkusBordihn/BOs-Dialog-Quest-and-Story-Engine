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

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.interaction.handler.BlockInteractHandler;
import de.markusbordihn.dialogqueststoryengine.interaction.handler.EntityInteractHandler;
import de.markusbordihn.dialogqueststoryengine.interaction.handler.StepOnInteractHandler;
import de.markusbordihn.dialogqueststoryengine.registry.InteractionHandler;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.Optional;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class InteractionRegistry {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private InteractionRegistry() {}

  public static void registerBuiltIns() {
    Registries.INTERACTIONS.register(
        InteractionEventType.ON_ENTITY_INTERACT.resourceLocation(), EntityInteractHandler.INSTANCE);
    Registries.INTERACTIONS.register(
        InteractionEventType.ON_BLOCK_INTERACT.resourceLocation(), BlockInteractHandler.INSTANCE);
    Registries.INTERACTIONS.register(
        InteractionEventType.ON_STEP_ON.resourceLocation(), StepOnInteractHandler.INSTANCE);
    Registries.INTERACTIONS.register(
        InteractionEventType.ON_COMMAND.resourceLocation(), context -> {});
    Registries.INTERACTIONS.register(
        InteractionEventType.ON_HOLOPAD_USE.resourceLocation(),
        context ->
            ActionDataExecutor.execute(
                context.entry().actionDataSet(),
                context.player(),
                context.level().getServer(),
                context.entry().targetId()));
    Registries.INTERACTIONS.register(
        InteractionEventType.ON_EASY_NPC_INTERACT.resourceLocation(), context -> {});
  }

  public static Optional<InteractionHandler> get(InteractionEventType eventType) {
    return Registries.INTERACTIONS.get(eventType.resourceLocation());
  }

  public static void dispatch(InteractionContext context) {
    InteractionEntry entry = context.entry();
    ServerPlayer player = context.player();
    log.info(
        "Interaction event {} on {} '{}' (UUID: {}) fired by {}",
        entry.eventType().resourceLocation().getPath(),
        entry.targetKind(),
        entry.label(),
        entry.targetId(),
        player.getName().getString());
    String positionInfo = entry.blockPos() != null ? " at " + entry.blockPos().toShortString() : "";
    player.sendSystemMessage(
        Component.literal(
                "\u25B6 "
                    + entry.eventType().resourceLocation().getPath()
                    + " | "
                    + entry.targetKind()
                    + " '"
                    + entry.label()
                    + "'"
                    + positionInfo)
            .withStyle(ChatFormatting.GOLD));
    player.sendSystemMessage(
        Component.literal("  UUID: " + entry.targetId()).withStyle(ChatFormatting.DARK_GRAY));
    Registries.INTERACTIONS
        .get(entry.eventType().resourceLocation())
        .ifPresent(handler -> handler.handle(context));
  }
}
