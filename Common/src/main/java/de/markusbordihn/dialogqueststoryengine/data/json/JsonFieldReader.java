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

package de.markusbordihn.dialogqueststoryengine.data.json;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public final class JsonFieldReader {

  public static final String FIELD_SCHEMA = "schema";

  private JsonFieldReader() {}

  private static void reportMissing(
      ContentType contentType,
      ResourceLocation id,
      String file,
      String field,
      List<ContentIssue> issues) {
    issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, file, field));
  }

  private static void reportInvalidType(
      ContentType contentType,
      ResourceLocation id,
      String file,
      String field,
      String expected,
      List<ContentIssue> issues) {
    issues.add(
        ContentIssue.of(
            IssueCode.INVALID_FIELD_TYPE,
            contentType,
            id,
            file,
            field,
            Map.of("expected", expected)));
  }

  public static Optional<Integer> readSchema(
      JsonObject jsonObject,
      ContentType contentType,
      int expectedSchema,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(FIELD_SCHEMA)) {
      issues.add(ContentIssue.of(IssueCode.MISSING_SCHEMA, contentType, id, file, FIELD_SCHEMA));
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(FIELD_SCHEMA);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      reportInvalidType(contentType, id, file, FIELD_SCHEMA, "integer", issues);
      return Optional.empty();
    }

    int schema = element.getAsInt();
    if (schema != expectedSchema) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNSUPPORTED_SCHEMA,
              contentType,
              id,
              file,
              FIELD_SCHEMA,
              Map.of("found", String.valueOf(schema), "expected", String.valueOf(expectedSchema))));
      return Optional.empty();
    }

    return Optional.of(schema);
  }

  public static Optional<String> readString(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      reportMissing(contentType, id, file, field, issues);
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
      reportInvalidType(contentType, id, file, field, "string", issues);
      return Optional.empty();
    }

    String value = element.getAsString();
    if (value.isBlank()) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD,
              contentType,
              id,
              file,
              field,
              Map.of("reason", "must not be blank")));
      return Optional.empty();
    }

    return Optional.of(value);
  }

  public static Optional<Integer> readInt(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      reportMissing(contentType, id, file, field, issues);
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      reportInvalidType(contentType, id, file, field, "integer", issues);
      return Optional.empty();
    }

    return Optional.of(element.getAsInt());
  }

  public static Optional<Float> readFloat(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      reportMissing(contentType, id, file, field, issues);
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      reportInvalidType(contentType, id, file, field, "number", issues);
      return Optional.empty();
    }

    return Optional.of(element.getAsFloat());
  }

  public static Optional<Boolean> readBoolean(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      reportMissing(contentType, id, file, field, issues);
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
      reportInvalidType(contentType, id, file, field, "boolean", issues);
      return Optional.empty();
    }

    return Optional.of(element.getAsBoolean());
  }

  public static Optional<JsonObject> readObject(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      reportMissing(contentType, id, file, field, issues);
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonObject()) {
      reportInvalidType(contentType, id, file, field, "object", issues);
      return Optional.empty();
    }

    return Optional.of(element.getAsJsonObject());
  }

  public static Optional<JsonArray> readArray(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      reportMissing(contentType, id, file, field, issues);
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonArray()) {
      reportInvalidType(contentType, id, file, field, "array", issues);
      return Optional.empty();
    }

    return Optional.of(element.getAsJsonArray());
  }

  public static Optional<ResourceLocation> readResourceLocation(
      JsonObject jsonObject,
      String field,
      ContentType contentType,
      ResourceLocation id,
      String file,
      List<ContentIssue> issues) {
    Optional<String> rawValue = readString(jsonObject, field, contentType, id, file, issues);
    if (rawValue.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(new ResourceLocation(rawValue.get()));
    } catch (ResourceLocationException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              id,
              file,
              field,
              Map.of("value", rawValue.get())));
      return Optional.empty();
    }
  }
}
