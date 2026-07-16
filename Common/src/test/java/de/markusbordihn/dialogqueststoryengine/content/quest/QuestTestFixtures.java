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

import de.markusbordihn.dialogqueststoryengine.content.NarrativeMetadata;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionList;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.resources.ResourceLocation;

public final class QuestTestFixtures {

  private QuestTestFixtures() {}

  public static void install(ResourceLocation... questIds) {
    Map<ResourceLocation, QuestDefinition> definitions =
        Arrays.stream(questIds).collect(Collectors.toMap(id -> id, QuestTestFixtures::definition));
    QuestContentRegistry.replaceAll(definitions);
  }

  public static void clear() {
    QuestContentRegistry.clear();
  }

  private static QuestDefinition definition(ResourceLocation questId) {
    return new QuestDefinition(
        questId,
        1,
        NarrativeMetadata.EMPTY,
        new DisplaySection(
            "title", "description", Optional.empty(), Optional.empty(), Optional.empty(), 0),
        new LogicSection(
            Optional.empty(), QuestPrerequisites.NONE, Map.of(), CompletionPolicy.ALL_STEPS, true),
        ActionList.EMPTY,
        RewardSection.EMPTY);
  }
}
