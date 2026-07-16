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

package de.markusbordihn.dialogqueststoryengine.content.quest;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import de.markusbordihn.dialogqueststoryengine.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import java.util.Map;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class QuestContentRegistryTest {

  private static final ResourceLocation TEST_ID_A = new ResourceLocation("test", "quest_a");
  private static final ResourceLocation TEST_ID_B = new ResourceLocation("test", "quest_b");

  private static QuestDefinition minimal(ResourceLocation id) {
    return new QuestDefinition(
        id,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "desc", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(
            Optional.empty(), QuestPrerequisites.NONE, Map.of(), CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }

  @AfterEach
  void clearRegistry() {
    QuestContentRegistry.clear();
  }

  @Test
  void replaceAllIsAtomic() {
    QuestContentRegistry.replaceAll(Map.of(TEST_ID_A, minimal(TEST_ID_A)));

    assertEquals(1, QuestContentRegistry.size());
    assertTrue(QuestContentRegistry.get(TEST_ID_A).isPresent());

    QuestContentRegistry.replaceAll(Map.of(TEST_ID_B, minimal(TEST_ID_B)));

    assertEquals(1, QuestContentRegistry.size());
    assertTrue(QuestContentRegistry.get(TEST_ID_B).isPresent());
    assertTrue(QuestContentRegistry.get(TEST_ID_A).isEmpty());
  }

  @Test
  void getReturnsEmptyForUnknown() {
    Optional<QuestDefinition> result =
        QuestContentRegistry.get(new ResourceLocation("test", "missing"));

    assertTrue(result.isEmpty());
  }

  @Test
  void allReturnsAllEntries() {
    QuestContentRegistry.replaceAll(
        Map.of(TEST_ID_A, minimal(TEST_ID_A), TEST_ID_B, minimal(TEST_ID_B)));

    assertEquals(2, QuestContentRegistry.all().size());
  }
}
