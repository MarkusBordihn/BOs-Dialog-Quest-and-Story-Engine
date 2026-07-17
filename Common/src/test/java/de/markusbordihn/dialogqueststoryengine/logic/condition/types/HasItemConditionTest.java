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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import de.markusbordihn.dialogqueststoryengine.data.ContentType;
import de.markusbordihn.dialogqueststoryengine.data.issue.ContentIssue;
import de.markusbordihn.dialogqueststoryengine.data.issue.IssueCode;
import de.markusbordihn.dialogqueststoryengine.logic.condition.Condition;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.resources.ResourceLocation;
import org.junit.jupiter.api.Test;

class HasItemConditionTest {

  private static final Gson GSON = new Gson();
  private static final ResourceLocation TEST_ID = new ResourceLocation("test", "content");

  private static Condition parse(String raw, List<ContentIssue> issues) {
    return HasItemCondition.parse(
        GSON.fromJson(raw, JsonObject.class), ContentType.DIALOG, TEST_ID, "test.json", issues);
  }

  @Test
  void parsesRegisteredItem() {
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition = parse("{\"item\":\"minecraft:diamond\",\"count\":2}", issues);

    assertTrue(issues.isEmpty());
    assertInstanceOf(HasItemCondition.class, condition);
    assertEquals(2, ((HasItemCondition) condition).count());
  }

  @Test
  void rejectsUnregisteredItem() {
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition = parse("{\"item\":\"minecraft:not_a_real_item\"}", issues);

    assertSame(Condition.NEVER, condition);
    assertEquals(1, issues.size());
    assertEquals(IssueCode.UNKNOWN_REGISTRY_REFERENCE, issues.get(0).code());
  }

  @Test
  void rejectsMalformedItemId() {
    List<ContentIssue> issues = new ArrayList<>();
    Condition condition = parse("{\"item\":\"Not An Item\"}", issues);

    assertSame(Condition.NEVER, condition);
    assertTrue(
        issues.stream().anyMatch(issue -> issue.code() == IssueCode.INVALID_RESOURCE_LOCATION));
  }
}
