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
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;

public final class DialogReachabilityValidator implements ContentValidator {

  @Override
  public ContentType contentType() {
    return ContentType.DIALOG;
  }

  @Override
  public List<ContentIssue> validate() {
    List<ContentIssue> issues = new ArrayList<>();
    for (DialogDefinition dialog : DialogContentRegistry.all()) {
      issues.addAll(this.validateReachability(dialog));
      issues.addAll(this.validateCycles(dialog));
    }

    return issues;
  }

  private List<ContentIssue> validateReachability(DialogDefinition dialog) {
    List<ContentIssue> issues = new ArrayList<>();
    Map<String, DialogNodeDefinition> nodes = dialog.nodes();
    String startNode = dialog.startNode();

    if (!nodes.containsKey(startNode)) {
      return issues;
    }

    Set<String> reachable = this.bfsReachable(startNode, nodes);
    for (String nodeId : nodes.keySet()) {
      if (!reachable.contains(nodeId)) {
        issues.add(
            ContentIssue.of(
                IssueCode.UNREACHABLE_DIALOG_NODE,
                ContentType.DIALOG,
                dialog.id(),
                "[cross-reference]",
                "nodes." + nodeId,
                Map.of("node", nodeId, "startNode", startNode)));
      }
    }

    return issues;
  }

  private List<ContentIssue> validateCycles(DialogDefinition dialog) {
    List<ContentIssue> issues = new ArrayList<>();
    Map<String, DialogNodeDefinition> nodes = dialog.nodes();

    Set<String> visited = new HashSet<>();
    Set<String> inStack = new HashSet<>();

    for (String nodeId : nodes.keySet()) {
      if (!visited.contains(nodeId) && this.dfsCycleDetect(nodeId, nodes, visited, inStack)) {
        issues.add(
            ContentIssue.of(
                IssueCode.CIRCULAR_DIALOG_FLOW,
                ContentType.DIALOG,
                dialog.id(),
                "[cross-reference]",
                "nodes." + nodeId));
        break;
      }
    }

    return issues;
  }

  private Set<String> bfsReachable(String startNode, Map<String, DialogNodeDefinition> nodes) {
    Set<String> visited = new HashSet<>();
    Queue<String> queue = new ArrayDeque<>();
    queue.add(startNode);

    while (!queue.isEmpty()) {
      String current = queue.poll();
      if (!visited.add(current)) {
        continue;
      }

      DialogNodeDefinition node = nodes.get(current);
      if (node == null) {
        continue;
      }

      for (var choice : node.choices()) {
        choice
            .next()
            .ifPresent(
                nextId -> {
                  if (!visited.contains(nextId) && nodes.containsKey(nextId)) {
                    queue.add(nextId);
                  }
                });
      }
    }

    return visited;
  }

  private boolean dfsCycleDetect(
      String nodeId,
      Map<String, DialogNodeDefinition> nodes,
      Set<String> visited,
      Set<String> inStack) {
    visited.add(nodeId);
    inStack.add(nodeId);

    DialogNodeDefinition node = nodes.get(nodeId);
    if (node != null) {
      for (var choice : node.choices()) {
        if (choice.next().isPresent()) {
          String nextId = choice.next().get();
          if (!nodes.containsKey(nextId)) {
            continue;
          }

          if (!visited.contains(nextId)) {
            if (this.dfsCycleDetect(nextId, nodes, visited, inStack)) {
              return true;
            }
          } else if (inStack.contains(nextId)) {
            return true;
          }
        }
      }
    }

    inStack.remove(nodeId);
    return false;
  }
}
