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

package de.markusbordihn.dialogqueststoryengine.data.interaction;

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.Locale;
import net.minecraft.resources.ResourceLocation;

public enum InteractionEventType {
  ON_ENTITY_INTERACT,
  ON_BLOCK_INTERACT,
  ON_STEP_ON,
  ON_COMMAND,
  ON_HOLOPAD_USE,
  ON_EASY_NPC_INTERACT;

  private final ResourceLocation resourceLocation;

  InteractionEventType() {
    this.resourceLocation =
        new ResourceLocation(Constants.MOD_ID, this.name().toLowerCase(Locale.ROOT));
  }

  public static InteractionEventType fromName(String name) {
    for (InteractionEventType type : values()) {
      if (type.name().equals(name)) {
        return type;
      }
    }

    return null;
  }

  public static InteractionEventType fromJsonKey(String key) {
    if (key == null) {
      return null;
    }

    return fromName(key.toUpperCase(Locale.ROOT));
  }

  public ResourceLocation resourceLocation() {
    return this.resourceLocation;
  }
}
