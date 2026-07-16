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

import de.markusbordihn.dialogqueststoryengine.content.dialog.DialogContentRegistry;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogDefinition;
import de.markusbordihn.dialogqueststoryengine.data.dialog.DialogNodeDefinition;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ContentValidator;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class DialogNodeReferenceValidator implements ContentValidator {

  @Override
  public ContentType contentType() {
    return ContentType.DIALOG;
  }

  @Override
  public List<ContentIssue> validate() {
    List<ContentIssue> issues = new ArrayList<>();
    for (DialogDefinition dialog : DialogContentRegistry.all()) {
      Map<String, DialogNodeDefinition> nodes = dialog.nodes();
      for (DialogNodeDefinition node : nodes.values()) {
        for (var choice : node.choices()) {
          choice
              .next()
              .ifPresent(
                  nextId -> {
                    if (!nodes.containsKey(nextId)) {
                      issues.add(
                          ContentIssue.of(
                              IssueCode.MISSING_DIALOG_NODE,
                              ContentType.DIALOG,
                              dialog.id(),
                              "[cross-reference]",
                              "nodes." + node.id() + ".choices." + choice.id() + ".next",
                              Map.of("next", nextId)));
                    }
                  });
        }
      }
    }

    return issues;
  }
}
