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

package de.markusbordihn.dialogqueststoryengine.data.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class EnumKeysTest {

  @Test
  void byNameResolvesExactName() {
    assertEquals(Optional.of(FactScope.PLAYER), EnumKeys.byName(FactScope.class, "PLAYER"));
  }

  @Test
  void byNameIsCaseInsensitive() {
    assertEquals(Optional.of(FactScope.WORLD), EnumKeys.byName(FactScope.class, "world"));
    assertEquals(Optional.of(FactScope.SERVER), EnumKeys.byName(FactScope.class, "Server"));
  }

  @Test
  void byNameReturnsEmptyForUnknown() {
    assertTrue(EnumKeys.byName(FactScope.class, "galaxy").isEmpty());
  }

  @Test
  void byNameReturnsEmptyForNullOrBlank() {
    assertTrue(EnumKeys.byName(FactScope.class, null).isEmpty());
    assertTrue(EnumKeys.byName(FactScope.class, "").isEmpty());
    assertTrue(EnumKeys.byName(FactScope.class, "   ").isEmpty());
  }
}
