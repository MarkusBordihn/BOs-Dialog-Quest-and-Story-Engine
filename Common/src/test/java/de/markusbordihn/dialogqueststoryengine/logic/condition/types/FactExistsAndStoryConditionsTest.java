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

package de.markusbordihn.dialogqueststoryengine.logic.condition.types;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.ConditionContext;
import de.markusbordihn.dialogqueststoryengine.state.FactScope;
import de.markusbordihn.dialogqueststoryengine.state.FactValue;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.ArrayList;
import java.util.UUID;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class FactExistsAndStoryConditionsTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");
  private static final ResourceLocation STORY_ID = new ResourceLocation("test", "chapter_1");

  private static JsonObject json(String raw) {
    return GSON.fromJson(raw, JsonObject.class);
  }

  @Test
  void factExistsReflectsPresence() {
    Condition condition =
        FactExistsCondition.parse(
            json("{\"scope\":\"player\",\"fact\":\"seen_intro\"}"),
            ContentType.DIALOG,
            TEST_ID,
            "test.json",
            new ArrayList<>());

    PlayerState withFact = new PlayerState(UUID.randomUUID());
    withFact.setFact(FactScope.PLAYER, "seen_intro", FactValue.of(true));

    assertTrue(condition.evaluate(ConditionContext.ofTest(withFact)));
    assertFalse(condition.evaluate(ConditionContext.ofTest(new PlayerState(UUID.randomUUID()))));
  }

  @Test
  void storyUnlockedReflectsProgress() {
    Condition condition =
        StoryUnlockedCondition.parse(
            json("{\"story\":\"test:chapter_1\"}"),
            ContentType.DIALOG,
            TEST_ID,
            "test.json",
            new ArrayList<>());

    PlayerState playerState = new PlayerState(UUID.randomUUID());
    assertFalse(condition.evaluate(ConditionContext.ofTest(playerState)));

    playerState.stories().unlock(STORY_ID);
    assertTrue(condition.evaluate(ConditionContext.ofTest(playerState)));
  }

  @Test
  void storyReadReflectsProgress() {
    Condition condition =
        StoryReadCondition.parse(
            json("{\"story\":\"test:chapter_1\"}"),
            ContentType.DIALOG,
            TEST_ID,
            "test.json",
            new ArrayList<>());

    PlayerState playerState = new PlayerState(UUID.randomUUID());
    playerState.stories().unlock(STORY_ID);
    assertFalse(condition.evaluate(ConditionContext.ofTest(playerState)));

    playerState.stories().markRead(STORY_ID);
    assertTrue(condition.evaluate(ConditionContext.ofTest(playerState)));
  }
}
