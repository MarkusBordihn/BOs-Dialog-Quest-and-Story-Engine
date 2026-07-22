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

package de.markusbordihn.dialogqueststoryengine.data.theme;

import de.markusbordihn.dialogqueststoryengine.data.theme.option.ThemeOption;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public record ThemeLayoutContract(
    ResourceLocation layoutId,
    Set<String> requiredAreas,
    Set<String> optionalAreas,
    Set<String> requiredSprites,
    Set<String> optionalSprites,
    Set<String> requiredColors,
    Set<String> optionalColors,
    Map<String, ThemeOption<?>> options,
    ThemeScaleLimits defaultScaleLimits) {

  public ThemeLayoutContract {
    requiredAreas = Set.copyOf(requiredAreas);
    optionalAreas = Set.copyOf(optionalAreas);
    requiredSprites = Set.copyOf(requiredSprites);
    optionalSprites = Set.copyOf(optionalSprites);
    requiredColors = Set.copyOf(requiredColors);
    optionalColors = Set.copyOf(optionalColors);
    options = Map.copyOf(options);
  }

  public static Builder builder(ResourceLocation layoutId) {
    return new Builder(layoutId);
  }

  public boolean knowsArea(String name) {
    return this.requiredAreas.contains(name) || this.optionalAreas.contains(name);
  }

  public boolean knowsSprite(String name) {
    return this.requiredSprites.contains(name) || this.optionalSprites.contains(name);
  }

  public boolean knowsColor(String name) {
    return this.requiredColors.contains(name) || this.optionalColors.contains(name);
  }

  public static final class Builder {
    private final ResourceLocation layoutId;
    private final Set<String> requiredAreas = new HashSet<>();
    private final Set<String> optionalAreas = new HashSet<>();
    private final Set<String> requiredSprites = new HashSet<>();
    private final Set<String> optionalSprites = new HashSet<>();
    private final Set<String> requiredColors = new HashSet<>();
    private final Set<String> optionalColors = new HashSet<>();
    private final Map<String, ThemeOption<?>> options = new LinkedHashMap<>();
    private ThemeScaleLimits defaultScaleLimits = ThemeScaleLimits.DEFAULT;

    private Builder(ResourceLocation layoutId) {
      this.layoutId = layoutId;
    }

    public Builder requiredAreas(String... names) {
      for (String name : names) {
        this.requiredAreas.add(name);
      }
      return this;
    }

    public Builder optionalAreas(String... names) {
      for (String name : names) {
        this.optionalAreas.add(name);
      }
      return this;
    }

    public Builder requiredSprites(String... names) {
      for (String name : names) {
        this.requiredSprites.add(name);
      }
      return this;
    }

    public Builder optionalSprites(String... names) {
      for (String name : names) {
        this.optionalSprites.add(name);
      }
      return this;
    }

    public Builder colors(String... names) {
      for (String name : names) {
        this.optionalColors.add(name);
      }
      return this;
    }

    public Builder option(ThemeOption<?> option) {
      this.options.put(option.key(), option);
      return this;
    }

    public Builder scaleLimits(ThemeScaleLimits limits) {
      this.defaultScaleLimits = limits;
      return this;
    }

    public ThemeLayoutContract build() {
      return new ThemeLayoutContract(
          this.layoutId,
          this.requiredAreas,
          this.optionalAreas,
          this.requiredSprites,
          this.optionalSprites,
          this.requiredColors,
          this.optionalColors,
          this.options,
          this.defaultScaleLimits);
    }
  }
}
