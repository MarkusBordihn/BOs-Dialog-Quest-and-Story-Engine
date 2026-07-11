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

package de.markusbordihn.dialogqueststoryengine.content.dialog;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.dialog.runtime.DialogRuntimeRegistry;
import de.markusbordihn.dialogqueststoryengine.content.dialog.runtime.JsonDialogRuntime;
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

public class DialogContentLoader extends SimpleJsonResourceReloadListener {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "dialogs");
  static final String DIRECTORY = "dqse/dialogs";
  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final Gson GSON = new Gson();

  public DialogContentLoader() {
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
    ContentIssueTracker.clearFor(ContentType.DIALOG);
    DialogClientRegistry.clear();
    DialogRuntimeRegistry.invalidateAll();
    Map<ResourceLocation, DialogDefinition> loaded = new LinkedHashMap<>();

    for (Map.Entry<ResourceLocation, JsonElement> fileEntry : jsonEntries.entrySet()) {
      ResourceLocation resourceLocation = fileEntry.getKey();
      String filePath = buildFilePath(resourceLocation);

      if (!fileEntry.getValue().isJsonObject()) {
        ContentIssueTracker.record(
            ContentIssue.of(
                IssueCode.JSON_PARSE_FAILED, ContentType.DIALOG, resourceLocation, filePath, null));
        log.error(
            "{} Dialog {} — root element is not a JSON object, skipping.",
            Constants.LOG_PREFIX,
            resourceLocation);
        continue;
      }

      ParseResult<DialogDefinition> result =
          ContentParserGuard.parse(
              ContentType.DIALOG,
              resourceLocation,
              filePath,
              fileEntry.getValue().getAsJsonObject(),
              json -> DialogContentParser.parse(resourceLocation, filePath, json));

      result.issues().forEach(ContentIssueTracker::record);

      if (result.isSuccess()) {
        DialogDefinition definition = result.value().get();
        loaded.put(resourceLocation, definition);
        DialogClientRegistry.put(definition);
        DialogRuntimeRegistry.put(new JsonDialogRuntime(definition));
      } else {
        log.error(
            "{} Skipped dialog {} — see issues above for details.",
            Constants.LOG_PREFIX,
            resourceLocation);
      }
    }

    DialogContentRegistry.replaceAll(loaded);

    log.info(
        "{} Loaded {} {}.",
        Constants.LOG_PREFIX,
        loaded.size(),
        loaded.size() == 1 ? "dialog" : "dialogs");
  }

  @Override
  public String getName() {
    return "dqse_dialogs";
  }
}
