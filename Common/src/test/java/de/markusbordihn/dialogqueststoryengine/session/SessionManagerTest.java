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

import static org.junit.jupiter.api.Assertions.assertEquals;

import de.markusbordihn.dialogqueststoryengine.data.session.SessionState;
import de.markusbordihn.dialogqueststoryengine.data.session.SessionType;
import java.lang.reflect.Field;
import java.util.EnumMap;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class SessionManagerTest {

  @AfterEach
  void cleanup() {
    SessionManager.invalidateAll();
  }

  @Test
  void invalidateAllIsNoOpWhenEmpty() {
    SessionManager.invalidateAll();
  }

  @Test
  void invalidateAllTwiceIsNoOp() {
    SessionManager.invalidateAll();
    SessionManager.invalidateAll();
  }

  @Test
  void invalidatePlayerSessionsIsNoOpForUnknownPlayer() {
    SessionManager.invalidatePlayerSessions(UUID.randomUUID());
  }

  @Test
  @SuppressWarnings("unchecked")
  void invalidatePlayerSessionsInvalidatesSessionAndAllowsSecondCallAsNoOp() throws Exception {
    UUID playerUuid = UUID.randomUUID();
    UUID sessionId = UUID.randomUUID();
    DialogSession session =
        new DialogSession(sessionId, playerUuid, new ResourceLocation("test", "dialog"), "start");

    Field sessionsByIdField = SessionManager.class.getDeclaredField("sessionsById");
    sessionsByIdField.setAccessible(true);
    ((ConcurrentHashMap<UUID, Session>) sessionsByIdField.get(null)).put(sessionId, session);

    Field sessionsByPlayerField = SessionManager.class.getDeclaredField("sessionsByPlayer");
    sessionsByPlayerField.setAccessible(true);
    EnumMap<SessionType, Session> playerMap = new EnumMap<>(SessionType.class);
    playerMap.put(session.sessionType(), session);
    ((ConcurrentHashMap<UUID, EnumMap<SessionType, Session>>) sessionsByPlayerField.get(null))
        .put(playerUuid, playerMap);

    SessionManager.invalidatePlayerSessions(playerUuid);

    assertEquals(SessionState.INVALIDATED, session.state());
    SessionManager.invalidatePlayerSessions(playerUuid);
  }
}
