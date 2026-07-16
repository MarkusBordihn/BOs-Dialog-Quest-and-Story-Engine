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

package de.markusbordihn.dialogqueststoryengine.content.dialog.runtime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogNodeDefinition;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class JsonDialogRuntimeTest {

  private static final ResourceLocation DIALOG_ID = new ResourceLocation("test", "greet");

  private static DialogDefinition testDefinition() {
    DialogNodeDefinition startNode =
        new DialogNodeDefinition("start", "npc.guard", "dialog.guard.greet", List.of());
    DialogNodeDefinition endNode =
        new DialogNodeDefinition("end", "npc.guard", "dialog.guard.bye", List.of());
    return new DialogDefinition(DIALOG_ID, 1, "start", Map.of("start", startNode, "end", endNode));
  }

  @Test
  void idDelegatesToDefinition() {
    JsonDialogRuntime runtime = new JsonDialogRuntime(testDefinition());

    assertEquals(DIALOG_ID, runtime.id());
  }

  @Test
  void startNodeIdDelegatesToDefinition() {
    JsonDialogRuntime runtime = new JsonDialogRuntime(testDefinition());

    assertEquals("start", runtime.startNodeId());
  }

  @Test
  void getNodeReturnsExistingNode() {
    JsonDialogRuntime runtime = new JsonDialogRuntime(testDefinition());

    assertTrue(runtime.getNode("start").isPresent());
    assertEquals("dialog.guard.greet", runtime.getNode("start").get().textKey());
  }

  @Test
  void getNodeReturnsEmptyForUnknownId() {
    JsonDialogRuntime runtime = new JsonDialogRuntime(testDefinition());

    assertTrue(runtime.getNode("missing").isEmpty());
  }
}
