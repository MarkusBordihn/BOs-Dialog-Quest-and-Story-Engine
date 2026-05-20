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

import de.markusbordihn.dialogqueststoryengine.utils.BlockUUID;
import java.util.UUID;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

@SuppressWarnings("unused")
public class BlockUuidGameTest {

  @GameTest(template = "dialog_quest_and_story_engine:gametest.3x3x3")
  public void testDeterminism(GameTestHelper helper) {
    BlockPos pos = new BlockPos(100, 64, -200);
    ResourceKey<Level> dim = Level.OVERWORLD;
    UUID first = BlockUUID.fromBlockPos(dim, pos);
    UUID second = BlockUUID.fromBlockPos(dim, pos);
    GameTestHelpers.assertEquals(helper, "Block UUID should be deterministic", first, second);
    helper.succeed();
  }

  @GameTest(template = "dialog_quest_and_story_engine:gametest.3x3x3")
  public void testUniquenessAcrossPositions(GameTestHelper helper) {
    ResourceKey<Level> dim = Level.OVERWORLD;
    UUID uuidA = BlockUUID.fromBlockPos(dim, new BlockPos(0, 64, 0));
    UUID uuidB = BlockUUID.fromBlockPos(dim, new BlockPos(1, 64, 0));
    GameTestHelpers.assertTrue(
        helper, "Different positions should produce different UUIDs", !uuidA.equals(uuidB));
    helper.succeed();
  }

  @GameTest(template = "dialog_quest_and_story_engine:gametest.3x3x3")
  public void testUniquenessAcrossDimensions(GameTestHelper helper) {
    BlockPos pos = new BlockPos(0, 64, 0);
    UUID overworld = BlockUUID.fromBlockPos(Level.OVERWORLD, pos);
    UUID nether = BlockUUID.fromBlockPos(Level.NETHER, pos);
    GameTestHelpers.assertTrue(
        helper,
        "Same position in different dimensions should produce different UUIDs",
        !overworld.equals(nether));
    helper.succeed();
  }
}
