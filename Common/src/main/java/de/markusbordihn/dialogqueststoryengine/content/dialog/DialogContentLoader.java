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

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.content.AbstractJsonContentLoader;
import de.markusbordihn.dialogqueststoryengine.content.dialog.runtime.DialogRuntimeRegistry;
import de.markusbordihn.dialogqueststoryengine.content.dialog.runtime.JsonDialogRuntime;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.data.json.ParseResult;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public class DialogContentLoader extends AbstractJsonContentLoader<DialogDefinition> {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "dialogs");

  public DialogContentLoader() {
    super("data", "dqse/dialogs");
  }

  @Override
  protected ContentType contentType() {
    return ContentType.DIALOG;
  }

  @Override
  protected ParseResult<DialogDefinition> parse(
      ResourceLocation id, String filePath, JsonObject json) {
    return DialogContentParser.parse(id, filePath, json);
  }

  @Override
  protected void beforeLoad() {
    DialogClientRegistry.clear();
    DialogRuntimeRegistry.invalidateAll();
  }

  @Override
  protected void commit(Map<ResourceLocation, DialogDefinition> loaded) {
    DialogContentRegistry.replaceAll(loaded);
    loaded
        .values()
        .forEach(
            definition -> {
              DialogClientRegistry.put(definition);
              DialogRuntimeRegistry.put(new JsonDialogRuntime(definition));
            });
  }

  @Override
  protected String contentName() {
    return "dialog";
  }

  @Override
  public String getName() {
    return "dqse_dialogs";
  }
}
