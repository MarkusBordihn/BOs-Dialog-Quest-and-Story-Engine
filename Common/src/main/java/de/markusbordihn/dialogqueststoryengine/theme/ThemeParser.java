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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueSeverity;
import de.markusbordihn.dialogqueststoryengine.data.json.JsonFieldReader;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeAnchor;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeArea;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeLayoutContract;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeScaleLimits;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSprite;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSpriteBorder;
import de.markusbordihn.dialogqueststoryengine.data.theme.ThemeSpriteScaling;
import de.markusbordihn.dialogqueststoryengine.data.theme.option.ThemeOption;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.registry.ThemeProvider;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import net.minecraft.ResourceLocationException;
import net.minecraft.resources.ResourceLocation;

public final class ThemeParser {

  public static final String FIELD_LAYOUT = "layout";
  public static final String FIELD_LOGICAL_WIDTH = "logical_width";
  public static final String FIELD_LOGICAL_HEIGHT = "logical_height";
  public static final String FIELD_MIN_SCALE = "min_scale";
  public static final String FIELD_MAX_SCALE = "max_scale";
  public static final String FIELD_ANCHOR = "anchor";
  public static final String FIELD_AREAS = "areas";
  public static final String FIELD_SPRITES = "sprites";
  public static final String FIELD_COLORS = "colors";
  public static final String FIELD_OPTIONS = "options";

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

    Optional<ResourceLocation> layoutId =
        JsonFieldReader.readResourceLocation(
            jsonObject, FIELD_LAYOUT, ContentType.THEME, id, filePath, issues);
    if (layoutId.isEmpty()) {
      return ParseResult.failure(issues);
    }

