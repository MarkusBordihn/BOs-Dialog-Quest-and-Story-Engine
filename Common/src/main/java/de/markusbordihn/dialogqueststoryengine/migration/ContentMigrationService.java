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

package de.markusbordihn.dialogqueststoryengine.migration;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import java.util.HashSet;
import java.util.Set;
import net.minecraft.resources.ResourceLocation;

public final class ContentMigrationService {

  private static final String FIELD_SCHEMA = "schema";

  private ContentMigrationService() {}

  public static JsonObject migrate(
      ContentType contentType, JsonObject source, ResourceLocation contentId, String filePath) {
    JsonElement schemaElement = source.get(FIELD_SCHEMA);
    if (schemaElement == null
        || !schemaElement.isJsonPrimitive()
        || !schemaElement.getAsJsonPrimitive().isNumber()) {
      return source;
    }

    int schema = schemaElement.getAsInt();
    if (schema >= contentType.currentSchema()) {
      return source;
    }

    JsonObject migrated = source.deepCopy();
    Set<Integer> visitedSchemas = new HashSet<>();
    while (schema < contentType.currentSchema() && visitedSchemas.add(schema)) {
      int currentSchema = schema;
      DataMigration migration =
          ContentMigrationRegistry.INSTANCE.getMigrations(contentType).stream()
              .filter(candidate -> candidate.sourceSchema() == currentSchema)
              .findFirst()
              .orElse(null);
      if (migration == null) {
        break;
      }
      migrated = migration.migrate(migrated);
      schema = migration.targetSchema();
      migrated.addProperty(FIELD_SCHEMA, schema);
    }

    if (schema > contentType.currentSchema()) {
      throw new IllegalStateException(
          "Migration for " + contentId + " in " + filePath + " exceeded the current schema");
    }
    return migrated;
  }
}
