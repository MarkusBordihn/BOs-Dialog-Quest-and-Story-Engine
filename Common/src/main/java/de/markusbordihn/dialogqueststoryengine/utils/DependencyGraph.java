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

package de.markusbordihn.dialogqueststoryengine.utils;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class DependencyGraph {

  private DependencyGraph() {}

  /**
   * Returns every node that cannot be topologically resolved via Kahn's algorithm, i.e. the nodes
   * participating in a dependency cycle. Each map entry lists the prerequisites its node depends
   * on; prerequisites are expected to be nodes of the graph.
   */
  public static <K> Set<K> findCycleNodes(Map<K, ? extends Collection<K>> edges) {
    Map<K, Integer> pending = new LinkedHashMap<>();
    Map<K, List<K>> dependents = new LinkedHashMap<>();
    ArrayDeque<K> resolvable = new ArrayDeque<>();

    for (Map.Entry<K, ? extends Collection<K>> entry : edges.entrySet()) {
      pending.put(entry.getKey(), entry.getValue().size());
      if (entry.getValue().isEmpty()) {
        resolvable.add(entry.getKey());
      }
      for (K prerequisite : entry.getValue()) {
        dependents.computeIfAbsent(prerequisite, key -> new ArrayList<>()).add(entry.getKey());
      }
    }

    while (!resolvable.isEmpty()) {
      K node = resolvable.poll();
      for (K dependent : dependents.getOrDefault(node, List.of())) {
        if (pending.merge(dependent, -1, Integer::sum) == 0) {
          resolvable.add(dependent);
        }
      }
    }

    Set<K> cycleNodes = new LinkedHashSet<>();
    pending.forEach(
        (node, remaining) -> {
          if (remaining > 0) {
            cycleNodes.add(node);
          }
        });
    return cycleNodes;
  }
}
