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

import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogNodeDefinition;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DialogRuntimeRegistryTest {

  private static final ResourceLocation ID_A = new ResourceLocation("test", "dialog_a");
  private static final ResourceLocation ID_B = new ResourceLocation("test", "dialog_b");

  private static JsonDialogRuntime runtimeFor(ResourceLocation id) {
    DialogNodeDefinition node = new DialogNodeDefinition("root", "npc", "text.key", List.of());
    return new JsonDialogRuntime(new DialogDefinition(id, 1, "root", Map.of("root", node)));
  }

  @AfterEach
  void clearRegistry() {
    DialogRuntimeRegistry.invalidateAll();
  }

  @Test
  void putAndGet() {
    DialogRuntimeRegistry.put(runtimeFor(ID_A));

    assertTrue(DialogRuntimeRegistry.get(ID_A).isPresent());
    assertEquals(ID_A, DialogRuntimeRegistry.get(ID_A).get().id());
  }

  @Test
  void getReturnsEmptyForUnknown() {
    assertTrue(DialogRuntimeRegistry.get(new ResourceLocation("test", "missing")).isEmpty());
  }

  @Test
  void invalidateAllEmptiesRegistry() {
    DialogRuntimeRegistry.put(runtimeFor(ID_A));
    DialogRuntimeRegistry.put(runtimeFor(ID_B));

    DialogRuntimeRegistry.invalidateAll();

    assertTrue(DialogRuntimeRegistry.get(ID_A).isEmpty());
    assertTrue(DialogRuntimeRegistry.get(ID_B).isEmpty());
  }

  @Test
  void putOverwritesPreviousEntry() {
    DialogRuntimeRegistry.put(runtimeFor(ID_A));
    DialogRuntimeRegistry.put(runtimeFor(ID_A));

    assertTrue(DialogRuntimeRegistry.get(ID_A).isPresent());
  }
}
