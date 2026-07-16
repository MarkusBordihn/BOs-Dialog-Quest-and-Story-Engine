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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
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

public abstract class AbstractJsonContentLoader<T> extends SimpleJsonResourceReloadListener {

  protected static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Gson GSON = new Gson();

  private final String resourceRoot;
  private final String directory;

  protected AbstractJsonContentLoader(String resourceRoot, String directory) {
    super(GSON, directory);
    this.resourceRoot = resourceRoot;
    this.directory = directory;
  }

  protected abstract ContentType contentType();

  protected abstract ParseResult<T> parse(ResourceLocation id, String filePath, JsonObject json);

  protected abstract void commit(Map<ResourceLocation, T> loaded);

  protected abstract String contentName();

  protected String contentNamePlural() {
    return contentName() + "s";
  }

  protected void beforeLoad() {}

  @Override
  protected final void apply(
      Map<ResourceLocation, JsonElement> jsonEntries,
      ResourceManager resourceManager,
      ProfilerFiller profiler) {
    ContentIssueTracker.clearFor(contentType());
    beforeLoad();

    Map<ResourceLocation, T> loaded = new LinkedHashMap<>();
    for (Map.Entry<ResourceLocation, JsonElement> fileEntry : jsonEntries.entrySet()) {
      ResourceLocation resourceLocation = fileEntry.getKey();
      String filePath = buildFilePath(resourceLocation);

      if (!fileEntry.getValue().isJsonObject()) {
        ContentIssueTracker.record(
            ContentIssue.of(
                IssueCode.JSON_PARSE_FAILED, contentType(), resourceLocation, filePath, null));
        log.error(
            "{} {} {} — root element is not a JSON object, skipping.",
            Constants.LOG_PREFIX,
            capitalizedName(),
            resourceLocation);
        continue;
      }

      ParseResult<T> result =
          ContentParserGuard.parse(
              contentType(),
              resourceLocation,
              filePath,
              fileEntry.getValue().getAsJsonObject(),
              json -> parse(resourceLocation, filePath, json));

      result.issues().forEach(ContentIssueTracker::record);

      if (result.isSuccess()) {
        loaded.put(resourceLocation, result.value().get());
      } else {
        log.error(
            "{} Skipped {} {} — see issues above for details.",
            Constants.LOG_PREFIX,
            contentName(),
            resourceLocation);
      }
    }

    commit(loaded);

    log.info(
        "{} Loaded {} {}.",
        Constants.LOG_PREFIX,
        loaded.size(),
        loaded.size() == 1 ? contentName() : contentNamePlural());
  }

  private String buildFilePath(ResourceLocation resourceLocation) {
    return resourceRoot
        + "/"
        + resourceLocation.getNamespace()
        + "/"
        + this.directory
        + "/"
        + resourceLocation.getPath()
        + ".json";
  }

  private String capitalizedName() {
    String name = contentName();
    return name.isEmpty() ? name : Character.toUpperCase(name.charAt(0)) + name.substring(1);
  }
}
