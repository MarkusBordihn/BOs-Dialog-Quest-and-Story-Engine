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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class StoryEntryLoader extends SimpleJsonResourceReloadListener {

  static final String DIRECTORY = "dqse/story_entries";
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Gson GSON = new Gson();

  public StoryEntryLoader() {
    super(GSON, DIRECTORY);
  }

  private static String buildFilePath(ResourceLocation id) {
    return "assets/" + id.getNamespace() + "/" + DIRECTORY + "/" + id.getPath() + ".json";
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> jsonEntries,
      ResourceManager resourceManager,
      ProfilerFiller profiler) {
    StoryEntryClientRegistry.clear();
    ContentIssueTracker.clearFor(ContentType.STORY_ENTRY);

    for (Map.Entry<ResourceLocation, JsonElement> fileEntry : jsonEntries.entrySet()) {
      ResourceLocation id = fileEntry.getKey();
      String filePath = buildFilePath(id);

      if (!fileEntry.getValue().isJsonObject()) {
        ContentIssueTracker.record(
            ContentIssue.of(
                IssueCode.JSON_PARSE_FAILED, ContentType.STORY_ENTRY, id, filePath, null));
        log.error(
            "{} Story entry {} — root element is not a JSON object, skipping.",
            Constants.LOG_PREFIX,
            id);
        continue;
      }

      ParseResult<StoryEntry> result =
          StoryEntryParser.parse(id, filePath, fileEntry.getValue().getAsJsonObject());

      result.issues().forEach(ContentIssueTracker::record);

      if (result.isSuccess()) {
        StoryEntryClientRegistry.put(result.value().get());
      } else {
        log.error(
            "{} Skipped story entry {} — see issues above for details.", Constants.LOG_PREFIX, id);
      }
    }

    log.info(
        "{} Loaded {} story {}.",
        Constants.LOG_PREFIX,
        StoryEntryClientRegistry.size(),
        StoryEntryClientRegistry.size() == 1 ? "entry" : "entries");

    StoryEntryThemeLinker.validate();
  }

  @Override
  public String getName() {
    return "dqse_story_entries";
  }
}
