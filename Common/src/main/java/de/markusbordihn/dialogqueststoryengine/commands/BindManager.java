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

package de.markusbordihn.dialogqueststoryengine.commands;

import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import java.util.WeakHashMap;
import net.minecraft.world.entity.player.Player;

public final class BindManager {

  private static final WeakHashMap<Player, BindContext> activeBinds = new WeakHashMap<>();

  private BindManager() {}

  public static void startBind(Player player, InteractionType type, String label) {
    activeBinds.put(player, new BindContext(type, label, false));
  }

  public static void startUnbind(Player player) {
    activeBinds.put(player, new BindContext(null, null, true));
  }

  public static void startUnbind(Player player, InteractionType type) {
    activeBinds.put(player, new BindContext(type, null, true));
  }

  public static boolean isBinding(Player player) {
    return activeBinds.containsKey(player);
  }

  public static BindContext consumeBind(Player player) {
    return activeBinds.remove(player);
  }

  public record BindContext(InteractionType type, String label, boolean unbind) {}
}
