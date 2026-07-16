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

package de.markusbordihn.dialogqueststoryengine.content;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.json.OptionalFieldReader;
import java.util.List;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;

public final class NarrativeMetadataParser {

  public static final String FIELD_NARRATIVE = "narrative";

  private static final String FIELD_ARC = "arc";
  private static final String FIELD_ARC_KEY = "arc_key";
  private static final String FIELD_ARC_ORDER = "arc_order";
  private static final String FIELD_CHAPTER = "chapter";
  private static final String FIELD_CHAPTER_KEY = "chapter_key";
  private static final String FIELD_CHAPTER_ORDER = "chapter_order";

  private NarrativeMetadataParser() {}

  public static NarrativeMetadata parse(
      JsonObject parent,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!parent.has(FIELD_NARRATIVE) || !parent.get(FIELD_NARRATIVE).isJsonObject()) {
      return NarrativeMetadata.EMPTY;
    }

    JsonObject narrative = parent.getAsJsonObject(FIELD_NARRATIVE);
    String path = FIELD_NARRATIVE + ".";
    Optional<ResourceLocation> arc =
        OptionalFieldReader.resourceLocation(
            narrative, FIELD_ARC, path + FIELD_ARC, contentType, id, filePath, issues);
    Optional<String> arcKey =
        OptionalFieldReader.string(
            narrative, FIELD_ARC_KEY, path + FIELD_ARC_KEY, contentType, id, filePath, issues);
    int arcOrder =
        OptionalFieldReader.integer(
            narrative, FIELD_ARC_ORDER, path + FIELD_ARC_ORDER, contentType, id, filePath, issues);
    Optional<ResourceLocation> chapter =
        OptionalFieldReader.resourceLocation(
            narrative, FIELD_CHAPTER, path + FIELD_CHAPTER, contentType, id, filePath, issues);
    Optional<String> chapterKey =
        OptionalFieldReader.string(
            narrative,
            FIELD_CHAPTER_KEY,
            path + FIELD_CHAPTER_KEY,
            contentType,
            id,
            filePath,
            issues);
    int chapterOrder =
        OptionalFieldReader.integer(
            narrative,
            FIELD_CHAPTER_ORDER,
            path + FIELD_CHAPTER_ORDER,
            contentType,
            id,
            filePath,
            issues);

    return new NarrativeMetadata(arc, arcKey, arcOrder, chapter, chapterKey, chapterOrder);
  }
}
