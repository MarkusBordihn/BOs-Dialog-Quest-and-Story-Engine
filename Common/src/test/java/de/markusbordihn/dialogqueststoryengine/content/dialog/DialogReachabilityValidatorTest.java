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

package de.markusbordihn.dialogqueststoryengine.content.dialog;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.validation.DialogReachabilityValidator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DialogReachabilityValidatorTest {

  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "dialog_reach");

  @AfterEach
  void clearRegistry() {
    DialogContentRegistry.clear();
  }

  @Test
  void detectsUnreachableNode() {
    DialogNodeDefinition root = new DialogNodeDefinition("root", "npc", "text.root", List.of());
    DialogNodeDefinition orphan =
        new DialogNodeDefinition("orphan", "npc", "text.orphan", List.of());
    DialogDefinition dialog =
        new DialogDefinition(TEST_ID, 1, "root", Map.of("root", root, "orphan", orphan));
    DialogContentRegistry.replaceAll(Map.of(TEST_ID, dialog));

    var issues = new DialogReachabilityValidator().validate();

    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.UNREACHABLE_DIALOG_NODE));
  }

  @Test
  void detectsCycleInDialog() {
    DialogChoiceDefinition backToA =
        new DialogChoiceDefinition(
            "c1", "lbl", List.of(), List.of(), Optional.of("node_a"), Optional.empty());
    DialogNodeDefinition nodeA =
        new DialogNodeDefinition("node_a", "npc", "text.a", List.of(backToA));
    DialogDefinition dialog = new DialogDefinition(TEST_ID, 1, "node_a", Map.of("node_a", nodeA));
    DialogContentRegistry.replaceAll(Map.of(TEST_ID, dialog));

    var issues = new DialogReachabilityValidator().validate();

    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.CIRCULAR_DIALOG_FLOW));
  }

  @Test
  void noIssuesForLinearDialog() {
    DialogChoiceDefinition toEnd =
        new DialogChoiceDefinition(
            "c1", "lbl", List.of(), List.of(), Optional.of("end"), Optional.empty());
    DialogNodeDefinition root =
        new DialogNodeDefinition("root", "npc", "text.root", List.of(toEnd));
    DialogNodeDefinition end = new DialogNodeDefinition("end", "npc", "text.end", List.of());
    DialogDefinition dialog =
        new DialogDefinition(TEST_ID, 1, "root", Map.of("root", root, "end", end));
    DialogContentRegistry.replaceAll(Map.of(TEST_ID, dialog));

    var issues = new DialogReachabilityValidator().validate();

    assertFalse(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.UNREACHABLE_DIALOG_NODE));
    assertFalse(issues.stream().anyMatch(issue -> issue.code() == IssueCode.CIRCULAR_DIALOG_FLOW));
  }
}
