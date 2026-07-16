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
import de.markusbordihn.dialogqueststoryengine.data.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import java.util.List;
import net.minecraft.resources.ResourceLocation;

public record FactExistsCondition(FactScope scope, String fact) implements Condition {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "fact_exists");

  public static Condition parse(
      JsonObject json,
      ContentType contentType,
      ResourceLocation id,
      String filePath,
      List<ContentIssue> issues) {
    FactScope scope = FactConditionFields.parseScope(json, contentType, id, filePath, issues);
    if (scope == null) {
      return Condition.NEVER;
    }

    String fact = FactConditionFields.parseFact(json, contentType, id, filePath, issues);
    if (fact == null) {
      return Condition.NEVER;
    }

    return new FactExistsCondition(scope, fact);
  }

  @Override
  public boolean evaluate(ConditionContext conditionContext) {
    return conditionContext.playerState().getFact(this.scope, this.fact) != null;
  }
}
