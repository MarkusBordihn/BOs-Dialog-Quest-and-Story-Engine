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

package de.markusbordihn.dialogqueststoryengine.quest.step.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.content.quest.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class CollectItemStepTypeTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  private static RawQuestStep step(String json) {
    return new RawQuestStep(
        "collect", CollectItemStepType.TYPE_ID, JsonParser.parseString(json).getAsJsonObject());
  }

  @Test
  void baselineKeyIsStablePerQuestAndStep() {
    assertEquals(
        "dqse.collect_baseline.test:quest.collect",
        CollectItemStepType.baselineKey(QUEST_ID, "collect"));
  }

  @Test
  void computeProgressSubtractsBaseline() {
    assertEquals(5, CollectItemStepType.computeProgress(8, 3));
  }

  @Test
  void computeProgressNeverGoesNegative() {
    assertEquals(0, CollectItemStepType.computeProgress(2, 5));
  }

  @Test
  void computeProgressWithoutBaselineCountsEverything() {
    assertEquals(7, CollectItemStepType.computeProgress(7, 0));
  }

  @Test
  void validateAcceptsValidItem() {
    List<ContentIssue> issues = new ArrayList<>();
    new CollectItemStepType()
        .validate(
            QUEST_ID, step("{\"item\": \"minecraft:oak_log\", \"count\": 5}"), "file", issues);

    assertTrue(issues.isEmpty());
  }

  @Test
  void validateReportsMissingItem() {
    List<ContentIssue> issues = new ArrayList<>();
    new CollectItemStepType().validate(QUEST_ID, step("{\"count\": 5}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void validateReportsMalformedItemId() {
    List<ContentIssue> issues = new ArrayList<>();
    new CollectItemStepType()
        .validate(QUEST_ID, step("{\"item\": \"Not An Item\"}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_RESOURCE_LOCATION, issues.get(0).code());
  }
}
