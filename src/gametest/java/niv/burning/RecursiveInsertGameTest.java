package niv.burning;

import java.util.HashSet;
import java.util.Set;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import niv.burning.api.BurningStorage;
import niv.burning.api.BurningStorageUtil;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;
import niv.burning.api.base.RecursiveReadyBurningStorage;

public class RecursiveInsertGameTest {

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void onCopperCube(GameTestHelper game) {
        var set = setup(game);

        var originPos = game.absolutePos(new BlockPos(4, 4, 4));
        var origin = BurningStorage.SIDED.find(game.getLevel(), originPos, null);

        var targetPos = set.stream()
                .filter(pos -> ((FurnaceStorage) BurningStorage.SIDED
                        .find(game.getLevel(), pos, null))
                        .getAmount() == 400L)
                .toList()
                .stream()
                .findFirst()
                .orElse(null);

        game.assertFalse(targetPos == null, literal("Target pos should not be null"));

        try (var transaction = Transaction.openOuter()) {
            game.assertTrue(1200L == BurningStorageUtil
                    .recursiveInsert(originPos, tx -> origin.insert(FuelVariant.COAL, 1600, tx), transaction),
                    literal("inserted amount"));
            transaction.commit();
        }

        for (var pos : set) {
            var storage = (FurnaceStorage) BurningStorage.SIDED.find(game.getLevel(), pos, null);
            if (pos.equals(targetPos)) {
                game.assertTrue(1600L == storage.getAmount(), literal("target storage amount"));
            } else {
                game.assertTrue(800L == storage.getAmount(), literal("storage amount"));
            }
        }

        game.succeed();
    }

    private Set<BlockPos> setup(GameTestHelper game) {
        BurningStorage.SIDED.registerForBlocks(
                (level, pos, state, entity, side) -> new RecursiveReadyBurningStorage(level, pos),
                Blocks.WAXED_COPPER_BLOCK);

        for (int i = 3; i <= 5; i++)
            for (int j = 3; j <= 5; j++)
                for (int k = 3; k <= 5; k++)
                    game.setBlock(i, j, k, Blocks.WAXED_COPPER_BLOCK);

        var blocks = new Block[] { Blocks.FURNACE, Blocks.SMOKER, Blocks.BLAST_FURNACE };
        var result = new HashSet<BlockPos>();

        var random = game.getLevel().getRandom();

        var base = 400L;

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
                game.assertBlock(pos, Blocks.AIR::equals,
                        literal("Block should be air, its " + game.getBlockState(pos).getBlock()));
                game.setBlock(pos, blocks[random.nextInt(3)]);

                try (var transaction = Transaction.openOuter()) {
                    var storage = BurningStorage.SIDED.find(game.getLevel(), game.absolutePos(pos), null);

                    game.assertFalse(storage == null,
                            literal("Just inserted storage not found"));

                    game.assertTrue(base == storage.insert(FuelVariant.COAL, base, transaction),
                            literal("inserted amount"));

                    base = 800L;

                    transaction.commit();
                }
            }
        }

        return result;
    }

    private static final String literal(String string) {
        return string;
    }
}
