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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.JsonParser;
import de.markusbordihn.dialogqueststoryengine.content.quest.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class KillEntityStepTypeTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");

  private static RawQuestStep step(String json) {
    return new RawQuestStep(
        "hunt", KillEntityStepType.TYPE_ID, JsonParser.parseString(json).getAsJsonObject());
  }

  @Test
  void validateAcceptsValidEntity() {
    List<ContentIssue> issues = new ArrayList<>();
    new KillEntityStepType()
        .validate(
            QUEST_ID, step("{\"entity\": \"minecraft:zombie\", \"count\": 2}"), "file", issues);

    assertTrue(issues.isEmpty());
  }

  @Test
  void validateReportsMissingEntity() {
    List<ContentIssue> issues = new ArrayList<>();
    new KillEntityStepType().validate(QUEST_ID, step("{\"count\": 2}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void validateReportsMalformedEntityId() {
    List<ContentIssue> issues = new ArrayList<>();
    new KillEntityStepType()
        .validate(QUEST_ID, step("{\"entity\": \"Not An Entity\"}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_RESOURCE_LOCATION, issues.get(0).code());
  }

  @Test
  void locationStepTypeIsRegisteredButUnsupported() {
    LocationStepType locationStepType = new LocationStepType();
    assertFalse(locationStepType.supported());

    List<ContentIssue> issues = new ArrayList<>();
    locationStepType.validate(
        QUEST_ID,
        new RawQuestStep(
            "goto", LocationStepType.TYPE_ID, JsonParser.parseString("{}").getAsJsonObject()),
        "file",
        issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNSUPPORTED_STEP_TYPE, issues.get(0).code());
  }
}
