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
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemStack;

public final class ItemRewardHandler implements RewardHandler {

  private static ItemStack stackFor(RewardEntry.Item item) {
    return new ItemStack(BuiltInRegistries.ITEM.get(item.item()), item.count());
  }

  @Override
  public Optional<QuestRewardClaimReason> preflight(RewardGrantContext context, RewardEntry entry) {
    if (!(entry instanceof RewardEntry.Item item)) {
      return Optional.of(QuestRewardClaimReason.GRANT_REJECTED);
    }

    return context.simulateInsert(stackFor(item))
        ? Optional.empty()
        : Optional.of(QuestRewardClaimReason.INVENTORY_FULL);
  }

  @Override
  public void grant(RewardGrantContext context, RewardEntry entry) {
    if (entry instanceof RewardEntry.Item item) {
      ItemStack stack = stackFor(item);
      if (!context.player().getInventory().add(stack) || !stack.isEmpty()) {
        context.player().drop(stack, false);
      }
    }
  }

  @Override
  public RewardDisplayEntry displayEntry(RewardEntry entry) {
    RewardEntry.Item item = (RewardEntry.Item) entry;
    return new RewardDisplayEntry(entry.type(), Optional.of(item.item()), item.count());
  }
}
