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

package de.markusbordihn.dialogqueststoryengine.logic.action.types;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.action.Action;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class GiveExperienceActionTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");

  private static Action parse(String raw, List<ContentIssue> issues) {
    return GiveExperienceAction.parse(
        GSON.fromJson(raw, JsonObject.class), ContentType.QUEST, TEST_ID, "test.json", issues);
  }

  @Test
  void parsesPositiveAmount() {
    List<ContentIssue> issues = new ArrayList<>();
    Action action = parse("{\"amount\":100}", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(GiveExperienceAction.class, action);
    assertEquals(100, ((GiveExperienceAction) action).amount());
  }

  @Test
  void rejectsNonPositiveAmount() {
    List<ContentIssue> issues = new ArrayList<>();
    Action action = parse("{\"amount\":0}", issues);

    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_REWARD_AMOUNT));
    assertSame(Action.NOOP, action);
  }

  @Test
  void rejectsMissingAmount() {
    List<ContentIssue> issues = new ArrayList<>();
    Action action = parse("{}", issues);

    assertTrue(issues.stream().anyMatch(issue -> issue.code() == IssueCode.MISSING_FIELD));
    assertSame(Action.NOOP, action);
  }
}
