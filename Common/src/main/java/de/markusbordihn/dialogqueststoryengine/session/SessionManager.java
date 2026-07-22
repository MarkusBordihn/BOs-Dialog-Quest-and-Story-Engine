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

package de.markusbordihn.dialogqueststoryengine.session;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.ChoiceDefinition;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.story.InteractiveStoryContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.debug.ConditionResult;
import de.markusbordihn.dialogqueststoryengine.data.debug.ExecutionTraceEntry;
import de.markusbordihn.dialogqueststoryengine.data.debug.TraceEventType;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogChoiceDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogNodeDefinition;
import de.markusbordihn.dialogqueststoryengine.data.session.SessionCloseReason;
import de.markusbordihn.dialogqueststoryengine.data.session.SessionRejectionReason;
import de.markusbordihn.dialogqueststoryengine.data.session.SessionType;
import de.markusbordihn.dialogqueststoryengine.data.story.InteractiveStoryChoice;
import de.markusbordihn.dialogqueststoryengine.data.story.InteractiveStoryDefinition;
import de.markusbordihn.dialogqueststoryengine.debug.ExecutionTraceService;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.message.session.CloseSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.DialogSessionPacketType;
import de.markusbordihn.dialogqueststoryengine.network.message.session.OpenStorySessionPacket;
import de.markusbordihn.dialogqueststoryengine.network.message.session.SessionRejectedPacket;
import de.markusbordihn.dialogqueststoryengine.server.ServerEvents;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import de.markusbordihn.dialogqueststoryengine.state.PlayerStateService;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class SessionManager {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final long MAX_SESSION_AGE_MS = TimeUnit.MINUTES.toMillis(15);
  private static final ConcurrentHashMap<UUID, Session> sessionsById = new ConcurrentHashMap<>();
  private static final ConcurrentHashMap<UUID, EnumMap<SessionType, Session>> sessionsByPlayer =
      new ConcurrentHashMap<>();
  private static final Set<ResourceLocation> openingStoryIds = new HashSet<>();

  private SessionManager() {}

  public static DialogSession openDialogSession(
      ServerPlayer player, ResourceLocation dialogId, Optional<String> startNodeOverride) {
    Optional<DialogDefinition> optionalDefinition = DialogContentRegistry.get(dialogId);
    if (optionalDefinition.isEmpty()) {
      log.warn(
          "{} openDialogSession: no dialog found for '{}' - player {}",
          Constants.LOG_PREFIX,
          dialogId,
          player.getName().getString());
      return null;
    }

    DialogDefinition definition = optionalDefinition.get();
    String startNode = startNodeOverride.orElse(definition.startNode());
    DialogNodeDefinition startNodeDefinition = definition.nodes().get(startNode);
    if (startNodeDefinition == null) {
      log.error(
          "{} openDialogSession: start node '{}' not found in dialog '{}' for player {}",
          Constants.LOG_PREFIX,
          startNode,
          dialogId,
          player.getName().getString());
      return null;
    }

    closeExistingSession(player, SessionType.DIALOG, SessionCloseReason.NEW_SESSION_OPENED);

    UUID sessionId = UUID.randomUUID();
    DialogSession session = new DialogSession(sessionId, player.getUUID(), dialogId, startNode);
    registerSession(player.getUUID(), session);

    PlayerState playerState = getOrCreatePlayerState(player);
    ConditionContext conditionContext = new ConditionContext(player, playerState, player.server);
    List<String> allowedChoiceIds =
        filterAllowedChoiceIds(startNodeDefinition.choices(), conditionContext);

    Map<String, String> choiceLabels =
        buildChoiceLabels(startNodeDefinition.choices(), allowedChoiceIds);
    NetworkHandlerManager.sendToPlayer(
        player,
        new DialogSessionPacket(
            sessionId,
            DialogSessionPacketType.OPEN_DIALOG,
            dialogId,
            startNode,
            startNodeDefinition.speakerKey(),
            startNodeDefinition.textKey(),
            allowedChoiceIds,
            choiceLabels,
            Map.of(),
            session.revision()));
    recordTrace(player, session, dialogId, TraceEventType.SESSION_OPENED, Optional.empty());
    return session;
  }

  public static InteractiveStorySession openStorySession(
      ServerPlayer player, ResourceLocation storyId) {
    if (!openingStoryIds.add(storyId)) {
      log.error("{} Recursive story open rejected for '{}'.", Constants.LOG_PREFIX, storyId);
      return null;
    }
    try {
      return openStorySessionInternal(player, storyId);
    } finally {
      openingStoryIds.remove(storyId);
    }
  }

  private static InteractiveStorySession openStorySessionInternal(
      ServerPlayer player, ResourceLocation storyId) {
    Optional<InteractiveStoryDefinition> optionalDefinition =
        InteractiveStoryContentRegistry.get(storyId);
    if (optionalDefinition.isEmpty()) {
      log.warn(
          "{} openStorySession: no story found for '{}' - player {}",
          Constants.LOG_PREFIX,
          storyId,
          player.getName().getString());
      return null;
    }

    InteractiveStoryDefinition definition = optionalDefinition.get();

    closeExistingSession(
        player, SessionType.INTERACTIVE_STORY, SessionCloseReason.NEW_SESSION_OPENED);

    UUID sessionId = UUID.randomUUID();
    InteractiveStorySession session =
        new InteractiveStorySession(sessionId, player.getUUID(), storyId);
    registerSession(player.getUUID(), session);

    PlayerState playerState = getOrCreatePlayerState(player);
    SessionContext sessionContext = new SessionContext(sessionId, SessionType.INTERACTIVE_STORY);
    ActionContext actionContext =
        new ActionContext(
            player,
            playerState,
            player.server,
            "session-" + sessionId,
            Optional.of(sessionContext));

    definition.onOpen().execute(actionContext);

    if (!isCurrentSession(session)) {
      return null;
    }

    ConditionContext conditionContext = new ConditionContext(player, playerState, player.server);
    List<String> allowedChoiceIds = filterAllowedChoiceIds(definition.choices(), conditionContext);
    Map<String, String> choiceLabels = buildChoiceLabels(definition.choices(), allowedChoiceIds);

    NetworkHandlerManager.sendToPlayer(
        player,
        new OpenStorySessionPacket(
            sessionId,
            storyId,
            definition.displayStoryId(),
            allowedChoiceIds,
            Map.of(),
            choiceLabels,
            session.revision()));

    recordTrace(player, session, storyId, TraceEventType.SESSION_OPENED, Optional.empty());
    return session;
  }

  public static void submitChoice(
      ServerPlayer player, UUID sessionId, String choiceId, int clientRevision) {
    Session session = sessionsById.get(sessionId);
    if (session == null) {
      NetworkHandlerManager.sendToPlayer(
          player, new SessionRejectedPacket(sessionId, SessionRejectionReason.UNKNOWN_SESSION));
      return;
    }

    if (!session.ownerPlayerUuid().equals(player.getUUID())) {
      log.warn(
          "{} submitChoice: player {} attempted to submit choice for session owned by {}",
          Constants.LOG_PREFIX,
          player.getName().getString(),
          session.ownerPlayerUuid());
      NetworkHandlerManager.sendToPlayer(
          player, new SessionRejectedPacket(sessionId, SessionRejectionReason.WRONG_OWNER));
      return;
    }

    if (!session.isOpen()) {
      NetworkHandlerManager.sendToPlayer(
          player, new SessionRejectedPacket(sessionId, SessionRejectionReason.SESSION_CLOSED));
      return;
    }

    if (isExpired(session)) {
      removeAndClose(session, player, SessionCloseReason.TIMEOUT);
      return;
    }

    if (clientRevision != session.revision()) {
      NetworkHandlerManager.sendToPlayer(
          player, new SessionRejectedPacket(sessionId, SessionRejectionReason.STALE_REVISION));
      return;
    }

    if (session instanceof DialogSession dialogSession) {
      recordTrace(
          player,
          session,
          dialogSession.dialogId(),
          TraceEventType.CHOICE_SUBMITTED,
          Optional.of(choiceId));
      submitDialogChoice(player, dialogSession, choiceId);
    } else if (session instanceof InteractiveStorySession storySession) {
      recordTrace(
          player,
          session,
          storySession.storyId(),
          TraceEventType.CHOICE_SUBMITTED,
          Optional.of(choiceId));
      submitStoryChoice(player, storySession, choiceId);
    }
  }

  public static void closeSession(ServerPlayer player, UUID sessionId) {
    Session session = sessionsById.get(sessionId);
    if (session == null || !session.isOpen()) {
      return;
    }

    if (!session.ownerPlayerUuid().equals(player.getUUID())) {
      log.warn(
          "{} closeSession: player {} attempted to close session owned by {}",
          Constants.LOG_PREFIX,
          player.getName().getString(),
          session.ownerPlayerUuid());
      return;
    }

    removeAndClose(session, player, SessionCloseReason.PLAYER_CLOSED);
  }

  public static void invalidatePlayerSessions(UUID playerUuid) {
    EnumMap<SessionType, Session> playerSessions = sessionsByPlayer.remove(playerUuid);
    if (playerSessions == null) {
      return;
    }

    for (Session session : playerSessions.values()) {
      session.invalidate();
      sessionsById.remove(session.sessionId());
    }
    log.debug(
        "{} Invalidated {} session(s) for disconnected player {}.",
        Constants.LOG_PREFIX,
        playerSessions.size(),
        playerUuid);
  }

  public static void closePlayerSessions(ServerPlayer player, SessionCloseReason reason) {
    EnumMap<SessionType, Session> playerSessions = sessionsByPlayer.get(player.getUUID());
    if (playerSessions == null) {
      return;
    }
    for (Session session : List.copyOf(playerSessions.values())) {
      if (session.isOpen()) {
        removeAndClose(session, player, reason);
      }
    }
  }

  public static void expireSessions(MinecraftServer server) {
    for (Session session : List.copyOf(sessionsById.values())) {
      if (!session.isOpen() || !isExpired(session)) {
        continue;
      }
      ServerPlayer player = server.getPlayerList().getPlayer(session.ownerPlayerUuid());
      if (player != null) {
        removeAndClose(session, player, SessionCloseReason.TIMEOUT);
      } else {
        invalidatePlayerSessions(session.ownerPlayerUuid());
      }
    }
  }

  public static void invalidateAll() {
    if (sessionsById.isEmpty()) {
      return;
    }

    MinecraftServer server = ServerEvents.getServer();
    for (Session session : sessionsById.values()) {
      if (!session.isOpen()) {
        continue;
      }

      session.invalidate();
      if (server != null) {
        ServerPlayer player = server.getPlayerList().getPlayer(session.ownerPlayerUuid());
        if (player != null) {
          NetworkHandlerManager.sendToPlayer(
              player, new CloseSessionPacket(session.sessionId(), SessionCloseReason.RELOAD));
        }
      }
    }
    sessionsById.clear();
    sessionsByPlayer.clear();
    log.info("{} Invalidated all sessions.", Constants.LOG_PREFIX);
  }

  private static void submitDialogChoice(
      ServerPlayer player, DialogSession session, String choiceId) {
    Optional<DialogDefinition> optionalDefinition = DialogContentRegistry.get(session.dialogId());
    if (optionalDefinition.isEmpty()) {
      removeAndClose(session, player, SessionCloseReason.RELOAD);
      return;
    }

    DialogDefinition definition = optionalDefinition.get();
    DialogNodeDefinition currentNode = definition.nodes().get(session.currentNodeId());
    if (currentNode == null) {
      removeAndClose(session, player, SessionCloseReason.RELOAD);
      return;
    }

    Optional<DialogChoiceDefinition> optionalChoice =
        currentNode.choices().stream().filter(choice -> choice.id().equals(choiceId)).findFirst();
    if (optionalChoice.isEmpty()) {
      NetworkHandlerManager.sendToPlayer(
          player,
          new SessionRejectedPacket(session.sessionId(), SessionRejectionReason.UNKNOWN_CHOICE));
      return;
    }

    DialogChoiceDefinition choice = optionalChoice.get();

    PlayerState playerState = getOrCreatePlayerState(player);
    ConditionContext conditionContext = new ConditionContext(player, playerState, player.server);
    if (!choice.conditions().evaluate(conditionContext)) {
      NetworkHandlerManager.sendToPlayer(
          player,
          new SessionRejectedPacket(session.sessionId(), SessionRejectionReason.CONDITION_FAILED));
      return;
    }

    SessionContext sessionContext = new SessionContext(session.sessionId(), SessionType.DIALOG);
    ActionContext actionContext =
        new ActionContext(
            player,
            playerState,
            player.server,
            "session-" + session.sessionId(),
            Optional.of(sessionContext));
    choice.actions().execute(actionContext);

    if (!isCurrentSession(session)) {
      return;
    }
    session.bumpRevision();

    if (choice.close() || choice.next().isEmpty()) {
      removeAndClose(session, player, SessionCloseReason.PLAYER_CLOSED);
      return;
    }

    String nextNodeId = choice.next().get();
    DialogNodeDefinition nextNode = definition.nodes().get(nextNodeId);
    if (nextNode == null) {
      log.warn(
          "{} submitDialogChoice: next node '{}' not found in dialog '{}' for session {}",
          Constants.LOG_PREFIX,
          nextNodeId,
          session.dialogId(),
          session.sessionId());
      removeAndClose(session, player, SessionCloseReason.RELOAD);
      return;
    }

    session.setCurrentNodeId(nextNodeId);
    List<String> allowedChoiceIds = filterAllowedChoiceIds(nextNode.choices(), conditionContext);
    Map<String, String> choiceLabels = buildChoiceLabels(nextNode.choices(), allowedChoiceIds);
    NetworkHandlerManager.sendToPlayer(
        player,
        new DialogSessionPacket(
            session.sessionId(),
            DialogSessionPacketType.NAVIGATE_NODE,
            null,
            nextNodeId,
            nextNode.speakerKey(),
            nextNode.textKey(),
            allowedChoiceIds,
            choiceLabels,
            Map.of(),
            session.revision()));
  }

  private static void submitStoryChoice(
      ServerPlayer player, InteractiveStorySession session, String choiceId) {
    Optional<InteractiveStoryDefinition> optionalDefinition =
        InteractiveStoryContentRegistry.get(session.storyId());
    if (optionalDefinition.isEmpty()) {
      removeAndClose(session, player, SessionCloseReason.RELOAD);
      return;
    }

    InteractiveStoryDefinition definition = optionalDefinition.get();

    Optional<InteractiveStoryChoice> optionalChoice =
        definition.choices().stream().filter(choice -> choice.id().equals(choiceId)).findFirst();
    if (optionalChoice.isEmpty()) {
      NetworkHandlerManager.sendToPlayer(
          player,
          new SessionRejectedPacket(session.sessionId(), SessionRejectionReason.UNKNOWN_CHOICE));
      return;
    }

    InteractiveStoryChoice choice = optionalChoice.get();

    PlayerState playerState = getOrCreatePlayerState(player);
    ConditionContext conditionContext = new ConditionContext(player, playerState, player.server);
    if (!choice.conditions().evaluate(conditionContext)) {
      NetworkHandlerManager.sendToPlayer(
          player,
          new SessionRejectedPacket(session.sessionId(), SessionRejectionReason.CONDITION_FAILED));
      return;
    }

    SessionContext sessionContext =
        new SessionContext(session.sessionId(), SessionType.INTERACTIVE_STORY);
    ActionContext actionContext =
        new ActionContext(
            player,
            playerState,
            player.server,
            "session-" + session.sessionId(),
            Optional.of(sessionContext));
    choice.actions().execute(actionContext);

    if (!isCurrentSession(session)) {
      return;
    }
    session.bumpRevision();

    removeAndClose(session, player, SessionCloseReason.PLAYER_CLOSED);
  }

  private static void registerSession(UUID playerUuid, Session session) {
    sessionsById.put(session.sessionId(), session);
    sessionsByPlayer
        .computeIfAbsent(playerUuid, ignored -> new EnumMap<>(SessionType.class))
        .put(session.sessionType(), session);
  }

  private static void closeExistingSession(
      ServerPlayer player, SessionType sessionType, SessionCloseReason reason) {
    EnumMap<SessionType, Session> playerSessions = sessionsByPlayer.get(player.getUUID());
    if (playerSessions == null) {
      return;
    }

    Session existing = playerSessions.get(sessionType);
    if (existing != null && existing.isOpen()) {
      removeAndClose(existing, player, reason);
    }
  }

  private static void removeAndClose(
      Session session, ServerPlayer player, SessionCloseReason reason) {
    session.close();
    sessionsById.remove(session.sessionId());
    EnumMap<SessionType, Session> playerSessions = sessionsByPlayer.get(session.ownerPlayerUuid());
    if (playerSessions != null) {
      playerSessions.remove(session.sessionType(), session);
      if (playerSessions.isEmpty()) {
        sessionsByPlayer.remove(session.ownerPlayerUuid(), playerSessions);
      }
    }
    NetworkHandlerManager.sendToPlayer(player, new CloseSessionPacket(session.sessionId(), reason));
  }

  private static PlayerState getOrCreatePlayerState(ServerPlayer player) {
    return PlayerStateService.get(player.getUUID())
        .orElseGet(() -> new PlayerState(player.getUUID()));
  }

  private static boolean isCurrentSession(Session session) {
    if (!session.isOpen() || sessionsById.get(session.sessionId()) != session) {
      return false;
    }
    EnumMap<SessionType, Session> playerSessions = sessionsByPlayer.get(session.ownerPlayerUuid());
    return playerSessions != null && playerSessions.get(session.sessionType()) == session;
  }

  private static boolean isExpired(Session session) {
    return System.currentTimeMillis() - session.openedAtMs() >= MAX_SESSION_AGE_MS;
  }

  private static void recordTrace(
      ServerPlayer player,
      Session session,
      ResourceLocation contentId,
      TraceEventType eventType,
      Optional<String> choiceId) {
    ExecutionTraceService.record(
        player.getUUID(),
        new ExecutionTraceEntry(
            System.currentTimeMillis(),
            session.sessionId(),
            contentId,
            eventType,
            ConditionResult.NONE,
            List.of(),
            choiceId,
            Optional.empty()));
  }

  private static List<String> filterAllowedChoiceIds(
      List<? extends ChoiceDefinition> choices, ConditionContext conditionContext) {
    List<String> allowed = new ArrayList<>();
    for (ChoiceDefinition choice : choices) {
      if (choice.conditions().evaluate(conditionContext)) {
        allowed.add(choice.id());
      }
    }

    return allowed;
  }

  private static Map<String, String> buildChoiceLabels(
      List<? extends ChoiceDefinition> choices, List<String> allowedChoiceIds) {
    Map<String, String> labels = new HashMap<>(allowedChoiceIds.size());
    for (ChoiceDefinition choice : choices) {
      if (allowedChoiceIds.contains(choice.id())) {
        labels.put(choice.id(), choice.labelKey());
      }
    }
    return labels;
  }
}
