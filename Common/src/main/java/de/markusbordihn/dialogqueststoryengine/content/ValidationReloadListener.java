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

import de.markusbordihn.dialogqueststoryengine.Constants;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssueTracker;
import de.markusbordihn.dialogqueststoryengine.registry.ContentValidator;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import de.markusbordihn.dialogqueststoryengine.session.SessionManager;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.PreparableReloadListener;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.util.profiling.ProfilerFiller;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class ValidationReloadListener implements PreparableReloadListener {

  public static final ResourceLocation ID = new ResourceLocation(Constants.MOD_ID, "validation");

  private static final Logger log = LogManager.getLogger(Constants.LOG_NAME);

  @Override
  public CompletableFuture<Void> reload(
      PreparationBarrier barrier,
      ResourceManager resourceManager,
      ProfilerFiller prepareProfiler,
      ProfilerFiller applyProfiler,
      Executor backgroundExecutor,
      Executor gameExecutor) {
    return CompletableFuture.runAsync(() -> {}, backgroundExecutor)
        .thenCompose(barrier::wait)
        .thenRunAsync(this::runValidators, gameExecutor);
  }

  private void runValidators() {
    SessionManager.invalidateAll();

    int issuesBefore = ContentIssueTracker.issues().size();

    for (ContentValidator validator : Registries.VALIDATORS.values()) {
      try {
        validator.validate().forEach(ContentIssueTracker::record);
      } catch (Exception exception) {
        log.error(
            "{} Validator '{}' threw an exception: {}",
            Constants.LOG_PREFIX,
            validator.contentType(),
            exception.getMessage(),
            exception);
      }
    }

    int newIssues = ContentIssueTracker.issues().size() - issuesBefore;
    if (newIssues > 0) {
      log.warn(
          "{} Validation found {} new issue(s). Run /dqs validate for details.",
          Constants.LOG_PREFIX,
          newIssues);
    } else {
      log.info("{} Validation passed with no new issues.", Constants.LOG_PREFIX);
    }
  }
}
