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
import de.markusbordihn.dialogqueststoryengine.state.StoryProgressService;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record UnlockStoryAction(ResourceLocation storyId) implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "unlock_story");

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has("story") || !jsonObject.get("story").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "story"));
      return Action.NOOP;
    }

    ResourceLocation storyId = ResourceLocation.tryParse(jsonObject.get("story").getAsString());
    if (storyId == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_RESOURCE_LOCATION,
              contentType,
              contentId,
              filePath,
              "story",
              Map.of("value", jsonObject.get("story").getAsString())));
      return Action.NOOP;
    }

    return new UnlockStoryAction(storyId);
  }

  @Override
  public void execute(ActionContext actionContext) {
    StoryProgressService.unlock(actionContext.player(), actionContext.playerState(), this.storyId);
  }
}
