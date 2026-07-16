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

package de.markusbordihn.dialogqueststoryengine.content.story;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.AbstractJsonContentLoader;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class InteractiveStoryContentLoader
    extends AbstractJsonContentLoader<InteractiveStoryDefinition> {

  public static final ResourceLocation ID =
      new ResourceLocation(Constants.MOD_ID, "interactive_stories");

  public InteractiveStoryContentLoader() {
    super("data", "dqse/interactive_story");
  }

  @Override
  protected ContentType contentType() {
    return ContentType.INTERACTIVE_STORY;
  }

  @Override
  protected ParseResult<InteractiveStoryDefinition> parse(
      ResourceLocation id, String filePath, JsonObject json) {
    return InteractiveStoryContentParser.parse(id, filePath, json);
  }

  @Override
  protected void commit(Map<ResourceLocation, InteractiveStoryDefinition> loaded) {
    InteractiveStoryContentRegistry.replaceAll(loaded);
  }

  @Override
  protected String contentName() {
    return "interactive story";
  }

  @Override
  protected String contentNamePlural() {
    return "interactive stories";
  }

  @Override
  public String getName() {
    return "dqse_interactive_stories";
  }
}
