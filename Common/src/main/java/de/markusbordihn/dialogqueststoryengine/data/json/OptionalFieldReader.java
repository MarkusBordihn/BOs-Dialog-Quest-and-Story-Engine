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

public final class OptionalFieldReader {

  private OptionalFieldReader() {}

  public static Optional<String> string(
      JsonObject jsonObject,
      String field,
      String path,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      return Optional.empty();
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
      reportInvalidType(contentType, id, filePath, path, "string", issues);
      return Optional.empty();
    }

    return Optional.of(element.getAsString());
  }

  public static int integer(
      JsonObject jsonObject,
      String field,
      String path,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      return 0;
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isNumber()) {
      reportInvalidType(contentType, id, filePath, path, "integer", issues);
      return 0;
    }

    return element.getAsInt();
  }

  public static boolean booleanValue(
      JsonObject jsonObject,
      String field,
      String path,
      boolean defaultValue,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has(field)) {
      return defaultValue;
    }

    JsonElement element = jsonObject.get(field);
    if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
      reportInvalidType(contentType, id, filePath, path, "boolean", issues);
      return defaultValue;
    }

    return element.getAsBoolean();
  }

  public static Optional<ResourceLocation> resourceLocation(
      JsonObject jsonObject,
      String field,
      String path,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<String> raw = string(jsonObject, field, path, contentType, id, filePath, issues);
    if (raw.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(new ResourceLocation(raw.get()));
    } catch (ResourceLocationException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              id,
              filePath,
              path,
              Map.of("value", raw.get())));
      return Optional.empty();
    }
  }

  private static void reportInvalidType(
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      String path,
      String expected,
      List<ContentIssue> issues) {
    issues.add(
        ContentIssue.of(
            IssueCode.INVALID_FIELD_TYPE,
            contentType,
            id,
            filePath,
            path,
            Map.of("expected", expected)));
  }
}
