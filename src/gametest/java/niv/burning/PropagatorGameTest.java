package niv.burning;

import static net.minecraft.network.chat.Component.literal;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import niv.burning.api.BurningPropagator;

public class PropagatorGameTest {

    @GameTest(maxTicks = 5)
    public void onCopperCube(GameTestHelper game) {
        var matches = setup(game);
        var found = new HashSet<BlockPos>();

        BurningPropagator.searchBurningStorages(
                game.getLevel(),
                game.absolutePos(new BlockPos(4, 4, 4)),
                (pos, storage) -> {
                    game.assertFalse(storage == null, literal("Found storage should not be null"));
                    game.assertTrue(found.add(pos), literal("The same position shouldn't be found more than once"));
                    return false;
                });

        game.assertValueEqual(matches, found, literal("found positions"));

        game.succeed();
    }

    private Set<BlockPos> setup(GameTestHelper game) {
        BurningPropagator.SIDED.registerForBlocks(
                (level, pos, state, entity, side) -> (lvl, ps) -> Set.of(Direction.values()),
                Blocks.WAXED_COPPER_BLOCK);

        for (int i = 3; i <= 5; i++)
            for (int j = 3; j <= 5; j++)
                for (int k = 3; k <= 5; k++)
                    game.setBlock(i, j, k, Blocks.WAXED_COPPER_BLOCK);

        var blocks = new Block[] { Blocks.FURNACE, Blocks.SMOKER, Blocks.BLAST_FURNACE };
        var result = new HashSet<BlockPos>();

        var random = game.getLevel().getRandom();

        while (result.size() < 10) {
            var xyz = new int[3];
            xyz[0] = 2 + (random.nextInt(1) * 4);
            xyz[1] = random.nextIntBetweenInclusive(3, 5);
            xyz[2] = random.nextIntBetweenInclusive(3, 5);
            for (int j = 2; j > 0; j--) {
                int k = random.nextInt(j + 1);
                int x = xyz[k];
                xyz[k] = xyz[j];
                xyz[j] = x;
            }
            var pos = new BlockPos(xyz[0], xyz[1], xyz[2]);
            if (result.add(game.absolutePos(pos))) {
                game.assertBlock(pos, Blocks.AIR::equals, block -> literal("Block should be air, its " + game.getBlockState(pos).getBlock()));
                game.setBlock(pos, blocks[random.nextInt(3)]);
            }
        }

        return result;
    }
}
