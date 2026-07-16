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
import de.markusbordihn.dialogqueststoryengine.config.DqseSecurityConfig;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataEntry;
import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunCommandAction;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.story.OpenClientStoryPacket;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ActionDataExecutor {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ActionDataExecutor() {}

  public static void execute(
      ActionDataSet actionDataSet, ServerPlayer player, MinecraftServer server) {
    if (actionDataSet.isEmpty()) {
      return;
    }

    for (ActionDataEntry action : actionDataSet.entries()) {
      executeAction(action, player, server);
    }
  }

  private static void executeAction(
      ActionDataEntry action, ServerPlayer player, MinecraftServer server) {
    switch (action.type()) {
      case OPEN_STORY -> {
        ResourceLocation storyId = action.storyId();
        if (storyId == null) {
          log.warn(
              "{} ActionDataExecutor: OPEN_STORY action has no storyId for player {}",
              Constants.LOG_PREFIX,
              player.getName().getString());
          return;
        }
        NetworkHandlerManager.sendToPlayer(
            player, new OpenClientStoryPacket(storyId, action.themeOverrideId()));
      }
      case OPEN_INTERACTIVE_STORY -> {
        ResourceLocation storyId = action.storyId();
        if (storyId == null) {
          log.warn(
              "{} ActionDataExecutor: OPEN_INTERACTIVE_STORY action has no storyId for player {}",
              Constants.LOG_PREFIX,
              player.getName().getString());
          return;
        }
        SessionManager.openStorySession(player, storyId);
      }
      case RUN_COMMAND -> {
        String command = action.command();
        if (command.isEmpty()) {
          return;
        }
        PlayerStateService.get(player.getUUID())
            .ifPresent(
                playerState ->
                    new RunCommandAction(
                            command, DqseSecurityConfig.getDefaultCommandPermissionLevel())
                        .execute(
                            new ActionContext(
                                player,
                                playerState,
                                server,
                                "interaction-action-" + action.id(),
                                Optional.empty())));
      }
      case SET_FACT -> {
        ResourceLocation factId = action.factId();
        if (factId == null) {
          return;
        }
        PlayerStateService.get(player.getUUID())
            .ifPresent(playerState -> setFact(playerState, factId, action.factValue()));
      }
      default ->
          log.debug(
              "{} ActionDataExecutor: unhandled action type {} - skipping",
              Constants.LOG_PREFIX,
              action.type());
    }
  }

  private static void setFact(PlayerState playerState, ResourceLocation factId, String factValue) {
    playerState.setFact(FactScope.PLAYER, factId.toString(), FactValue.of(factValue));
  }
}
