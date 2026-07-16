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
public class DialogWorkflowGameTest {

  @GameTest(template = "gametest.3x3x3")
  public void testThreeNodeNavigation(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testThreeNodeNavigation(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testGatedChoiceFilteredFromOpenPacket(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testGatedChoiceFilteredFromOpenPacket(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testBuiltinCloseEndsSession(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testBuiltinCloseEndsSession(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testQuestStartChoiceLandsOnNextNode(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testQuestStartChoiceLandsOnNextNode(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testStaleRevisionRejectedAndStateUnchanged(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testStaleRevisionRejectedAndStateUnchanged(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testUnknownChoiceRejected(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testUnknownChoiceRejected(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testChoiceButtonPacketNavigates(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testChoiceButtonPacketNavigates(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testChoiceButtonPacketStartsQuest(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testChoiceButtonPacketStartsQuest(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testCloseButtonPacketEndsSession(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testCloseButtonPacketEndsSession(helper);
    helper.succeed();
  }

  @GameTest(template = "gametest.3x3x3")
  public void testStaleChoiceButtonPacketRejected(GameTestHelper helper) {
    DialogWorkflowGameTestHelper.testStaleChoiceButtonPacketRejected(helper);
    helper.succeed();
  }
}
