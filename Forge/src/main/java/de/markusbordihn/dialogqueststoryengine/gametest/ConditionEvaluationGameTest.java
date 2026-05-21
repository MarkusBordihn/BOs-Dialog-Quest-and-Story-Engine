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

package de.markusbordihn.dialogqueststoryengine.gametest;

import de.markusbordihn.dialogqueststoryengine.Constants;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

@SuppressWarnings("unused")
@PrefixGameTestTemplate(value = false)
@GameTestHolder(Constants.MOD_ID)
public class ConditionEvaluationGameTest {

  @GameTest(template = "gametest.3x3x3")
  public void testFactEqualsGatesDialogChoiceByPlayerState(GameTestHelper helper) {
    ConditionEvaluationGameTestHelper.testFactEqualsGatesDialogChoiceByPlayerState(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testQuestStateConditionInLiveContext(GameTestHelper helper) {
    ConditionEvaluationGameTestHelper.testQuestStateConditionInLiveContext(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testAllGroupRequiresBothConditionsTrue(GameTestHelper helper) {
    ConditionEvaluationGameTestHelper.testAllGroupRequiresBothConditionsTrue(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testAnyGroupTrueWhenOnlyOneConditionIsTrue(GameTestHelper helper) {
    ConditionEvaluationGameTestHelper.testAnyGroupTrueWhenOnlyOneConditionIsTrue(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testNestedGroupsTwoLevelsDeep(GameTestHelper helper) {
    ConditionEvaluationGameTestHelper.testNestedGroupsTwoLevelsDeep(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testUnknownConditionTypeNeverCrashes(GameTestHelper helper) {
    ConditionEvaluationGameTestHelper.testUnknownConditionTypeNeverCrashes(helper);
    helper.succeed();
  }
}
