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

package de.markusbordihn.dialogqueststoryengine.data.issue;

import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public final class ContentIssueTracker {

  private static final CopyOnWriteArrayList<ContentIssue> issues = new CopyOnWriteArrayList<>();

  private ContentIssueTracker() {}

  public static void record(ContentIssue issue) {
    issues.add(issue);
  }

  public static void clearFor(ContentType contentType) {
    issues.removeIf(issue -> issue.contentType() == contentType);
  }

  public static void clearByCode(IssueCode code) {
    issues.removeIf(issue -> issue.code() == code);
  }

  public static void clearAll() {
    issues.clear();
  }

  public static List<ContentIssue> issues() {
    return Collections.unmodifiableList(issues);
  }

  public static List<ContentIssue> issuesFor(ContentType contentType) {
    return issues.stream().filter(issue -> issue.contentType() == contentType).toList();
  }

  public static List<ContentIssue> errorsFor(ContentType contentType) {
    return issues.stream()
        .filter(
            issue -> issue.contentType() == contentType && issue.severity() == IssueSeverity.ERROR)
        .toList();
  }

  public static boolean hasErrors() {
    return issues.stream().anyMatch(issue -> issue.severity() == IssueSeverity.ERROR);
  }
}
