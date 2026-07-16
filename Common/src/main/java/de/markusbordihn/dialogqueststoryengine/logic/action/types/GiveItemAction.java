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

package de.markusbordihn.dialogqueststoryengine.logic.action.types;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import java.util.List;
import java.util.Map;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record GiveItemAction(ResourceLocation itemId, int count) implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "give_item");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has("item") || !jsonObject.get("item").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "item"));
      return Action.NOOP;
    }
    ResourceLocation itemId = ResourceLocation.tryParse(jsonObject.get("item").getAsString());
    if (itemId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              contentId,
              filePath,
              "item",
              Map.of("value", jsonObject.get("item").getAsString())));
      return Action.NOOP;
    }

    int count = 1;
    if (jsonObject.has("count") && jsonObject.get("count").isJsonPrimitive()) {
      count = Math.max(1, jsonObject.get("count").getAsInt());
    }

    return new GiveItemAction(itemId, count);
  }

  @Override
  public void execute(ActionContext actionContext) {
    if (actionContext.player() == null) {
      return;
    }
    Item item = BuiltInRegistries.ITEM.getOptional(this.itemId).orElse(null);
    if (item == null) {
      log.warn("{} give_item: unknown item '{}' - skipping", Constants.LOG_PREFIX, this.itemId);
      return;
    }
    ItemStack stack = new ItemStack(item, this.count);
    if (!actionContext.player().getInventory().add(stack)) {
      actionContext.player().drop(stack, false);
    }
  }
}
