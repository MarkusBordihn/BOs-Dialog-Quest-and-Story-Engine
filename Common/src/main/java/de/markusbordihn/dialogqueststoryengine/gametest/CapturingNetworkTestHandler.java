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

package de.markusbordihn.dialogqueststoryengine.gametest;

import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerInterface;
import de.markusbordihn.dialogqueststoryengine.network.NetworkHandlerManager;
import de.markusbordihn.dialogqueststoryengine.network.NetworkMessageRecord;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public final class CapturingNetworkTestHandler implements NetworkHandlerInterface {

  private static final String HANDLER_FIELD = "networkHandler";

  private final List<NetworkMessageRecord> captured = new ArrayList<>();
  private NetworkHandlerInterface previous;

  private CapturingNetworkTestHandler() {}

  public static CapturingNetworkTestHandler install() {
    CapturingNetworkTestHandler handler = new CapturingNetworkTestHandler();
    handler.previous = swapHandler(handler);
    return handler;
  }

  private static NetworkHandlerInterface swapHandler(
      NetworkHandlerInterface networkHandlerInterface) {
    try {
      Field field = NetworkHandlerManager.class.getDeclaredField(HANDLER_FIELD);
      field.setAccessible(true);
      NetworkHandlerInterface previous = (NetworkHandlerInterface) field.get(null);
      field.set(null, networkHandlerInterface);
      return previous;
    } catch (ReflectiveOperationException exception) {
      throw new IllegalStateException("Failed to swap network handler for tests", exception);
    }
  }

  public void restore() {
    swapHandler(this.previous);
  }

  public List<NetworkMessageRecord> captured() {
    return this.captured;
  }

  public void clear() {
    this.captured.clear();
  }

  public boolean has(Class<? extends NetworkMessageRecord> type) {
    return this.captured.stream().anyMatch(type::isInstance);
  }

  public long count(Class<? extends NetworkMessageRecord> type) {
    return this.captured.stream().filter(type::isInstance).count();
  }

  public <T extends NetworkMessageRecord> T last(Class<T> type) {
    for (int i = this.captured.size() - 1; i >= 0; i--) {
      if (type.isInstance(this.captured.get(i))) {
        return type.cast(this.captured.get(i));
      }
    }
    throw new IllegalStateException("No " + type.getSimpleName() + " captured");
  }

  @Override
  public <M extends NetworkMessageRecord> void registerClientNetworkMessageHandler(
      ResourceLocation messageId, Class<M> networkMessage, Function<FriendlyByteBuf, M> creator) {}

  @Override
  public <M extends NetworkMessageRecord> void registerServerNetworkMessageHandler(
      ResourceLocation messageId, Class<M> networkMessage, Function<FriendlyByteBuf, M> creator) {}

  @Override
  public void sendToPlayer(ServerPlayer serverPlayer, NetworkMessageRecord networkMessageRecord) {
    this.captured.add(networkMessageRecord);
  }

  @Override
  public void sendToServer(NetworkMessageRecord networkMessageRecord) {}
}
