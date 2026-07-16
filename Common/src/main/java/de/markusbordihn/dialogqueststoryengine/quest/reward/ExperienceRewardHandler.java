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

package de.markusbordihn.dialogqueststoryengine.quest.reward;

import de.markusbordihn.dialogqueststoryengine.data.quest.QuestRewardClaimReason;
import de.markusbordihn.dialogqueststoryengine.data.quest.RewardDisplayEntry;
import de.markusbordihn.dialogqueststoryengine.data.quest.content.RewardEntry;
import de.markusbordihn.dialogqueststoryengine.registry.RewardHandler;
import java.util.Optional;

public final class ExperienceRewardHandler implements RewardHandler {

  @Override
  public Optional<QuestRewardClaimReason> preflight(RewardGrantContext context, RewardEntry entry) {
    return entry instanceof RewardEntry.Experience
        ? Optional.empty()
        : Optional.of(QuestRewardClaimReason.GRANT_REJECTED);
  }

  @Override
  public void grant(RewardGrantContext context, RewardEntry entry) {
    context.player().giveExperiencePoints(((RewardEntry.Experience) entry).amount());
  }

  @Override
  public RewardDisplayEntry displayEntry(RewardEntry entry) {
    return new RewardDisplayEntry(
        entry.type(), Optional.empty(), ((RewardEntry.Experience) entry).amount());
  }
}
