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

package de.markusbordihn.dialogqueststoryengine.data.json;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.config.ValidationConfig;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueSeverity;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class RegistryReferenceValidatorTest {

  private static final ResourceLocation CONTENT_ID = new ResourceLocation("test", "content");
  private static final ResourceLocation KNOWN_ITEM = new ResourceLocation("minecraft", "diamond");
  private static final ResourceLocation UNKNOWN_ITEM =
      new ResourceLocation("minecraft", "not_a_real_item");

  private static boolean requireItem(ResourceLocation item, List<ContentIssue> issues) {
    return RegistryReferenceValidator.requireRegistered(
        BuiltInRegistries.ITEM, item, ContentType.QUEST, CONTENT_ID, "file", "item", issues);
  }

  @AfterEach
  void resetToStrict() {
    ValidationConfig.configure(true);
  }

  @Test
  void registeredReferencePassesWithoutIssue() {
    List<ContentIssue> issues = new ArrayList<>();

    assertTrue(requireItem(KNOWN_ITEM, issues));
    assertTrue(issues.isEmpty());
  }

  @Test
  void strictModeRejectsUnknownReferenceAsError() {
    ValidationConfig.configure(true);
    List<ContentIssue> issues = new ArrayList<>();

    assertFalse(requireItem(UNKNOWN_ITEM, issues));
    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_REGISTRY_REFERENCE, issues.get(0).code());
    assertEquals(IssueSeverity.ERROR, issues.get(0).severity());
  }

  @Test
  void lenientModeToleratesUnknownReferenceAsWarning() {
    ValidationConfig.configure(false);
    List<ContentIssue> issues = new ArrayList<>();

    assertTrue(requireItem(UNKNOWN_ITEM, issues));
    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_REGISTRY_REFERENCE, issues.get(0).code());
    assertEquals(IssueSeverity.WARNING, issues.get(0).severity());
  }
}
