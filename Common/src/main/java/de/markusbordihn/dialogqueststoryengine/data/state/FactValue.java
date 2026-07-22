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

package de.markusbordihn.dialogqueststoryengine.data.state;

import de.markusbordihn.dialogqueststoryengine.data.json.EnumKeys;
import java.util.OptionalDouble;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;

public sealed interface FactValue
    permits FactValue.BooleanValue,
        FactValue.LongValue,
        FactValue.DoubleValue,
        FactValue.StringValue,
        FactValue.ResourceLocationValue {

  String TAG_TYPE = "type";
  String TAG_VALUE = "value";

  static FactValue of(boolean value) {
    return new BooleanValue(value);
  }

  static FactValue of(long value) {
    return new LongValue(value);
  }

  static FactValue of(double value) {
    return new DoubleValue(value);
  }

  static FactValue of(String value) {
    return new StringValue(value);
  }

  static FactValue of(ResourceLocation value) {
    return new ResourceLocationValue(value);
  }

  static CompoundTag toNbt(FactValue factValue) {
    CompoundTag tag = new CompoundTag();
    tag.putString(TAG_TYPE, factValue.type().name());
    if (factValue instanceof BooleanValue booleanValue) {
      tag.putBoolean(TAG_VALUE, booleanValue.value());
    } else if (factValue instanceof LongValue longValue) {
      tag.putLong(TAG_VALUE, longValue.value());
    } else if (factValue instanceof DoubleValue doubleValue) {
      tag.putDouble(TAG_VALUE, doubleValue.value());
    } else if (factValue instanceof StringValue stringValue) {
      tag.putString(TAG_VALUE, stringValue.value());
    } else if (factValue instanceof ResourceLocationValue resourceLocationValue) {
      tag.putString(TAG_VALUE, resourceLocationValue.value().toString());
    }
    return tag;
  }

  static FactValue fromNbt(CompoundTag tag) {
    if (!tag.contains(TAG_TYPE) || !tag.contains(TAG_VALUE)) {
      return null;
    }
    Type type = Type.fromName(tag.getString(TAG_TYPE));
    if (type == null) {
      return null;
    }
    return switch (type) {
      case BOOLEAN -> new BooleanValue(tag.getBoolean(TAG_VALUE));
      case LONG -> new LongValue(tag.getLong(TAG_VALUE));
      case DOUBLE -> new DoubleValue(tag.getDouble(TAG_VALUE));
      case STRING -> new StringValue(tag.getString(TAG_VALUE));
      case RESOURCE_LOCATION -> {
        ResourceLocation location = ResourceLocation.tryParse(tag.getString(TAG_VALUE));
        yield location != null ? new ResourceLocationValue(location) : null;
      }
    };
  }

  Type type();

  String displayValue();

  default OptionalDouble numericValue() {
    return OptionalDouble.empty();
  }

  enum Type {
    BOOLEAN,
    LONG,
    DOUBLE,
    STRING,
    RESOURCE_LOCATION;

    public static Type fromName(String name) {
      return EnumKeys.byName(Type.class, name).orElse(null);
    }
  }

  record BooleanValue(boolean value) implements FactValue {
    @Override
    public Type type() {
      return Type.BOOLEAN;
    }

    @Override
    public String displayValue() {
      return Boolean.toString(this.value);
    }
  }

  record LongValue(long value) implements FactValue {
    @Override
    public Type type() {
      return Type.LONG;
    }

    @Override
    public String displayValue() {
      return Long.toString(this.value);
    }

    @Override
    public OptionalDouble numericValue() {
      return OptionalDouble.of(this.value);
    }
  }

  record DoubleValue(double value) implements FactValue {
    @Override
    public Type type() {
      return Type.DOUBLE;
    }

    @Override
    public String displayValue() {
      return Double.toString(this.value);
    }

    @Override
    public OptionalDouble numericValue() {
      return OptionalDouble.of(this.value);
    }
  }

  record StringValue(String value) implements FactValue {
    @Override
    public Type type() {
      return Type.STRING;
    }

    @Override
    public String displayValue() {
      return this.value;
    }
  }

  record ResourceLocationValue(ResourceLocation value) implements FactValue {
    @Override
    public Type type() {
      return Type.RESOURCE_LOCATION;
    }

    @Override
    public String displayValue() {
      return this.value.toString();
    }
  }
}