    Optional<ThemeProvider> provider = Registries.THEMES.get(layoutId.get());
    if (provider.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.UNKNOWN_THEME_LAYOUT,
              ContentType.THEME,
              id,
              filePath,
              FIELD_LAYOUT,
              Map.of("value", layoutId.get().toString())));
      return ParseResult.failure(issues);
    }

    ThemeLayoutContract contract = provider.get().contract();

    Optional<Integer> logicalWidth =
        JsonFieldReader.readInt(
            jsonObject, FIELD_LOGICAL_WIDTH, ContentType.THEME, id, filePath, issues);
    Optional<Integer> logicalHeight =
        JsonFieldReader.readInt(
            jsonObject, FIELD_LOGICAL_HEIGHT, ContentType.THEME, id, filePath, issues);
    if (logicalWidth.isEmpty() || logicalHeight.isEmpty()) {
      return ParseResult.failure(issues);
    }

    ThemeScaleLimits scaleLimits = parseScaleLimits(jsonObject, contract, id, filePath, issues);
    ThemeAnchor anchor = parseAnchor(jsonObject, id, filePath, issues);
    Map<String, ThemeArea> areas = parseAreas(jsonObject, id, filePath, issues);
    Map<String, ThemeSprite> sprites = parseSprites(jsonObject, id, filePath, issues);
    Map<String, Integer> colors = parseColors(jsonObject, id, filePath, issues);
    Map<String, Object> options = parseOptions(jsonObject, contract, id, filePath, issues);

    Theme theme =
        new Theme(
            UUID.nameUUIDFromBytes(id.toString().getBytes(StandardCharsets.UTF_8)),
            id,
            schema.get(),
            layoutId.get(),
            logicalWidth.get(),
            logicalHeight.get(),
            scaleLimits,
            anchor,
            areas,
            sprites,
            colors,
            options);

    issues.addAll(provider.get().validate(theme, filePath));

    boolean hasError = issues.stream().anyMatch(issue -> issue.severity() == IssueSeverity.ERROR);
    if (hasError) {
      return ParseResult.failure(issues);
    }

    return ParseResult.success(theme, issues);
  }

  private static ThemeScaleLimits parseScaleLimits(
      JsonObject jsonObject,
      ThemeLayoutContract contract,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    float min = contract.defaultScaleLimits().min();
    float max = contract.defaultScaleLimits().max();
    if (jsonObject.has(FIELD_MIN_SCALE)) {
      Optional<Float> value =
          JsonFieldReader.readFloat(
              jsonObject, FIELD_MIN_SCALE, ContentType.THEME, id, filePath, issues);
      if (value.isPresent()) {
        min = value.get();
      }
    }
    if (jsonObject.has(FIELD_MAX_SCALE)) {
      Optional<Float> value =
          JsonFieldReader.readFloat(
              jsonObject, FIELD_MAX_SCALE, ContentType.THEME, id, filePath, issues);
      if (value.isPresent()) {
        max = value.get();
      }
    }
    try {
      return new ThemeScaleLimits(min, max);
    } catch (IllegalArgumentException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_THEME_SCALE_LIMITS,
              ContentType.THEME,
              id,
              filePath,
              FIELD_MIN_SCALE,
              Map.of("reason", e.getMessage())));
      return ThemeScaleLimits.DEFAULT;
    }
  }

  private static ThemeAnchor parseAnchor(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    if (!jsonObject.has(FIELD_ANCHOR)) {
      return ThemeAnchor.CENTER;
    }

    Optional<String> raw =
        JsonFieldReader.readString(
            jsonObject, FIELD_ANCHOR, ContentType.THEME, id, filePath, issues);
    if (raw.isEmpty()) {
      return ThemeAnchor.CENTER;
    }

    Optional<ThemeAnchor> anchor = ThemeAnchor.fromKey(raw.get());
    if (anchor.isEmpty()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              ContentType.THEME,
              id,
              filePath,
              FIELD_ANCHOR,
              Map.of("value", raw.get(), "expected", "center, top, bottom, left, right, ...")));
      return ThemeAnchor.CENTER;
    }
    return anchor.get();
  }

  private static Map<String, ThemeArea> parseAreas(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, ThemeArea> areas = new LinkedHashMap<>();
    if (!jsonObject.has(FIELD_AREAS)) {
      return areas;
    }

    Optional<JsonObject> areasJson =
        JsonFieldReader.readObject(
            jsonObject, FIELD_AREAS, ContentType.THEME, id, filePath, issues);
    if (areasJson.isEmpty()) {
      return areas;
    }

    for (Map.Entry<String, JsonElement> entry : areasJson.get().entrySet()) {
      String name = entry.getKey();
      String path = FIELD_AREAS + "." + name;
      if (!entry.getValue().isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.THEME,
                id,
                filePath,
                path,
                Map.of("expected", "object")));
        continue;
      }
      parseArea(entry.getValue().getAsJsonObject(), path, id, filePath, issues)
          .ifPresent(area -> areas.put(name, area));
    }
    return areas;
  }

  private static Optional<ThemeArea> parseArea(
      JsonObject areaJson,
      String path,
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
              path,
              Map.of("reason", e.getMessage())));
      return Optional.empty();
    }
  }

  private static Map<String, ThemeSprite> parseSprites(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, ThemeSprite> sprites = new LinkedHashMap<>();
    if (!jsonObject.has(FIELD_SPRITES)) {
      return sprites;
    }

    Optional<JsonObject> spritesJson =
        JsonFieldReader.readObject(
            jsonObject, FIELD_SPRITES, ContentType.THEME, id, filePath, issues);
    if (spritesJson.isEmpty()) {
      return sprites;
    }

    for (Map.Entry<String, JsonElement> entry : spritesJson.get().entrySet()) {
      String name = entry.getKey();
      String path = FIELD_SPRITES + "." + name;
      if (!entry.getValue().isJsonObject()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_FIELD_TYPE,
                ContentType.THEME,
                id,
                filePath,
                path,
                Map.of("expected", "object")));
        continue;
      }
      parseSprite(entry.getValue().getAsJsonObject(), path, id, filePath, issues)
          .ifPresent(sprite -> sprites.put(name, sprite));
    }
    return sprites;
  }

  private static Optional<ThemeSprite> parseSprite(
      JsonObject spriteJson,
      String path,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Optional<ResourceLocation> texture =
        JsonFieldReader.readResourceLocation(
            spriteJson, "texture", ContentType.THEME, id, filePath, issues);
    Optional<Integer> textureWidth =
        JsonFieldReader.readInt(
            spriteJson, "texture_width", ContentType.THEME, id, filePath, issues);
    Optional<Integer> textureHeight =
        JsonFieldReader.readInt(
            spriteJson, "texture_height", ContentType.THEME, id, filePath, issues);
    Optional<Integer> sourceWidth =
        JsonFieldReader.readInt(
            spriteJson, "source_width", ContentType.THEME, id, filePath, issues);
    Optional<Integer> sourceHeight =
        JsonFieldReader.readInt(
            spriteJson, "source_height", ContentType.THEME, id, filePath, issues);
    if (texture.isEmpty()
        || textureWidth.isEmpty()
        || textureHeight.isEmpty()
        || sourceWidth.isEmpty()
        || sourceHeight.isEmpty()) {
      return Optional.empty();
    }

    int u = spriteJson.has("u") ? spriteJson.get("u").getAsInt() : 0;
    int v = spriteJson.has("v") ? spriteJson.get("v").getAsInt() : 0;

    ThemeSpriteScaling scaling = ThemeSpriteScaling.STRETCH;
    if (spriteJson.has("scaling")) {
      Optional<ThemeSpriteScaling> parsed =
          ThemeSpriteScaling.fromKey(spriteJson.get("scaling").getAsString());
      if (parsed.isEmpty()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_THEME_SPRITE,
                ContentType.THEME,
                id,
                filePath,
                path + ".scaling",
                Map.of("value", spriteJson.get("scaling").getAsString())));
        return Optional.empty();
      }
      scaling = parsed.get();
    }

    ThemeSpriteBorder border = null;
    if (spriteJson.has("border")) {
      Optional<ThemeSpriteBorder> parsedBorder =
          parseBorder(spriteJson.get("border"), path + ".border", id, filePath, issues);
      if (parsedBorder.isEmpty()) {
        return Optional.empty();
      }
      border = parsedBorder.get();
    }

    try {
      return Optional.of(
          new ThemeSprite(
              texture.get(),
              textureWidth.get(),
              textureHeight.get(),
              u,
              v,
              sourceWidth.get(),
              sourceHeight.get(),
              scaling,
              border));
    } catch (IllegalArgumentException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_THEME_SPRITE,
              ContentType.THEME,
              id,
              filePath,
              path,
              Map.of("reason", e.getMessage())));
      return Optional.empty();
    }
  }

  private static Optional<ThemeSpriteBorder> parseBorder(
      JsonElement element,
      String path,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    try {
      if (element.isJsonPrimitive() && element.getAsJsonPrimitive().isNumber()) {
        return Optional.of(ThemeSpriteBorder.all(element.getAsInt()));
      }
      if (element.isJsonObject()) {
        JsonObject borderObject = element.getAsJsonObject();
        int left = borderObject.has("left") ? borderObject.get("left").getAsInt() : 0;
        int top = borderObject.has("top") ? borderObject.get("top").getAsInt() : 0;
        int right = borderObject.has("right") ? borderObject.get("right").getAsInt() : 0;
        int bottom = borderObject.has("bottom") ? borderObject.get("bottom").getAsInt() : 0;
        return Optional.of(new ThemeSpriteBorder(left, top, right, bottom));
      }
    } catch (IllegalArgumentException e) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_THEME_SPRITE,
              ContentType.THEME,
              id,
              filePath,
              path,
              Map.of("reason", e.getMessage())));
      return Optional.empty();
    }
    issues.add(
        ContentIssue.of(
            IssueCode.INVALID_THEME_SPRITE,
            ContentType.THEME,
            id,
            filePath,
            path,
            Map.of("expected", "integer or {left,top,right,bottom}")));
    return Optional.empty();
  }

  private static Map<String, Integer> parseColors(
      JsonObject jsonObject, ResourceLocation id, String filePath, List<ContentIssue> issues) {
    Map<String, Integer> colors = new LinkedHashMap<>();
    if (!jsonObject.has(FIELD_COLORS)) {
      return colors;
    }

    Optional<JsonObject> colorsJson =
        JsonFieldReader.readObject(
            jsonObject, FIELD_COLORS, ContentType.THEME, id, filePath, issues);
    if (colorsJson.isEmpty()) {
      return colors;
    }

    for (Map.Entry<String, JsonElement> entry : colorsJson.get().entrySet()) {
      String name = entry.getKey();
      String path = FIELD_COLORS + "." + name;
      if (!entry.getValue().isJsonPrimitive()
          || !entry.getValue().getAsJsonPrimitive().isString()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_THEME_COLOR,
                ContentType.THEME,
                id,
                filePath,
                path,
                Map.of("expected", "hex color string")));
        continue;
      }
      Optional<Integer> value = parseColorValue(entry.getValue().getAsString());
      if (value.isEmpty()) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_THEME_COLOR,
                ContentType.THEME,
                id,
                filePath,
                path,
                Map.of("value", entry.getValue().getAsString())));
        continue;
      }
      colors.put(name, value.get());
    }
    return colors;
  }

  static Optional<Integer> parseColorValue(String raw) {
    String hex = raw.startsWith("#") ? raw.substring(1) : raw;
    try {
      if (hex.length() == 6) {
        return Optional.of(0xFF000000 | (int) Long.parseLong(hex, 16));
      }
      if (hex.length() == 8) {
        return Optional.of((int) Long.parseLong(hex, 16));
      }
    } catch (NumberFormatException e) {
      return Optional.empty();
    }
    return Optional.empty();
  }

  private static Map<String, Object> parseOptions(
      JsonObject jsonObject,
      ThemeLayoutContract contract,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    Map<String, Object> options = new LinkedHashMap<>();
    if (!jsonObject.has(FIELD_OPTIONS)) {
      return options;
    }

    Optional<JsonObject> optionsJson =
        JsonFieldReader.readObject(
            jsonObject, FIELD_OPTIONS, ContentType.THEME, id, filePath, issues);
    if (optionsJson.isEmpty()) {
      return options;
    }

    for (Map.Entry<String, JsonElement> entry : optionsJson.get().entrySet()) {
      String key = entry.getKey();
      String path = FIELD_OPTIONS + "." + key;
      ThemeOption<?> option = contract.options().get(key);
      if (option == null) {
        issues.add(
            ContentIssue.of(
                IssueCode.INVALID_THEME_OPTION,
                ContentType.THEME,
                id,
                filePath,
                path,
                Map.of("key", key, "reason", "unknown option for this layout")));
        continue;
      }
      parseOptionValue(option, entry.getValue(), path, id, filePath, issues)
          .ifPresent(value -> options.put(key, value));
    }
    return options;
  }

  private static Optional<Object> parseOptionValue(
      ThemeOption<?> option,
      JsonElement element,
      String path,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    switch (option.kind()) {
      case BOOLEAN -> {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isBoolean()) {
          issues.add(invalidOption(id, filePath, path, "boolean"));
          return Optional.empty();
        }
        return Optional.of(element.getAsBoolean());
      }
      case ENUM -> {
        if (!element.isJsonPrimitive() || !element.getAsJsonPrimitive().isString()) {
          issues.add(invalidOption(id, filePath, path, "string"));
          return Optional.empty();
        }
        Optional<?> parsed = option.parse(element.getAsString());
        if (parsed.isEmpty()) {
          issues.add(
              ContentIssue.of(
                  IssueCode.INVALID_THEME_OPTION,
                  ContentType.THEME,
                  id,
                  filePath,
                  path,
                  Map.of("value", element.getAsString())));
          return Optional.empty();
        }
        return Optional.of(parsed.get());
      }
      default -> {
        return Optional.empty();
      }
    }
  }

  private static ContentIssue invalidOption(
      ResourceLocation id, String filePath, String path, String expected) {
    return ContentIssue.of(
        IssueCode.INVALID_THEME_OPTION,
        ContentType.THEME,
        id,
        filePath,
        path,
        Map.of("expected", expected));
  }

  public static Optional<ResourceLocation> parseLayoutId(String raw) {
    try {
      return Optional.of(new ResourceLocation(raw));
    } catch (ResourceLocationException e) {
      return Optional.empty();
    }
  }
}
