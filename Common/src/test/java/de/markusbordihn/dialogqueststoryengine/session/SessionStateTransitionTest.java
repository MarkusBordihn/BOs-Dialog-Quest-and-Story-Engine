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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.session.SessionState;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class SessionStateTransitionTest {

  private static final UUID PLAYER_UUID = UUID.fromString("11111111-1111-1111-1111-111111111111");
  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "dialog");

  private DialogSession newSession() {
    return new DialogSession(UUID.randomUUID(), PLAYER_UUID, DIALOG_ID, "start");
  }

  @Test
  void newSessionIsOpen() {
    DialogSession session = this.newSession();

    assertTrue(session.isOpen());
    assertEquals(SessionState.OPEN, session.state());
    assertEquals(0, session.revision());
  }

  @Test
  void bumpRevisionIncrementsRevision() {
    DialogSession session = this.newSession();

    session.bumpRevision();
    assertEquals(1, session.revision());

    session.bumpRevision();
    assertEquals(2, session.revision());
  }

  @Test
  void closeTransitionsToClosedState() {
    DialogSession session = this.newSession();

    session.close();

    assertFalse(session.isOpen());
    assertEquals(SessionState.CLOSED, session.state());
  }

  @Test
  void invalidateTransitionsToInvalidatedState() {
    DialogSession session = this.newSession();

    session.invalidate();

    assertFalse(session.isOpen());
    assertEquals(SessionState.INVALIDATED, session.state());
  }

  @Test
  void sessionIdAndOwnerArePreservedAfterClose() {
    UUID sessionId = UUID.randomUUID();
    DialogSession session = new DialogSession(sessionId, PLAYER_UUID, DIALOG_ID, "start");

    session.close();

    assertEquals(sessionId, session.sessionId());
    assertEquals(PLAYER_UUID, session.ownerPlayerUuid());
  }
}
