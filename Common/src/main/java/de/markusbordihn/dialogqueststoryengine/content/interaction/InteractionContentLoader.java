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

package de.markusbordihn.dialogqueststoryengine.content.interaction;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.DataPackReloadNotifier;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import de.markusbordihn.dialogqueststoryengine.interaction.InteractionManager;
import java.util.LinkedHashMap;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class InteractionContentLoader extends SimpleJsonResourceReloadListener {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "interactions");
  static final String DIRECTORY = "dqse/interactions";
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Gson GSON = new Gson();

  public InteractionContentLoader() {
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
    ContentIssueTracker.clearFor(ContentType.INTERACTION);
    Map<ResourceLocation, InteractionDefinition> loaded = new LinkedHashMap<>();

    for (Map.Entry<ResourceLocation, JsonElement> fileEntry : jsonEntries.entrySet()) {
      ResourceLocation resourceLocation = fileEntry.getKey();
      String filePath = buildFilePath(resourceLocation);

      if (!fileEntry.getValue().isJsonObject()) {
        ContentIssueTracker.record(
            ContentIssue.of(
                IssueCode.JSON_PARSE_FAILED,
                ContentType.INTERACTION,
                resourceLocation,
                filePath,
                null));
        log.error(
            "{} Interaction {} — root element is not a JSON object, skipping.",
            Constants.LOG_PREFIX,
            resourceLocation);
        continue;
      }

      ParseResult<InteractionDefinition> result =
          InteractionContentParser.parse(
              resourceLocation, filePath, fileEntry.getValue().getAsJsonObject());

      result.issues().forEach(ContentIssueTracker::record);

      if (result.isSuccess()) {
        loaded.put(resourceLocation, result.value().get());
      } else {
        log.error(
            "{} Skipped interaction {} — see issues above for details.",
            Constants.LOG_PREFIX,
            resourceLocation);
      }
    }

    InteractionContentRegistry.replaceAll(loaded);

    InteractionManager.clearDatapackInteractions();
    for (InteractionDefinition definition : loaded.values()) {
      InteractionDefinitions.toEntry(definition)
          .ifPresent(InteractionManager::registerDatapackInteraction);
    }

    DataPackReloadNotifier.fire();

    log.info(
        "{} Loaded {} {}.",
        Constants.LOG_PREFIX,
        loaded.size(),
        loaded.size() == 1 ? "interaction" : "interactions");
  }

  @Override
  public String getName() {
    return "dqse_interactions";
  }
}
