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

package de.markusbordihn.dialogqueststoryengine.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ContentValidator;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ValidationReloadListenerTest {

  private static final ResourceLocation ALWAYS_ERROR_VALIDATOR_ID =
      new ResourceLocation("test", "always_error");
  private static final ResourceLocation NO_ISSUE_VALIDATOR_ID =
      new ResourceLocation("test", "no_issue");

  static {
    Registries.VALIDATORS.register(
        ALWAYS_ERROR_VALIDATOR_ID,
        new ContentValidator() {
          @Override
          public ContentType contentType() {
            return ContentType.DIALOG;
          }

          @Override
          public List<ContentIssue> validate() {
            return List.of(
                ContentIssue.of(
                    IssueCode.MISSING_DIALOG_NODE,
                    ContentType.DIALOG,
                    new ResourceLocation("test", "bad_dialog"),
                    "[cross-reference]",
                    "nodes.root.choices.c1.next"));
          }
        });

    Registries.VALIDATORS.register(
        NO_ISSUE_VALIDATOR_ID,
        new ContentValidator() {
          @Override
          public ContentType contentType() {
            return ContentType.DIALOG;
          }

          @Override
          public List<ContentIssue> validate() {
            return List.of();
          }
        });
  }

  @BeforeEach
  void clearIssues() {
    ContentIssueTracker.clearAll();
  }

  private void runReloadSynchronously() throws Exception {
    PreparableReloadListener.PreparationBarrier barrier =
        new PreparableReloadListener.PreparationBarrier() {
          @Override
          public <T> java.util.concurrent.CompletableFuture<T> wait(T value) {
            return java.util.concurrent.CompletableFuture.completedFuture(value);
          }
        };
    PreparableReloadListener listener = new ValidationReloadListener();
    listener
        .reload(barrier, null, null, null, Runnable::run, Runnable::run)
        .toCompletableFuture()
        .join();
  }

  @Test
  void validatorIssuesAppearInTracker() throws Exception {
    this.runReloadSynchronously();

    assertTrue(ContentIssueTracker.issues().size() >= 1);
    assertTrue(
        ContentIssueTracker.issues().stream()
            .anyMatch(issue -> issue.code() == IssueCode.MISSING_DIALOG_NODE));
  }

  @Test
  void reloadCanBeCalledMultipleTimes() throws Exception {
    this.runReloadSynchronously();
    int issuesAfterFirst = ContentIssueTracker.issues().size();

    this.runReloadSynchronously();

    assertEquals(issuesAfterFirst, ContentIssueTracker.issues().size());
  }

  @Test
  void reloadNotificationFiresOnceAfterValidation() throws Exception {
    AtomicInteger calls = new AtomicInteger();
    Runnable listener = calls::incrementAndGet;
    DataPackReloadNotifier.subscribe(listener);
    try {
      this.runReloadSynchronously();
    } finally {
      DataPackReloadNotifier.unsubscribe(listener);
    }

    assertEquals(1, calls.get());
  }
}
