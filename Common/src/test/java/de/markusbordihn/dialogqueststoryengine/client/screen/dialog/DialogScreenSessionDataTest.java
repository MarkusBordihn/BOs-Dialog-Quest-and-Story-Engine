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

package de.markusbordihn.dialogqueststoryengine.client.screen.dialog;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class DialogScreenSessionDataTest {

  private static final UUID SESSION_ID = UUID.fromString("cccccccc-cccc-cccc-cccc-cccccccccccc");
  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "intro");

  @Test
  void allowedChoiceIdsIsDefensivelyCopied() {
    List<String> mutableList = new ArrayList<>(List.of("choice_a", "choice_b"));
    DialogSessionData sessionData =
        new DialogSessionData(
            SESSION_ID, DIALOG_ID, "start", "npc.name", "dialog.text", mutableList, Map.of(), 1);

    mutableList.add("choice_c");

    assertEquals(2, sessionData.allowedChoiceIds().size());
  }

  @Test
  void allowedChoiceIdsIsUnmodifiable() {
    DialogSessionData sessionData =
        new DialogSessionData(
            SESSION_ID,
            DIALOG_ID,
            "start",
            "npc.name",
            "dialog.text",
            List.of("choice_a"),
            Map.of(),
            1);

    assertThrows(
        UnsupportedOperationException.class, () -> sessionData.allowedChoiceIds().add("choice_b"));
  }

  @Test
  void revisionIsPreserved() {
    DialogSessionData sessionData =
        new DialogSessionData(
            SESSION_ID, DIALOG_ID, "start", "npc.name", "dialog.text", List.of(), Map.of(), 42);

    assertEquals(42, sessionData.revision());
  }

  @Test
  void sessionIdAndDialogIdAreAccessible() {
    DialogSessionData sessionData =
        new DialogSessionData(
            SESSION_ID, DIALOG_ID, "start", "npc.name", "dialog.text", List.of(), Map.of(), 0);

    assertEquals(SESSION_ID, sessionData.sessionId());
    assertEquals(DIALOG_ID, sessionData.dialogId());
  }
}
