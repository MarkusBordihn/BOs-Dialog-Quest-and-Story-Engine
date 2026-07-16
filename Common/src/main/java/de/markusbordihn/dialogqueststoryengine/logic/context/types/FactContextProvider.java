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

package de.markusbordihn.dialogqueststoryengine.logic.context.types;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.data.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.data.text.ContextArgument;
import de.markusbordihn.dialogqueststoryengine.logic.context.NarrativeContextResolution;
import de.markusbordihn.dialogqueststoryengine.registry.ContextValueProvider;
import java.util.Locale;
import java.util.Optional;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

public final class FactContextProvider implements ContextValueProvider {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "fact");

  static final String PARAM_SCOPE = "scope";
  static final String PARAM_FACT = "fact";

  @Override
  public Component resolve(ContextArgument argument, NarrativeContextResolution resolution) {
    String fact = argument.param(PARAM_FACT);
    if (fact == null || resolution.playerState() == null) {
      return Component.empty();
    }

    FactValue value = resolution.playerState().getFact(FactScope.PLAYER, fact);
    return value != null ? Component.literal(value.displayValue()) : Component.empty();
  }

  @Override
  public Optional<String> validate(ContextArgument argument) {
    String scope = argument.param(PARAM_SCOPE);
    if (scope == null) {
      return Optional.of("missing required '" + PARAM_SCOPE + "' parameter");
    }
    if (!FactScope.PLAYER.name().equals(scope.toUpperCase(Locale.ROOT))) {
      return Optional.of("only the 'player' fact scope is supported");
    }

    String fact = argument.param(PARAM_FACT);
    if (fact == null || fact.isBlank()) {
      return Optional.of("missing required '" + PARAM_FACT + "' parameter");
    }
    return Optional.empty();
  }
}
