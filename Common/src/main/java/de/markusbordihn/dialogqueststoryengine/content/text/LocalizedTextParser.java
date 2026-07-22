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

package de.markusbordihn.dialogqueststoryengine.content.text;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.text.ContextArgument;
import de.markusbordihn.dialogqueststoryengine.data.text.LocalizedTextSource;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public final class LocalizedTextParser {

  private static final String SUFFIX_KEY = "_key";
  private static final String SUFFIX_ARGS = "_args";
  private static final String FIELD_TYPE = "type";

  private LocalizedTextParser() {}

  public static Optional<LocalizedTextSource> parse(
      JsonObject parent,
      String name,
      boolean required,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    String keyField = name + SUFFIX_KEY;
    String argumentsField = name + SUFFIX_ARGS;
    boolean hasLiteral = isString(parent, name);
    boolean hasKey = isString(parent, keyField);

    if (hasLiteral && hasKey) {
      issues.add(
          ContentIssue.of(IssueCode.LOCALIZED_TEXT_CONFLICT, contentType, id, filePath, name));
      return Optional.empty();
    }

    if (hasKey) {
      List<ContextArgument> arguments =
          parseArguments(parent, argumentsField, contentType, id, filePath, issues);
      return Optional.of(LocalizedTextSource.keyed(parent.get(keyField).getAsString(), arguments));
    }

    if (parent.has(argumentsField)) {
      issues.add(
          ContentIssue.of(
              IssueCode.LOCALIZED_ARGS_WITHOUT_KEY, contentType, id, filePath, argumentsField));
    }

    if (hasLiteral) {
      return Optional.of(LocalizedTextSource.literal(parent.get(name).getAsString()));
    }

    if (required) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, keyField));
    }
    return Optional.empty();
  }

  private static List<ContextArgument> parseArguments(
      JsonObject parent,
      String argumentsField,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    List<ContextArgument> arguments = new ArrayList<>();
    if (!parent.has(argumentsField) || !parent.get(argumentsField).isJsonArray()) {
      return arguments;
    }

    var array = parent.getAsJsonArray(argumentsField);
    for (int i = 0; i < array.size(); i++) {
      String path = argumentsField + "[" + i + "]";
      JsonElement element = array.get(i);
      if (!element.isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                contentType,
                id,
                filePath,
                path,
                Map.of("expected", "object")));
        continue;
      }
      parseArgument(element.getAsJsonObject(), path, contentType, id, filePath, issues)
          .ifPresent(arguments::add);
    }
    return arguments;
  }

  private static Optional<ContextArgument> parseArgument(
      JsonObject argumentJson,
      String path,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!isString(argumentJson, FIELD_TYPE)) {
      issues.add(
          ContentIssue.of(
              IssueCode.MISSING_FIELD, contentType, id, filePath, path + "." + FIELD_TYPE));
      return Optional.empty();
    }

    String typeString = argumentJson.get(FIELD_TYPE).getAsString();
    ResourceLocation type;
    try {
      type = new ResourceLocation(typeString);
    } catch (ResourceLocationException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              id,
              filePath,
              path + "." + FIELD_TYPE,
              Map.of("value", typeString)));
      return Optional.empty();
    }

    Map<String, String> parameters = new LinkedHashMap<>();
    for (Map.Entry<String, JsonElement> entry : argumentJson.entrySet()) {
      if (entry.getKey().equals(FIELD_TYPE)) {
        continue;
      }
      if (entry.getValue().isJsonPrimitive()) {
        parameters.put(entry.getKey(), entry.getValue().getAsString());
      }
    }

    ContextArgument argument = new ContextArgument(type, parameters);
    return Registries.CONTEXT_VALUES
        .get(type)
        .map(
            provider -> {
              provider
                  .validate(argument)
                  .ifPresent(
                      message ->
                          issues.add(
                              ContentIssue.of(
                                  IssueCode.INVALID_CONTEXT_ARGUMENT,
                                  contentType,
                                  id,
                                  filePath,
                                  path,
                                  Map.of("reason", message))));
              return Optional.of(argument);
            })
        .orElseGet(
            () -> {
              issues.add(
                  ContentIssue.of(
                      IssueCode.UNKNOWN_CONTEXT_PROVIDER,
                      contentType,
                      id,
                      filePath,
                      path + "." + FIELD_TYPE,
                      Map.of("type", type.toString())));
              return Optional.empty();
            });
  }

  private static boolean isString(JsonObject jsonObject, String field) {
    return jsonObject.has(field)
        && jsonObject.get(field).isJsonPrimitive()
        && jsonObject.get(field).getAsJsonPrimitive().isString();
  }
}
