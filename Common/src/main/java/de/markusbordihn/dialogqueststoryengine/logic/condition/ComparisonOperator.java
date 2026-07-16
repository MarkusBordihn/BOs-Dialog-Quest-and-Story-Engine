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

package de.markusbordihn.dialogqueststoryengine.logic.condition;

import java.util.Locale;
import java.util.Optional;

public enum ComparisonOperator {
  EQUALS,
  NOT_EQUALS,
  LESS_THAN,
  LESS_OR_EQUAL,
  GREATER_THAN,
  GREATER_OR_EQUAL;

  public static Optional<ComparisonOperator> fromKey(String key) {
    if (key == null) {
      return Optional.empty();
    }

    try {
      return Optional.of(valueOf(key.toUpperCase(Locale.ROOT)));
    } catch (IllegalArgumentException e) {
      return Optional.empty();
    }
  }

  public boolean requiresNumericOrder() {
    return this != EQUALS && this != NOT_EQUALS;
  }

  public boolean compareNumeric(double actual, double expected) {
    return switch (this) {
      case EQUALS -> actual == expected;
      case NOT_EQUALS -> actual != expected;
      case LESS_THAN -> actual < expected;
      case LESS_OR_EQUAL -> actual <= expected;
      case GREATER_THAN -> actual > expected;
      case GREATER_OR_EQUAL -> actual >= expected;
    };
  }
}
