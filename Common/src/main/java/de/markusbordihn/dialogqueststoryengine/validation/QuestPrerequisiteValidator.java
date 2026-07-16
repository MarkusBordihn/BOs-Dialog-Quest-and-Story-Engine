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

import de.markusbordihn.dialogqueststoryengine.content.quest.QuestContentRegistry;
import de.markusbordihn.dialogqueststoryengine.content.quest.QuestDefinition;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ContentValidator;
import de.markusbordihn.dialogqueststoryengine.utils.DependencyGraph;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public final class QuestPrerequisiteValidator implements ContentValidator {

  private static final String FILE_PATH = "[cross-reference]";
  private static final String PATH = "logic.prerequisites.quests";

  private static List<ResourceLocation> validateEdges(
      QuestDefinition quest, Set<ResourceLocation> knownQuests, List<ContentIssue> issues) {
    List<ResourceLocation> validEdges = new ArrayList<>();
    Set<ResourceLocation> seen = new HashSet<>();
    for (ResourceLocation prerequisite : quest.logic().prerequisites().quests()) {
      if (!seen.add(prerequisite)) {
        issues.add(issue(IssueCode.DUPLICATE_QUEST_PREREQUISITE, quest.id(), prerequisite));
      } else if (prerequisite.equals(quest.id())) {
        issues.add(issue(IssueCode.SELF_QUEST_PREREQUISITE, quest.id(), prerequisite));
      } else if (!knownQuests.contains(prerequisite)) {
        issues.add(issue(IssueCode.UNKNOWN_QUEST_PREREQUISITE, quest.id(), prerequisite));
      } else {
        validEdges.add(prerequisite);
      }
    }
    return validEdges;
  }

  private static void reportCycles(
      Map<ResourceLocation, List<ResourceLocation>> edges, List<ContentIssue> issues) {
    for (ResourceLocation questId : DependencyGraph.findCycleNodes(edges)) {
      String cycleEdges =
          edges.get(questId).stream()
              .map(ResourceLocation::toString)
              .collect(Collectors.joining(", "));
      issues.add(
          ContentIssue.of(
              IssueCode.QUEST_PREREQUISITE_CYCLE,
              ContentType.QUEST,
              questId,
              FILE_PATH,
              PATH,
              Map.of("quests", cycleEdges)));
    }
  }

  private static ContentIssue issue(
      IssueCode code, ResourceLocation questId, ResourceLocation prerequisite) {
    return ContentIssue.of(
        code,
        ContentType.QUEST,
        questId,
        FILE_PATH,
        PATH,
        Map.of("quest", prerequisite.toString()));
  }

  @Override
  public ContentType contentType() {
    return ContentType.QUEST;
  }

  @Override
  public List<ContentIssue> validate() {
    List<ContentIssue> issues = new ArrayList<>();
    Set<ResourceLocation> knownQuests =
        QuestContentRegistry.all().stream().map(QuestDefinition::id).collect(Collectors.toSet());

    Map<ResourceLocation, List<ResourceLocation>> edges = new LinkedHashMap<>();
    for (QuestDefinition quest : QuestContentRegistry.all()) {
      edges.put(quest.id(), validateEdges(quest, knownQuests, issues));
    }

    reportCycles(edges, issues);
    return issues;
  }
}
