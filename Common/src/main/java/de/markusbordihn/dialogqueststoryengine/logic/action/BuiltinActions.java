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

package de.markusbordihn.dialogqueststoryengine.logic.action;

import de.markusbordihn.dialogqueststoryengine.logic.action.types.AdvanceQuestStepAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.CloseSessionAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.CompleteQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.FailQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveExperienceAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.GiveItemAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.MarkStoryReadAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenDialogAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.OpenStoryAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RemoveFactAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunCommandAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.RunFunctionAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.SetFactAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.StartQuestAction;
import de.markusbordihn.dialogqueststoryengine.logic.action.types.UnlockStoryAction;
import de.markusbordihn.dialogqueststoryengine.registry.Registries;

public final class BuiltinActions {

  private BuiltinActions() {}

  public static void register() {
    Registries.ACTIONS.register(SetFactAction.TYPE_ID, SetFactAction::parse);
    Registries.ACTIONS.register(RemoveFactAction.TYPE_ID, RemoveFactAction::parse);
    Registries.ACTIONS.register(StartQuestAction.TYPE_ID, StartQuestAction::parse);
    Registries.ACTIONS.register(CompleteQuestAction.TYPE_ID, CompleteQuestAction::parse);
    Registries.ACTIONS.register(FailQuestAction.TYPE_ID, FailQuestAction::parse);
    Registries.ACTIONS.register(AdvanceQuestStepAction.TYPE_ID, AdvanceQuestStepAction::parse);
    Registries.ACTIONS.register(UnlockStoryAction.TYPE_ID, UnlockStoryAction::parse);
    Registries.ACTIONS.register(MarkStoryReadAction.TYPE_ID, MarkStoryReadAction::parse);
    Registries.ACTIONS.register(OpenStoryAction.TYPE_ID, OpenStoryAction::parse);
    Registries.ACTIONS.register(OpenDialogAction.TYPE_ID, OpenDialogAction::parse);
    Registries.ACTIONS.register(GiveItemAction.TYPE_ID, GiveItemAction::parse);
    Registries.ACTIONS.register(GiveExperienceAction.TYPE_ID, GiveExperienceAction::parse);
    Registries.ACTIONS.register(RunFunctionAction.TYPE_ID, RunFunctionAction::parse);
    Registries.ACTIONS.register(RunCommandAction.TYPE_ID, RunCommandAction::parse);
    Registries.ACTIONS.register(CloseSessionAction.TYPE_ID, CloseSessionAction::parse);
  }
}
