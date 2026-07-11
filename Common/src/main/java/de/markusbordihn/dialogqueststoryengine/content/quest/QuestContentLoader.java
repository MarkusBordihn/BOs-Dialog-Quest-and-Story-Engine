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

package de.markusbordihn.dialogqueststoryengine.content.quest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.ContentParserGuard;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class QuestContentLoader extends SimpleJsonResourceReloadListener {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "quests");
  static final String DIRECTORY = "dqse/quests";
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Gson GSON = new Gson();

  public QuestContentLoader() {
    super(GSON, DIRECTORY);
  }

  private static String buildFilePath(ResourceLocation resourceLocation) {
    return "data/"
        + resourceLocation.getNamespace()
        + "/"
        + DIRECTORY
        + "/"
        + resourceLocation.getPath()
        + ".json";
  }

  @Override
  protected void apply(
      Map<ResourceLocation, JsonElement> jsonEntries,
      ResourceManager resourceManager,
      ProfilerFiller profiler) {
    ContentIssueTracker.clearFor(ContentType.QUEST);
    QuestClientRegistry.clear();
    Map<ResourceLocation, QuestDefinition> loaded = new LinkedHashMap<>();

    for (Map.Entry<ResourceLocation, JsonElement> fileEntry : jsonEntries.entrySet()) {
      ResourceLocation resourceLocation = fileEntry.getKey();
      String filePath = buildFilePath(resourceLocation);

      if (!fileEntry.getValue().isJsonObject()) {
        ContentIssueTracker.record(
            ContentIssue.of(
                IssueCode.JSON_PARSE_FAILED, ContentType.QUEST, resourceLocation, filePath, null));
        log.error(
            "{} Quest {} — root element is not a JSON object, skipping.",
            Constants.LOG_PREFIX,
            resourceLocation);
        continue;
      }

      ParseResult<QuestDefinition> result =
          ContentParserGuard.parse(
              ContentType.QUEST,
              resourceLocation,
              filePath,
              fileEntry.getValue().getAsJsonObject(),
              json -> QuestContentParser.parse(resourceLocation, filePath, json));

      result.issues().forEach(ContentIssueTracker::record);

      if (result.isSuccess()) {
        QuestDefinition definition = result.value().get();
        loaded.put(resourceLocation, definition);
        QuestClientRegistry.put(definition);
      } else {
        log.error(
            "{} Skipped quest {} — see issues above for details.",
            Constants.LOG_PREFIX,
            resourceLocation);
      }
    }

    QuestContentRegistry.replaceAll(loaded);

    log.info(
        "{} Loaded {} {}.",
        Constants.LOG_PREFIX,
        loaded.size(),
        loaded.size() == 1 ? "quest" : "quests");
  }

  @Override
  public String getName() {
    return "dqse_quests";
  }
}
