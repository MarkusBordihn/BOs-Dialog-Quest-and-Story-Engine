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
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.config.DqseSecurityConfig;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public record RunCommandAction(String command, int permissionLevel) implements Action {

  public static final ResourceLocation TYPE_ID =
      new ResourceLocation(Constants.MOD_NAMESPACE, "run_command");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  public static Action parse(
      JsonObject jsonObject,
      ContentType contentType,
      ResourceLocation contentId,
      String filePath,
      List<ContentIssue> issues) {
    if (!jsonObject.has("command") || !jsonObject.get("command").isJsonPrimitive()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "command"));
      return Action.NOOP;
    }

    String command = jsonObject.get("command").getAsString();
    if (command.isEmpty()) {
      issues.add(
          ContentIssue.of(IssueCode.MISSING_FIELD, contentType, contentId, filePath, "command"));
      return Action.NOOP;
    }

    return new RunCommandAction(command, DqseSecurityConfig.parsePermissionLevel(jsonObject));
  }

  @Override
  public void execute(ActionContext actionContext) {
    if (!DqseSecurityConfig.isCommandActionsEnabled()) {
      log.warn(
          "{} run_command is disabled by security config - skipping for event '{}'",
          Constants.LOG_PREFIX,
          actionContext.interactionEventId());
      return;
    }

    if (actionContext.server() == null || actionContext.player() == null) {
      return;
    }

    String resolved =
        this.command
            .replace("{player}", actionContext.player().getName().getString())
            .replace("{player_uuid}", actionContext.player().getStringUUID())
            .replace(
                "{dimension}", actionContext.player().level().dimension().location().toString());

    if (!DqseSecurityConfig.isCommandAllowed(resolved)) {
      log.warn(
          "{} run_command: command '{}' is not in the whitelist - skipping",
          Constants.LOG_PREFIX,
          resolved);
      return;
    }

    log.debug(
        "{} run_command executing for event '{}': {}",
        Constants.LOG_PREFIX,
        actionContext.interactionEventId(),
        resolved);

    try {
      actionContext
          .server()
          .getCommands()
          .getDispatcher()
          .execute(
              resolved,
              actionContext
                  .server()
                  .createCommandSourceStack()
                  .withPermission(this.permissionLevel));
    } catch (CommandSyntaxException e) {
      log.error(
          "{} run_command: command syntax error for '{}': {}",
          Constants.LOG_PREFIX,
          resolved,
          e.getMessage());
    }
  }
}
