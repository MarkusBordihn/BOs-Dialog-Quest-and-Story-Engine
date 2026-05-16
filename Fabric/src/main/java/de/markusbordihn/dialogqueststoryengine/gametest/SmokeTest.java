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
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEntry;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionEventType;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionStore;
import de.markusbordihn.dialogqueststoryengine.data.interaction.InteractionType;
import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.UUID;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class SmokeTest {

  @GameTest(template = "dialog_quest_and_story_engine:gametest.3x3x3")
  public void testModRegistered(GameTestHelper helper) {
    GameTestHelpers.assertTrue(
        helper,
        "Mod " + Constants.MOD_ID + " is not available!",
        FabricLoader.getInstance().isModLoaded(Constants.MOD_ID));
    helper.succeed();
  }

  @GameTest(template = "dialog_quest_and_story_engine:gametest.3x3x3")
  public void testBlockUUIDDeterminism(GameTestHelper helper) {
    BlockPos pos = new BlockPos(100, 64, -200);
    ResourceKey<Level> dim = Level.OVERWORLD;
    UUID first = BlockUUID.fromBlockPos(dim, pos);
    UUID second = BlockUUID.fromBlockPos(dim, pos);
    GameTestHelpers.assertEquals(helper, "Block UUID should be deterministic", first, second);
  }

  @GameTest(template = "dialog_quest_and_story_engine:gametest.3x3x3")
  public void testInteractionDataSetRegisterAndLookup(GameTestHelper helper) {
    InteractionStore store = new InteractionStore();
    UUID targetId = UUID.randomUUID();
    InteractionEntry entry =
        InteractionEntry.forEntityInteract(
            targetId,
            InteractionType.RIGHT_CLICK,
            "test_label",
            ResourceLocation.tryParse("minecraft:overworld"));
    store.register(entry);
    GameTestHelpers.assertTrue(
        helper,
        "InteractionStore should contain the registered interaction",
        store.hasInteraction(targetId, InteractionEventType.ON_ENTITY_INTERACT));
    helper.succeed();
  }
}
