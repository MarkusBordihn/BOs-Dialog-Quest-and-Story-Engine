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
import net.minecraft.resources.ResourceLocation;

public record GiveExperienceAction(int amount) implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "give_experience");

  private static final String FIELD_AMOUNT = "amount";

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has(FIELD_AMOUNT)
        || !jsonObject.get(FIELD_AMOUNT).isJsonPrimitive()
        || !jsonObject.get(FIELD_AMOUNT).getAsJsonPrimitive().isNumber()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, FIELD_AMOUNT));
      return Action.NOOP;
    }

    int amount = jsonObject.get(FIELD_AMOUNT).getAsInt();
    if (amount <= 0) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_REWARD_AMOUNT,
              contentType,
              contentId,
              filePath,
              FIELD_AMOUNT,
              Map.of("value", String.valueOf(amount))));
      return Action.NOOP;
    }
    return new GiveExperienceAction(amount);
  }

  @Override
  public void execute(ActionContext actionContext) {
    if (actionContext.player() != null) {
      actionContext.player().giveExperiencePoints(this.amount);
    }
  }
}
