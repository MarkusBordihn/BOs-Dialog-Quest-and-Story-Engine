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

package de.markusbordihn.dialogqueststoryengine.registry;

import de.markusbordihn.dialogqueststoryengine.Constants;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ExtensionRegistry<V> {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private final RegistryType registryType;
  private final Map<ResourceLocation, V> entries = new LinkedHashMap<>();
  private boolean frozen = false;

  public ExtensionRegistry(RegistryType registryType) {
    this.registryType = registryType;
  }

  public void register(ResourceLocation id, V handler) {
    if (this.frozen) {
      throw new IllegalStateException(
          Constants.LOG_PREFIX
              + " Registry '"
              + this.registryType.displayName()
              + "' is frozen - cannot register "
              + id);
    }

    if (this.entries.containsKey(id)) {
      log.warn(
          "{} Registry '{}': duplicate id {} - overwriting.",
          Constants.LOG_PREFIX,
          this.registryType.displayName(),
          id);
    }
    this.entries.put(id, handler);
  }

  public Optional<V> get(ResourceLocation id) {
    return Optional.ofNullable(this.entries.get(id));
  }

  public boolean contains(ResourceLocation id) {
    return this.entries.containsKey(id);
  }

  public int size() {
    return this.entries.size();
  }

  public Set<ResourceLocation> keys() {
    return Collections.unmodifiableSet(this.entries.keySet());
  }

  public Collection<V> values() {
    return Collections.unmodifiableCollection(this.entries.values());
  }

  public boolean isFrozen() {
    return this.frozen;
  }

  public void freeze() {
    if (this.frozen) {
      return;
    }

    this.frozen = true;
    log.info(
        "{} Registry '{}' frozen with {} entries.",
        Constants.LOG_PREFIX,
        this.registryType.displayName(),
        this.entries.size());
  }

  public String name() {
    return this.registryType.displayName();
  }
}
