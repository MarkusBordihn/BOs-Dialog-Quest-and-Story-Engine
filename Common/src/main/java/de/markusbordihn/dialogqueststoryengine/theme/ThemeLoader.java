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
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.AbstractJsonContentLoader;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.data.theme.Theme;
import de.markusbordihn.dialogqueststoryengine.story.entry.StoryEntryThemeLinker;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class ThemeLoader extends AbstractJsonContentLoader<Theme> {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "themes");

  public ThemeLoader() {
    super("assets", "dqse/themes");
  }

  @Override
  protected ContentType contentType() {
    return ContentType.THEME;
  }

  @Override
  protected ParseResult<Theme> parse(ResourceLocation id, String filePath, JsonObject json) {
    return ThemeParser.parse(id, filePath, json);
  }

  @Override
  protected void beforeLoad() {
    ThemeClientRegistry.clear();
  }

  @Override
  protected void commit(Map<ResourceLocation, Theme> loaded) {
    loaded.values().forEach(ThemeClientRegistry::put);
    StoryEntryThemeLinker.validate();
  }

  @Override
  protected String contentName() {
    return "theme";
  }

  @Override
  public String getName() {
    return "dqse_themes";
  }
}
