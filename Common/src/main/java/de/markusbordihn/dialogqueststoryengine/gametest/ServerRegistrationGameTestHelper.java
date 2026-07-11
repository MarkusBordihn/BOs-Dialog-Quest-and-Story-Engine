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

import de.markusbordihn.dialogqueststoryengine.logic.action.types.SetFactAction;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.HasItemCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.PermissionLevelCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.QuestStateCondition;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;
import net.minecraft.gametest.framework.GameTestHelper;

public class ServerRegistrationGameTestHelper {

  private ServerRegistrationGameTestHelper() {}

  public static void testConditionsRegistryFrozenOnServer(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "CONDITIONS registry must be frozen after server start — BuiltinConditions.register() and"
            + " Registries.freezeAll() did not run",
        Registries.CONDITIONS.isFrozen());
  }

  public static void testBuiltinConditionTypesRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        FactEqualsCondition.TYPE_ID + " must be registered",
        Registries.CONDITIONS.contains(FactEqualsCondition.TYPE_ID));
    GameTestHelpers.assertTrue(
        helper,
        QuestStateCondition.TYPE_ID + " must be registered",
        Registries.CONDITIONS.contains(QuestStateCondition.TYPE_ID));
    GameTestHelpers.assertTrue(
        helper,
        HasItemCondition.TYPE_ID + " must be registered",
        Registries.CONDITIONS.contains(HasItemCondition.TYPE_ID));
    GameTestHelpers.assertTrue(
        helper,
        PermissionLevelCondition.TYPE_ID + " must be registered",
        Registries.CONDITIONS.contains(PermissionLevelCondition.TYPE_ID));
  }

  public static void testBuiltinActionTypesRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "ACTIONS registry must be frozen after server start",
        Registries.ACTIONS.isFrozen());
    GameTestHelpers.assertTrue(
        helper,
        SetFactAction.TYPE_ID + " must be registered",
        Registries.ACTIONS.contains(SetFactAction.TYPE_ID));
  }
}
