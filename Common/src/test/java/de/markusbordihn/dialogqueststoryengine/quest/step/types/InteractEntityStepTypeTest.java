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
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class InteractEntityStepTypeTest {

  private static final ResourceLocation QUEST_ID = new ResourceLocation("test", "quest");
  private static final ResourceLocation VILLAGER = new ResourceLocation("minecraft", "villager");

  private static RawQuestStep step(String json) {
    return new RawQuestStep(
        "talk", InteractEntityStepType.TYPE_ID, JsonParser.parseString(json).getAsJsonObject());
  }

  @Test
  void matchesEntityTypeId() {
    assertTrue(InteractEntityStepType.matches("minecraft:villager", UUID.randomUUID(), VILLAGER));
  }

  @Test
  void rejectsDifferentEntityTypeId() {
    assertFalse(InteractEntityStepType.matches("minecraft:pig", UUID.randomUUID(), VILLAGER));
  }

  @Test
  void matchesSpecificEntityUuid() {
    UUID entityUuid = UUID.randomUUID();
    assertTrue(InteractEntityStepType.matches(entityUuid.toString(), entityUuid, VILLAGER));
  }

  @Test
  void rejectsDifferentEntityUuid() {
    assertFalse(
        InteractEntityStepType.matches(UUID.randomUUID().toString(), UUID.randomUUID(), VILLAGER));
  }

  @Test
  void rejectsMalformedTarget() {
    assertFalse(InteractEntityStepType.matches("Not A Valid Target", UUID.randomUUID(), VILLAGER));
  }

  @Test
  void validateAcceptsEntityTypeAndUuidTargets() {
    List<ContentIssue> issues = new ArrayList<>();
    new InteractEntityStepType()
        .validate(QUEST_ID, step("{\"target\": \"minecraft:villager\"}"), "file", issues);
    new InteractEntityStepType()
        .validate(QUEST_ID, step("{\"target\": \"" + UUID.randomUUID() + "\"}"), "file", issues);

    assertTrue(issues.isEmpty());
  }

  @Test
  void validateReportsMissingTarget() {
    List<ContentIssue> issues = new ArrayList<>();
    new InteractEntityStepType().validate(QUEST_ID, step("{}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.MISSING_FIELD, issues.get(0).code());
  }

  @Test
  void validateReportsMalformedTarget() {
    List<ContentIssue> issues = new ArrayList<>();
    new InteractEntityStepType()
        .validate(QUEST_ID, step("{\"target\": \"Not A Valid Target\"}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.INVALID_RESOURCE_LOCATION, issues.get(0).code());
  }

  @Test
  void validateReportsUnregisteredEntityTarget() {
    List<ContentIssue> issues = new ArrayList<>();
    new InteractEntityStepType()
        .validate(QUEST_ID, step("{\"target\": \"minecraft:not_a_real_entity\"}"), "file", issues);

    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_REGISTRY_REFERENCE, issues.get(0).code());
  }
}
