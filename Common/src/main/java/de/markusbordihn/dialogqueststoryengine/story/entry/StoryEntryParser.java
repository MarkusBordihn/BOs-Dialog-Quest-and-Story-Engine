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

package de.markusbordihn.dialogqueststoryengine.story.entry;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public final class StoryEntryParser {

  public static final String FIELD_TYPE = "type";
  public static final String FIELD_TITLE_KEY = "title_key";
  public static final String FIELD_THEME = "theme";
  public static final String FIELD_PAGES = "pages";
  public static final String FIELD_TEXT_KEY = "text_key";

  private StoryEntryParser() {}

  public static ParseResult<StoryEntry> parse(
      ResourceLocation id, String filePath, JsonObject jsonObject) {
    List<ContentIssue> issues = new ArrayList<>();

    Optional<Integer> schema =
        JsonFieldReader.readSchema(
            jsonObject,
            ContentType.STORY_ENTRY,
            ContentType.STORY_ENTRY.currentSchema(),
            id,
            filePath,
            issues);
    if (schema.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<String> typeStr =
        JsonFieldReader.readString(
            jsonObject, FIELD_TYPE, ContentType.STORY_ENTRY, id, filePath, issues);
    if (typeStr.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<StoryEntryType> type = StoryEntryType.fromKey(typeStr.get());
    if (type.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_STORY_TYPE,
              ContentType.STORY_ENTRY,
              id,
              filePath,
              FIELD_TYPE,
              Map.of("value", typeStr.get())));
      return ParseResult.failure(issues);
    }

    Optional<String> titleKey =
        JsonFieldReader.readString(
            jsonObject, FIELD_TITLE_KEY, ContentType.STORY_ENTRY, id, filePath, issues);
    if (titleKey.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<ResourceLocation> themeId =
        JsonFieldReader.readResourceLocation(
            jsonObject, FIELD_THEME, ContentType.STORY_ENTRY, id, filePath, issues);
    if (themeId.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<JsonArray> pagesArray =
        JsonFieldReader.readArray(
            jsonObject, FIELD_PAGES, ContentType.STORY_ENTRY, id, filePath, issues);
    if (pagesArray.isEmpty()) {
      return ParseResult.failure(issues);
    }

    List<StoryPage> pages = parsePages(pagesArray.get(), id, filePath, issues);
    if (pages.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.EMPTY_PAGES, ContentType.STORY_ENTRY, id, filePath, FIELD_PAGES));
      return ParseResult.failure(issues);
    }

    return ParseResult.success(
        new StoryEntry(
            UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)),
            id,
            schema.get(),
            type.get(),
            titleKey.get(),
            themeId.get(),
            pages),
        issues);
  }

  private static List<StoryPage> parsePages(
      JsonArray pagesArray, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    List<StoryPage> pages = new ArrayList<>();

    for (JsonElement pageElement : pagesArray) {
      if (!pageElement.isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.STORY_ENTRY,
                id,
                filePath,
                "pages[]",
                Map.of("expected", "object")));
        continue;
      }

      JsonFieldReader.readString(
              pageElement.getAsJsonObject(),
              FIELD_TEXT_KEY,
              ContentType.STORY_ENTRY,
              id,
              filePath,
              issues)
          .ifPresent(textKey -> pages.add(new StoryPage(textKey)));
    }

    return pages;
  }
}
