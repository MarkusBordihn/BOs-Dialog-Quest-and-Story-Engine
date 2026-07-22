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

package de.markusbordihn.dialogqueststoryengine.data.theme.option;

import java.util.Optional;
import java.util.function.Function;

public final class ThemeOption<T> {

  private final String key;
  private final Kind kind;
  private final T defaultValue;
  private final Function<String, Optional<T>> enumParser;

  private ThemeOption(
      String key, Kind kind, T defaultValue, Function<String, Optional<T>> enumParser) {
    this.key = key;
    this.kind = kind;
    this.defaultValue = defaultValue;
    this.enumParser = enumParser;
  }

  public static <E extends Enum<E>> ThemeOption<E> ofEnum(
      String key, E defaultValue, Function<String, Optional<E>> parser) {
    return new ThemeOption<>(key, Kind.ENUM, defaultValue, parser);
  }

  public static ThemeOption<Boolean> ofBoolean(String key, boolean defaultValue) {
    return new ThemeOption<>(key, Kind.BOOLEAN, defaultValue, null);
  }

  public String key() {
    return this.key;
  }

  public Kind kind() {
    return this.kind;
  }

  public T defaultValue() {
    return this.defaultValue;
  }

  public Optional<T> parse(String stringValue) {
    if (this.kind != Kind.ENUM) {
      throw new IllegalStateException("parse(String) is only valid for ENUM options: " + this.key);
    }
    return this.enumParser.apply(stringValue);
  }

  public enum Kind {
    ENUM,
    BOOLEAN
  }
}
