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

package de.markusbordihn.dialogqueststoryengine.content.interaction;

import de.markusbordihn.dialogqueststoryengine.data.action.ActionDataSet;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionSource;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.TargetKind;
import de.markusbordihn.dialogqueststoryengine.data.interaction.content.InteractionBinding;
import de.markusbordihn.dialogqueststoryengine.data.interaction.content.InteractionDefinition;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;

public final class InteractionDefinitions {

  private InteractionDefinitions() {}

  public static Optional<InteractionEntry> toEntry(InteractionDefinition definition) {
    InteractionBinding binding = definition.binding();

    if (binding instanceof InteractionBinding.EntityBinding entityBinding) {
      return Optional.of(
          new InteractionEntry(
              InteractionSource.DATAPACK,
              definition.event(),
              entityBinding.targetId(),
              TargetKind.ENTITY,
              InteractionType.RIGHT_CLICK,
              definition.id().toString(),
              entityBinding.dimension(),
              null,
              new ActionDataSet()));
    }

    if (binding instanceof InteractionBinding.BlockBinding blockBinding) {
      return Optional.of(
          new InteractionEntry(
              InteractionSource.DATAPACK,
              definition.event(),
              UUID.nameUUIDFromBytes(
                  (blockBinding.dimension() + "@" + blockBinding.blockPos().toShortString())
                      .getBytes(StandardCharsets.UTF_8)),
              TargetKind.BLOCK,
              InteractionType.RIGHT_CLICK,
              definition.id().toString(),
              blockBinding.dimension(),
              blockBinding.blockPos(),
              new ActionDataSet()));
    }

    return Optional.empty();
  }
}
