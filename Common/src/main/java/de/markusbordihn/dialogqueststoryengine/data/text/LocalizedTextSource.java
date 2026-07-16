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

package de.markusbordihn.dialogqueststoryengine.data.text;

import de.markusbordihn.dialogqueststoryengine.logic.context.NarrativeContextResolution;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.network.chat.Component;

public record LocalizedTextSource(boolean literal, String value, List<ContextArgument> arguments) {

  public LocalizedTextSource {
    arguments = List.copyOf(arguments);
  }

  public static LocalizedTextSource literal(String text) {
    return new LocalizedTextSource(true, text, List.of());
  }

  public static LocalizedTextSource keyed(String key, List<ContextArgument> arguments) {
    return new LocalizedTextSource(false, key, arguments);
  }

  public LocalizedText resolve(NarrativeContextResolution resolution) {
    if (this.literal) {
      return LocalizedText.literal(this.value);
    }

    List<Component> resolved = new ArrayList<>(this.arguments.size());
    for (ContextArgument argument : this.arguments) {
      resolved.add(argument.resolve(resolution));
    }
    return LocalizedText.translatable(this.value, resolved);
  }
}
