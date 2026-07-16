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

package de.markusbordihn.dialogqueststoryengine.theme;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayout;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeTextAlignment;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;

public final class ThemeParser {

  public static final String FIELD_LAYOUT = "layout";
  public static final String FIELD_FRAME_TEXTURE = "frame_texture";
  public static final String FIELD_BACKGROUND_TEXTURE = "background_texture";
  public static final String FIELD_SHOW_PAGE_NUMBERS = "show_page_numbers";
  public static final String FIELD_SHOW_CLOSE_BUTTON = "show_close_button";
  public static final String FIELD_DISPLAY_AREA = "display_area";
  public static final String FIELD_TITLE_AREA = "title_area";
  public static final String FIELD_TITLE_ALIGNMENT = "title_alignment";
  public static final String FIELD_TEXT_AREA = "text_area";
  public static final String FIELD_CHOICE_AREA = "choice_area";
  public static final String FIELD_SCREEN_WIDTH = "screen_width";
  public static final String FIELD_SCREEN_HEIGHT = "screen_height";

  private ThemeParser() {}

  public static ParseResult<Theme> parse(
      ResourceLocation id, String filePath, JsonObject jsonObject) {
    List<ContentIssue> issues = new ArrayList<>();

    Optional<Integer> schema =
        JsonFieldReader.readSchema(
            jsonObject, ContentType.THEME, ContentType.THEME.currentSchema(), id, filePath, issues);
    if (schema.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<ResourceLocation> layoutRL =
        JsonFieldReader.readResourceLocation(
            jsonObject, FIELD_LAYOUT, ContentType.THEME, id, filePath, issues);
    if (layoutRL.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<ThemeLayout> layout = ThemeLayout.fromResourceLocation(layoutRL.get());
    if (layout.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_THEME_LAYOUT,
              ContentType.THEME,
              id,
              filePath,
              FIELD_LAYOUT,
              Map.of("value", layoutRL.get().toString())));
      return ParseResult.failure(issues);
    }

    Optional<ResourceLocation> frameTexture =
        JsonFieldReader.readResourceLocation(
            jsonObject, FIELD_FRAME_TEXTURE, ContentType.THEME, id, filePath, issues);
    if (frameTexture.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<ResourceLocation> backgroundTexture =
        JsonFieldReader.readResourceLocation(
            jsonObject, FIELD_BACKGROUND_TEXTURE, ContentType.THEME, id, filePath, issues);
    if (backgroundTexture.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<Boolean> showPageNumbers =
        JsonFieldReader.readBoolean(
            jsonObject, FIELD_SHOW_PAGE_NUMBERS, ContentType.THEME, id, filePath, issues);
    if (showPageNumbers.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<Boolean> showCloseButton =
        JsonFieldReader.readBoolean(
            jsonObject, FIELD_SHOW_CLOSE_BUTTON, ContentType.THEME, id, filePath, issues);
    if (showCloseButton.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<ThemeArea> displayArea =
        readArea(jsonObject, FIELD_DISPLAY_AREA, id, filePath, issues);
    Optional<ThemeArea> titleArea = readArea(jsonObject, FIELD_TITLE_AREA, id, filePath, issues);
    Optional<ThemeTextAlignment> titleAlignment =
        readTitleAlignment(jsonObject, id, filePath, issues);
    Optional<ThemeArea> textArea = readArea(jsonObject, FIELD_TEXT_AREA, id, filePath, issues);
    Optional<ThemeArea> choiceArea = readArea(jsonObject, FIELD_CHOICE_AREA, id, filePath, issues);
    if (displayArea.isEmpty()
        || titleArea.isEmpty()
        || titleAlignment.isEmpty()
        || textArea.isEmpty()
        || choiceArea.isEmpty()) {
      return ParseResult.failure(issues);
    }

    int screenWidth =
        jsonObject.has(FIELD_SCREEN_WIDTH) ? jsonObject.get(FIELD_SCREEN_WIDTH).getAsInt() : 0;
    int screenHeight =
        jsonObject.has(FIELD_SCREEN_HEIGHT) ? jsonObject.get(FIELD_SCREEN_HEIGHT).getAsInt() : 0;

    return ParseResult.success(
        new Theme(
            UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)),
            id,
            schema.get(),
            layout.get(),
            frameTexture.get(),
            backgroundTexture.get(),
            showPageNumbers.get(),
            showCloseButton.get(),
            displayArea.get(),
            titleArea.get(),
            titleAlignment.get(),
            textArea.get(),
            choiceArea.get(),
            screenWidth,
            screenHeight),
        issues);
  }

  private static Optional<ThemeArea> readArea(
      JsonObject jsonObject,
      String field,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<JsonObject> areaJson =
        JsonFieldReader.readObject(jsonObject, field, ContentType.THEME, id, filePath, issues);
    if (areaJson.isEmpty()) {
      return Optional.empty();
    }

    return parseArea(areaJson.get(), field, id, filePath, issues);
  }

  private static Optional<ThemeTextAlignment> readTitleAlignment(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Optional<String> rawAlignment =
        JsonFieldReader.readString(
            jsonObject, FIELD_TITLE_ALIGNMENT, ContentType.THEME, id, filePath, issues);
    if (rawAlignment.isEmpty()) {
      return Optional.empty();
    }

    Optional<ThemeTextAlignment> alignment = ThemeTextAlignment.fromKey(rawAlignment.get());
    if (alignment.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              ContentType.THEME,
              id,
              filePath,
              FIELD_TITLE_ALIGNMENT,
              Map.of("value", rawAlignment.get(), "expected", "left, center, or right")));
    }

    return alignment;
  }

  private static Optional<ThemeArea> parseArea(
      JsonObject areaJson,
      String field,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<Integer> x =
        JsonFieldReader.readInt(areaJson, "x", ContentType.THEME, id, filePath, issues);
    Optional<Integer> y =
        JsonFieldReader.readInt(areaJson, "y", ContentType.THEME, id, filePath, issues);
    Optional<Integer> width =
        JsonFieldReader.readInt(areaJson, "width", ContentType.THEME, id, filePath, issues);
    Optional<Integer> height =
        JsonFieldReader.readInt(areaJson, "height", ContentType.THEME, id, filePath, issues);

    if (x.isEmpty() || y.isEmpty() || width.isEmpty() || height.isEmpty()) {
      return Optional.empty();
    }

    try {
      return Optional.of(new ThemeArea(x.get(), y.get(), width.get(), height.get()));
    } catch (IllegalArgumentException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              ContentType.THEME,
              id,
              filePath,
              field,
              Map.of("reason", e.getMessage())));
      return Optional.empty();
    }
  }
}
