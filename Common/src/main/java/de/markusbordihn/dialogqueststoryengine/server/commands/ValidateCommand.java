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

package de.markusbordihn.dialogqueststoryengine.server.commands;

import com.mojang.brigadier.builder.ArgumentBuilder;
import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.commands.Command;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueSeverity;
import de.markusbordihn.dialogqueststoryengine.validation.ValidationService;
import java.util.List;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidateCommand extends Command {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);
  private static final int MAX_INLINE_ISSUES = 15;

  private ValidateCommand() {}

  public static ArgumentBuilder<CommandSourceStack, ?> register() {
    return Commands.literal("validate")
        .requires(source -> source.hasPermission(PERMISSION_LEVEL))
        .executes(context -> executeValidate(context.getSource()));
  }

  private static int executeValidate(CommandSourceStack source) {
    ValidationService.validate();
    List<ContentIssue> issues = ContentIssueTracker.issues();
    long errorCount = 0;
    long warningCount = 0;
    for (ContentIssue issue : issues) {
      if (issue.severity() == IssueSeverity.ERROR) {
        errorCount++;
      } else if (issue.severity() == IssueSeverity.WARNING) {
        warningCount++;
      }
    }

    if (issues.isEmpty()) {
      sendSuccessMessage(source, "Validation passed - 0 errors, 0 warnings.");
      return 1;
    }

    sendInfoMessage(
        source,
        "Validation: "
            + errorCount
            + " error(s), "
            + warningCount
            + " warning(s) - showing first "
            + Math.min(issues.size(), MAX_INLINE_ISSUES)
            + ":");

    int shown = 0;
    for (ContentIssue issue : issues) {
      if (shown >= MAX_INLINE_ISSUES) {
        break;
      }

      String location =
          issue.file() != null && !issue.file().isEmpty() ? issue.file() : "[cross-reference]";
      String field = issue.field() != null ? " @ " + issue.field() : "";
      sendInfoMessage(
          source,
          "  ["
              + issue.severity()
              + "] "
              + issue.code().name()
              + " | "
              + issue.contentType()
              + " "
              + issue.id()
              + " | "
              + location
              + field);
      shown++;
    }

    if (issues.size() > MAX_INLINE_ISSUES) {
      int remaining = issues.size() - MAX_INLINE_ISSUES;
      sendInfoMessage(source, "  ... and " + remaining + " more - see log for full report.");
      issues.stream()
          .skip(MAX_INLINE_ISSUES)
          .forEach(issue -> log.warn("{} {}", Constants.LOG_PREFIX, formatIssue(issue)));
    }

    return errorCount > 0 ? 0 : 1;
  }

  private static String formatIssue(ContentIssue issue) {
    String location =
        issue.file() != null && !issue.file().isEmpty() ? issue.file() : "[cross-reference]";
    String field = issue.field() != null ? " @ " + issue.field() : "";
    return "["
        + issue.severity()
        + "] "
        + issue.code().name()
        + " | "
        + issue.contentType()
        + " "
        + issue.id()
        + " | "
        + location
        + field;
  }
}
