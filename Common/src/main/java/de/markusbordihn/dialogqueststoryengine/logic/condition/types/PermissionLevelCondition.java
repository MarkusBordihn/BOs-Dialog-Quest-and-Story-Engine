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

package de.markusbordihn.dialogqueststoryengine.logic.condition.types;

import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import java.util.List;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;

public record PermissionLevelCondition(int level) implements Condition {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "permission_level");

  private static final int MIN_LEVEL = 0;
  private static final int MAX_LEVEL = 4;

  public static Condition parse(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    if (!json.has("level") || !json.get("level").isJsonPrimitive()) {
      issues.add(ContentIssue.of(IssueCode.MISSING_FIELD, contentType, id, filePath, "level"));
      return Condition.NEVER;
    }

    int level = json.get("level").getAsInt();
    if (level < MIN_LEVEL || level > MAX_LEVEL) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              id,
              filePath,
              "level",
              Map.of("value", String.valueOf(level), "reason", "level must be between 0 and 4")));
      return Condition.NEVER;
    }

    return new PermissionLevelCondition(level);
  }

  @Override
  public boolean evaluate(ConditionContext conditionContext) {
    if (conditionContext.player() == null) {
      return false;
    }

    return conditionContext.player().hasPermissions(this.level);
  }
}
