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

package de.markusbordihn.dialogqueststoryengine.quest.step;

import de.markusbordihn.dialogqueststoryengine.data.quest.StepProgress;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RawQuestStep;
import de.markusbordihn.dialogqueststoryengine.logic.action.ActionContext;
import de.markusbordihn.dialogqueststoryengine.quest.runtime.QuestService;
import de.markusbordihn.dialogqueststoryengine.state.PlayerState;
import java.util.Optional;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

public record QuestStepContext(
    Player player,
    PlayerState playerState,
    ResourceLocation questId,
    RawQuestStep step,
    StepProgress stepProgress) {

  private static final String EVENT_ID = "quest-step-tracker";

  public void progress(int amount) {
    if (this.player instanceof ServerPlayer serverPlayer) {
      QuestService.progressStep(actionContext(serverPlayer), this.questId, this.step.id(), amount);
    } else {
      QuestService.progressStep(
          this.playerState.playerUuid(), this.questId, this.step.id(), amount);
    }
  }

  public void setProgress(int value) {
    if (this.player instanceof ServerPlayer serverPlayer) {
      QuestService.setStepProgress(
          actionContext(serverPlayer), this.questId, this.step.id(), value);
    } else {
      QuestService.setStepProgress(
          this.playerState.playerUuid(), this.questId, this.step.id(), value);
    }
  }

  private ActionContext actionContext(ServerPlayer serverPlayer) {
    return new ActionContext(
        serverPlayer, this.playerState, serverPlayer.server, EVENT_ID, Optional.empty());
  }
}
