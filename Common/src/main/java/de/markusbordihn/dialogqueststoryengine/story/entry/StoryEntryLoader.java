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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.AbstractJsonContentLoader;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class StoryEntryLoader extends AbstractJsonContentLoader<StoryEntry> {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "story_entries");

  public StoryEntryLoader() {
    super("assets", "dqse/story_entries");
  }

  @Override
  protected ContentType contentType() {
    return ContentType.STORY_ENTRY;
  }

  @Override
  protected ParseResult<StoryEntry> parse(ResourceLocation id, String filePath, JsonObject json) {
    return StoryEntryParser.parse(id, filePath, json);
  }

  @Override
  protected void beforeLoad() {
    StoryEntryClientRegistry.clear();
  }

  @Override
  protected void commit(Map<ResourceLocation, StoryEntry> loaded) {
    loaded.values().forEach(StoryEntryClientRegistry::put);
    StoryEntryThemeLinker.validate();
  }

  @Override
  protected String contentName() {
    return "story entry";
  }

  @Override
  protected String contentNamePlural() {
    return "story entries";
  }

  @Override
  public String getName() {
    return "dqse_story_entries";
  }
}
