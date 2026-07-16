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
import de.markusbordihn.dialogqueststoryengine.content.quest.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.registry.ContentValidator;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class QuestStepValidator implements ContentValidator {

  private static final String FILE_PATH = "[cross-reference]";

  @Override
  public ContentType contentType() {
    return ContentType.QUEST;
  }

  @Override
  public List<ContentIssue> validate() {
    List<ContentIssue> issues = new ArrayList<>();
    for (QuestDefinition quest : QuestContentRegistry.all()) {
      for (RawQuestStep step : quest.logic().steps().values()) {
        Registries.QUEST_STEPS
            .get(step.type())
            .ifPresentOrElse(
                handler -> handler.validate(quest.id(), step, FILE_PATH, issues),
                () ->
                    issues.add(
                        ContentIssue.of(
                            IssueCode.UNKNOWN_STEP_TYPE,
                            ContentType.QUEST,
                            quest.id(),
                            FILE_PATH,
                            "logic.steps." + step.id() + ".type",
                            Map.of("type", step.type().toString()))));
      }
    }

    return issues;
  }
}
