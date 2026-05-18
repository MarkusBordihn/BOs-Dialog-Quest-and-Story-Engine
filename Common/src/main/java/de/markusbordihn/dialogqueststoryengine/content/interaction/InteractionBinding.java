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

import com.google.gson.JsonObject;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;

public sealed interface InteractionBinding
    permits InteractionBinding.EntityBinding,
        InteractionBinding.BlockBinding,
        InteractionBinding.PlayerBinding,
        InteractionBinding.InventoryBinding,
        InteractionBinding.WorldBinding,
        InteractionBinding.CustomBinding,
        InteractionBinding.UnboundBinding {

  InteractionBindingKind kind();

  record EntityBinding(UUID targetId, ResourceLocation dimension) implements InteractionBinding {
    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.ENTITY;
    }
  }

  record BlockBinding(BlockPos pos, ResourceLocation dimension) implements InteractionBinding {
    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.BLOCK;
    }
  }

  record PlayerBinding() implements InteractionBinding {
    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.PLAYER;
    }
  }

  record InventoryBinding() implements InteractionBinding {
    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.INVENTORY;
    }
  }

  record WorldBinding(ResourceLocation dimension) implements InteractionBinding {
    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.WORLD;
    }
  }

  record CustomBinding(ResourceLocation typeId, JsonObject payload) implements InteractionBinding {
    public CustomBinding {
      payload = payload.deepCopy();
    }

    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.CUSTOM;
    }
  }

  final class UnboundBinding implements InteractionBinding {
    public static final UnboundBinding INSTANCE = new UnboundBinding();

    private UnboundBinding() {}

    @Override
    public InteractionBindingKind kind() {
      return InteractionBindingKind.UNBOUND;
    }
  }
}
