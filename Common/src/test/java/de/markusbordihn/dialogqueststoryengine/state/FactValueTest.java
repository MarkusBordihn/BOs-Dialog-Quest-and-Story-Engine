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

package de.markusbordihn.dialogqueststoryengine.state;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class FactValueTest {

  @Test
  void booleanRoundTrip() {
    FactValue original = FactValue.of(true);
    CompoundTag nbt = FactValue.toNbt(original);
    FactValue restored = FactValue.fromNbt(nbt);

    assertEquals(FactValue.Type.BOOLEAN, restored.type());
    assertTrue(((FactValue.BooleanValue) restored).value());
  }

  @Test
  void longRoundTrip() {
    FactValue original = FactValue.of(42L);
    CompoundTag nbt = FactValue.toNbt(original);
    FactValue restored = FactValue.fromNbt(nbt);

    assertEquals(FactValue.Type.LONG, restored.type());
    assertEquals(42L, ((FactValue.LongValue) restored).value());
  }

  @Test
  void doubleRoundTrip() {
    FactValue original = FactValue.of(3.14);
    CompoundTag nbt = FactValue.toNbt(original);
    FactValue restored = FactValue.fromNbt(nbt);

    assertEquals(FactValue.Type.DOUBLE, restored.type());
    assertEquals(3.14, ((FactValue.DoubleValue) restored).value(), 0.0001);
  }

  @Test
  void stringRoundTrip() {
    FactValue original = FactValue.of("hello");
    CompoundTag nbt = FactValue.toNbt(original);
    FactValue restored = FactValue.fromNbt(nbt);

    assertEquals(FactValue.Type.STRING, restored.type());
    assertEquals("hello", ((FactValue.StringValue) restored).value());
  }

  @Test
  void resourceLocationRoundTrip() {
    ResourceLocation id = new ResourceLocation("test", "item");
    FactValue original = FactValue.of(id);
    CompoundTag nbt = FactValue.toNbt(original);
    FactValue restored = FactValue.fromNbt(nbt);

    assertEquals(FactValue.Type.RESOURCE_LOCATION, restored.type());
    assertEquals(id, ((FactValue.ResourceLocationValue) restored).value());
  }

  @Test
  void fromNbtReturnsNullForMissingType() {
    assertNull(FactValue.fromNbt(new CompoundTag()));
  }

  @Test
  void fromNbtReturnsNullForUnknownType() {
    CompoundTag tag = new CompoundTag();
    tag.putString("type", "UNKNOWN_TYPE");
    tag.putString("value", "x");

    assertNull(FactValue.fromNbt(tag));
  }

  @Test
  void falseBooleanRoundTrip() {
    FactValue original = FactValue.of(false);
    FactValue restored = FactValue.fromNbt(FactValue.toNbt(original));

    assertFalse(((FactValue.BooleanValue) restored).value());
  }
}
