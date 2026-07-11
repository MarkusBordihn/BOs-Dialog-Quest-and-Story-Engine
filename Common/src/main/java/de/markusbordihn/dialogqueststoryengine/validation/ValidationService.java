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

package de.markusbordihn.dialogqueststoryengine.validation;

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ContentValidator;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class ValidationService {

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  private ValidationService() {}

  public static int validate() {
    clearPreviousValidationIssues();
    int issuesBefore = ContentIssueTracker.issues().size();
    for (ContentValidator validator : Registries.VALIDATORS.values()) {
      try {
        validator.validate().forEach(ContentIssueTracker::record);
      } catch (RuntimeException exception) {
        log.error(
            "{} Validator '{}' threw an exception: {}",
            Constants.LOG_PREFIX,
            validator.contentType(),
            exception.getMessage(),
            exception);
      }
    }
    return ContentIssueTracker.issues().size() - issuesBefore;
  }

  private static void clearPreviousValidationIssues() {
    ContentIssueTracker.clearByCode(IssueCode.MISSING_DIALOG_NODE);
    ContentIssueTracker.clearByCode(IssueCode.UNREACHABLE_DIALOG_NODE);
    ContentIssueTracker.clearByCode(IssueCode.CIRCULAR_DIALOG_FLOW);
  }
}
