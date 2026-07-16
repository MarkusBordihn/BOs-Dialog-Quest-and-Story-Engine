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

package de.markusbordihn.dialogqueststoryengine.core;

import de.markusbordihn.dialogqueststoryengine.interaction.InteractionRegistry;
import de.markusbordihn.dialogqueststoryengine.logic.action.BuiltinActions;
import de.markusbordihn.dialogqueststoryengine.logic.condition.BuiltinConditions;
import de.markusbordihn.dialogqueststoryengine.logic.context.BuiltinContextValueProviders;
import de.markusbordihn.dialogqueststoryengine.quest.reward.BuiltinRewardHandlers;
import de.markusbordihn.dialogqueststoryengine.quest.step.BuiltinQuestSteps;
import de.markusbordihn.dialogqueststoryengine.state.QuestProgressSync;
import de.markusbordihn.dialogqueststoryengine.validation.BuiltinValidators;

public final class DqseBootstrap {

  private static boolean initialized;

  private DqseBootstrap() {}

  public static synchronized void initialize() {
    if (initialized) {
      return;
    }

    BuiltinActions.register();
    BuiltinConditions.register();
    BuiltinContextValueProviders.register();
    BuiltinQuestSteps.register();
    BuiltinRewardHandlers.register();
    InteractionRegistry.registerBuiltIns();
    BuiltinValidators.register();
    QuestProgressSync.register();
    initialized = true;
  }
}
