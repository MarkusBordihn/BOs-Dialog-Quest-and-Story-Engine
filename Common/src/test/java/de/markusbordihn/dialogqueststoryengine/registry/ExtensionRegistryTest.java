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

package de.markusbordihn.dialogqueststoryengine.registry;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class ExtensionRegistryTest {

  private static final ResourceLocation TEST_ID = new ResourceLocation(Constants.MOD_ID, "test_entry");
  private static final ResourceLocation OTHER_ID = new ResourceLocation(Constants.MOD_ID, "other_entry");

  @Test
  void registerAndGetReturnsValue() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    registry.register(TEST_ID, "handler");
    assertEquals(Optional.of("handler"), registry.get(TEST_ID));
  }

  @Test
  void getMissingIdReturnsEmpty() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    assertEquals(Optional.empty(), registry.get(TEST_ID));
  }

  @Test
  void containsReflectsRegisteredEntries() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    assertFalse(registry.contains(TEST_ID));
    registry.register(TEST_ID, "handler");
    assertTrue(registry.contains(TEST_ID));
  }

  @Test
  void sizeTracksEntries() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    assertEquals(0, registry.size());
    registry.register(TEST_ID, "first");
    assertEquals(1, registry.size());
    registry.register(OTHER_ID, "second");
    assertEquals(2, registry.size());
  }

  @Test
  void isFrozenFalseByDefault() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    assertFalse(registry.isFrozen());
  }

  @Test
  void isFrozenTrueAfterFreeze() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    registry.freeze();
    assertTrue(registry.isFrozen());
  }

  @Test
  void freezeIsIdempotent() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    registry.freeze();
    registry.freeze();
    assertTrue(registry.isFrozen());
  }

  @Test
  void freezeBlocksRegistration() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    registry.freeze();
    assertThrows(IllegalStateException.class, () -> registry.register(TEST_ID, "handler"));
  }

  @Test
  void keysReturnsUnmodifiableView() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    registry.register(TEST_ID, "handler");
    assertThrows(UnsupportedOperationException.class, () -> registry.keys().add(OTHER_ID));
  }

  @Test
  void nameReturnsDisplayNameOfRegistryType() {
    ExtensionRegistry<String> registry = new ExtensionRegistry<>(RegistryType.CONDITIONS);
    assertEquals(RegistryType.CONDITIONS.displayName(), registry.name());
  }
}
