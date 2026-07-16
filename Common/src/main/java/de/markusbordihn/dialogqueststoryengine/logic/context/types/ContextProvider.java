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
import de.markusbordihn.dialogqueststoryengine.data.text.ContextArgument;
import de.markusbordihn.dialogqueststoryengine.logic.context.NarrativeContextResolution;
import de.markusbordihn.dialogqueststoryengine.registry.ContextValueProvider;
import java.util.Optional;
import java.util.Set;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class ContextProvider implements ContextValueProvider {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "context");

  static final String PARAM_KEY = "key";

  static final String PLAYER_NAME = "player_name";
  static final String PLAYER_UUID = "player_uuid";
  static final String DIMENSION = "dimension";
  static final String TARGET_NAME = "target_name";
  static final String TARGET_TYPE = "target_type";
  static final String TARGET_UUID = "target_uuid";
  static final String BLOCK_X = "block_x";
  static final String BLOCK_Y = "block_y";
  static final String BLOCK_Z = "block_z";

  private static final Set<String> KEYS =
      Set.of(
          PLAYER_NAME,
          PLAYER_UUID,
          DIMENSION,
          TARGET_NAME,
          TARGET_TYPE,
          TARGET_UUID,
          BLOCK_X,
          BLOCK_Y,
          BLOCK_Z);

  @Override
  public Component resolve(ContextArgument argument, NarrativeContextResolution resolution) {
    String key = argument.param(PARAM_KEY);
    if (key == null) {
      return Component.empty();
    }

    ServerPlayer player = resolution.player();
    return switch (key) {
      case PLAYER_NAME -> player != null ? player.getName() : Component.empty();
      case PLAYER_UUID ->
          player != null ? Component.literal(player.getUUID().toString()) : Component.empty();
      case DIMENSION ->
          player != null
              ? Component.literal(player.level().dimension().location().toString())
              : Component.empty();
      default -> Component.empty();
    };
  }

  @Override
  public Optional<String> validate(ContextArgument argument) {
    String key = argument.param(PARAM_KEY);
    if (key == null) {
      return Optional.of("missing required '" + PARAM_KEY + "' parameter");
    }
    if (!KEYS.contains(key)) {
      return Optional.of("unknown context key '" + key + "'");
    }
    return Optional.empty();
  }
}
