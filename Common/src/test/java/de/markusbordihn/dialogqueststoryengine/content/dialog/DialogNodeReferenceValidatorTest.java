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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionGroup;
import de.markusbordihn.dialogqueststoryengine.validation.DialogNodeReferenceValidator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class DialogNodeReferenceValidatorTest {

  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "dialog_ref");

  @AfterEach
  void clearRegistry() {
    DialogContentRegistry.clear();
  }

  @Test
  void detectsMissingNextNodeReference() {
    DialogChoiceDefinition badChoice =
        new DialogChoiceDefinition(
            "c1",
            "choice.label",
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY,
            Optional.of("nonexistent"),
            Optional.empty());
    DialogNodeDefinition node =
        new DialogNodeDefinition("root", "npc.name", "dialog.text", List.of(badChoice));
    DialogDefinition dialog = new DialogDefinition(TEST_ID, 1, "root", Map.of("root", node));
    DialogContentRegistry.replaceAll(Map.of(TEST_ID, dialog));

    List<ContentIssue> issues = new DialogNodeReferenceValidator().validate();

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_DIALOG_NODE, issues.get(0).code());
    assertEquals(TEST_ID, issues.get(0).id());
  }

  @Test
  void noIssuesForValidReferences() {
    DialogChoiceDefinition validChoice =
        new DialogChoiceDefinition(
            "c1",
            "choice.label",
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY,
            Optional.of("end"),
            Optional.empty());
    DialogNodeDefinition root =
        new DialogNodeDefinition("root", "npc.name", "dialog.text", List.of(validChoice));
    DialogNodeDefinition end = new DialogNodeDefinition("end", "npc.name", "dialog.end", List.of());
    DialogDefinition dialog =
        new DialogDefinition(TEST_ID, 1, "root", Map.of("root", root, "end", end));
    DialogContentRegistry.replaceAll(Map.of(TEST_ID, dialog));

    List<ContentIssue> issues = new DialogNodeReferenceValidator().validate();

    assertTrue(issues.isEmpty());
  }

  @Test
  void emptyNextOptionalIsNotFlagged() {
    DialogChoiceDefinition closeChoice =
        new DialogChoiceDefinition(
            "c1",
            "choice.close",
            ConditionGroup.ALWAYS_TRUE,
            ActionList.EMPTY,
            Optional.empty(),
            Optional.empty());
    DialogNodeDefinition root =
        new DialogNodeDefinition("root", "npc.name", "dialog.text", List.of(closeChoice));
    DialogDefinition dialog = new DialogDefinition(TEST_ID, 1, "root", Map.of("root", root));
    DialogContentRegistry.replaceAll(Map.of(TEST_ID, dialog));

    List<ContentIssue> issues = new DialogNodeReferenceValidator().validate();

    assertTrue(issues.isEmpty());
  }
}
