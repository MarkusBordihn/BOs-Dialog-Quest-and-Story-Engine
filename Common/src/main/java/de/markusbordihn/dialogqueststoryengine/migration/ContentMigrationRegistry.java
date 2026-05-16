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

import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public final class ContentMigrationRegistry {

  public static final ContentMigrationRegistry INSTANCE = new ContentMigrationRegistry();

  private final Map<ContentType, List<DataMigration>> migrations =
      new EnumMap<>(ContentType.class);

  private ContentMigrationRegistry() {}

  public void register(ContentType contentType, DataMigration migration) {
    List<DataMigration> migrations =
        this.migrations.computeIfAbsent(contentType, ignored -> new ArrayList<>());
    migrations.add(migration);
    migrations.sort(Comparator.comparingInt(DataMigration::sourceSchema));
  }

  public List<DataMigration> getMigrations(ContentType contentType) {
    List<DataMigration> migrations = this.migrations.get(contentType);
    if (migrations == null) {
      return Collections.emptyList();
    }

    return Collections.unmodifiableList(migrations);
  }
}
