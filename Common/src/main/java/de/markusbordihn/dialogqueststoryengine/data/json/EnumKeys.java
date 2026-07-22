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

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public final class EnumKeys {

  private static final Map<Class<?>, Map<String, Enum<?>>> LOOKUP_CACHE = new ConcurrentHashMap<>();

  private EnumKeys() {}

  public static <E extends Enum<E>> Optional<E> byName(Class<E> type, String value) {
    if (value == null || value.isBlank()) {
      return Optional.empty();
    }
    Map<String, Enum<?>> lookup = LOOKUP_CACHE.computeIfAbsent(type, EnumKeys::buildLookup);
    @SuppressWarnings("unchecked")
    E result = (E) lookup.get(value.toLowerCase(Locale.ROOT));
    return Optional.ofNullable(result);
  }

  private static Map<String, Enum<?>> buildLookup(Class<?> type) {
    Map<String, Enum<?>> lookup = new HashMap<>();
    for (Object constant : type.getEnumConstants()) {
      Enum<?> enumConstant = (Enum<?>) constant;
      lookup.put(enumConstant.name().toLowerCase(Locale.ROOT), enumConstant);
    }
    return lookup;
  }
}
