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

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record SetFactAction(FactScope scope, String fact, FactValue value) implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "set_fact");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has("scope") || !jsonObject.get("scope").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "scope"));
      return Action.NOOP;
    }

    FactScope scope =
        FactScope.fromName(jsonObject.get("scope").getAsString().toUpperCase(Locale.ROOT));
    if (scope == null) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              contentId,
              filePath,
              "scope",
              Map.of("value", jsonObject.get("scope").getAsString())));
      return Action.NOOP;
    }

    if (!jsonObject.has("fact") || !jsonObject.get("fact").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "fact"));
      return Action.NOOP;
    }

    String fact = jsonObject.get("fact").getAsString();
    if (fact.isEmpty()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "fact"));
      return Action.NOOP;
    }

    if (!jsonObject.has("value")) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "value"));
      return Action.NOOP;
    }

    JsonElement valueElement = jsonObject.get("value");
    if (!valueElement.isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(
              IssueCode.INVALID_FIELD_TYPE,
              contentType,
              contentId,
              filePath,
              "value",
              Map.of("expected", "boolean, number, or string")));
      return Action.NOOP;
    }

    return new SetFactAction(
        scope, fact, factValueFromPrimitive(valueElement.getAsJsonPrimitive()));
  }

  private static FactValue factValueFromPrimitive(JsonPrimitive primitive) {
    if (primitive.isBoolean()) {
      return FactValue.of(primitive.getAsBoolean());
    }

    if (primitive.isNumber()) {
      double doubleValue = primitive.getAsDouble();
      long longValue = (long) doubleValue;
      return doubleValue == longValue ? FactValue.of(longValue) : FactValue.of(doubleValue);
    }

    return FactValue.of(primitive.getAsString());
  }

  @Override
  public void execute(ActionContext actionContext) {
    if (this.scope != FactScope.PLAYER) {
      log.warn(
          "{} set_fact: non-PLAYER scope '{}' is not supported in V1 — skipping fact '{}'",
          Constants.LOG_PREFIX,
          this.scope,
          this.fact);
      return;
    }

    actionContext.playerState().setFact(this.scope, this.fact, this.value);
  }
}
