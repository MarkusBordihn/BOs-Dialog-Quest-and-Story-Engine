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

package de.markusbordihn.dialogqueststoryengine.logic.condition;

import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactCompareCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactEqualsCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.FactExistsCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.HasItemCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.PermissionLevelCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.QuestStateCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.QuestStepStateCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.StoryReadCondition;
import de.markusbordihn.dialogqueststoryengine.logic.condition.types.StoryUnlockedCondition;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;

public final class BuiltinConditions {

  private BuiltinConditions() {}

  public static void register() {
    Registries.CONDITIONS.register(FactEqualsCondition.TYPE_ID, FactEqualsCondition::parse);
    Registries.CONDITIONS.register(FactExistsCondition.TYPE_ID, FactExistsCondition::parse);
    Registries.CONDITIONS.register(FactCompareCondition.TYPE_ID, FactCompareCondition::parse);
    Registries.CONDITIONS.register(QuestStateCondition.TYPE_ID, QuestStateCondition::parse);
    Registries.CONDITIONS.register(QuestStepStateCondition.TYPE_ID, QuestStepStateCondition::parse);
    Registries.CONDITIONS.register(StoryUnlockedCondition.TYPE_ID, StoryUnlockedCondition::parse);
    Registries.CONDITIONS.register(StoryReadCondition.TYPE_ID, StoryReadCondition::parse);
    Registries.CONDITIONS.register(HasItemCondition.TYPE_ID, HasItemCondition::parse);
    Registries.CONDITIONS.register(
        PermissionLevelCondition.TYPE_ID, PermissionLevelCondition::parse);
  }
}
