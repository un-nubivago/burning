package niv.burning;

import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.fabricmc.fabric.api.transfer.v1.item.ItemStorage;
import net.fabricmc.fabric.api.transfer.v1.item.ItemVariant;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

public class VanillaBurningStorageGameTest {

    private static final String SHOULD_BE_BURNING = "BurningStorage should be burning";

    private static final String SHOULD_NOT_BE_BURNING = "BurningStorage shouldn't be burning";

    private static final BlockPos POS = new BlockPos(4, 4, 4);

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void onFurnace(GameTestHelper game) {
        game.setBlock(POS, Blocks.FURNACE);
        runCommonSequence(game, Items.COBBLESTONE, 1);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void onBlastFurnace(GameTestHelper game) {
        game.setBlock(POS, Blocks.BLAST_FURNACE);
        runCommonSequence(game, Items.RAW_IRON, 2);
    }

    @GameTest(template = FabricGameTest.EMPTY_STRUCTURE)
    public void onSmoker(GameTestHelper game) {
        game.setBlock(POS, Blocks.SMOKER);
        runCommonSequence(game, Items.PORKCHOP, 2);
    }

    private void runCommonSequence(GameTestHelper game, Item material, int speed) {

        var context = BurningContext.worldlyContext(game.getLevel());
        var coal = Burning.COAL.one().getValue(context).intValue();
        var lavalBucket = Burning.LAVA_BUCKET.one().getValue(context).intValue();

        var storage = BurningStorage.SIDED.find(game.getLevel(), game.absolutePos(POS), null);
        game.assertFalse(storage == null, "BurningStorage found null");

        game.runAtTickTime(1, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);
            game.assertFalse(storage.isBurning(), SHOULD_NOT_BE_BURNING);
            game.assertTrue(.0 == storage.getBurning(context).getPercent(),
                    "Expected " + .0 + " Burning, got " + storage.getBurning(context).getPercent() + " instead");
        });

        game.runAtTickTime(2, () -> {
            try (var transaction = Transaction.openOuter()) {
                ItemStorage.SIDED.find(game.getLevel(), game.absolutePos(POS), Direction.SOUTH)
                        .insert(ItemVariant.of(Items.COAL), 1, transaction);
                transaction.commit();
            }
        });

        game.runAtTickTime(3, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);
            game.assertFalse(storage.isBurning(), SHOULD_NOT_BE_BURNING);
            game.assertTrue(.0 == storage.getBurning(context).getPercent(),
                    "Expected " + .0 + " Burning, got " + storage.getBurning(context).getPercent() + " instead");
        });

        game.runAtTickTime(4, () -> {
            try (var transaction = Transaction.openOuter()) {
                ItemStorage.SIDED.find(game.getLevel(), game.absolutePos(POS), Direction.UP)
                        .insert(ItemVariant.of(material), 1, transaction);
                transaction.commit();
            }
        });

        game.runAtTickTime(5, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);
            game.assertTrue(storage.isBurning(), SHOULD_BE_BURNING);
            game.assertTrue(Burning.COAL.one().equals(storage.getBurning(context)),
                    "Expected " + Burning.COAL.one() + " Burning, got " + storage.getBurning(context) + " instead");
        });

        game.runAtTickTime(6, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);
            game.assertTrue(storage.isBurning(), SHOULD_BE_BURNING);
            game.assertTrue(Burning.COAL.withValue(coal - speed, context).equals(storage.getBurning(context)),
                    "Expected " + Burning.COAL.withValue(coal - speed, context)
                            + " Burning, got " + storage.getBurning(context) + " instead");
        });

        game.runAtTickTime(7, () -> {
            try (var transaction = Transaction.openOuter()) {
                storage.insert(Burning.LAVA_BUCKET.one(), context, transaction);
                transaction.commit();
            }
        });

        game.runAtTickTime(8, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);
            game.assertTrue(storage.isBurning(), SHOULD_BE_BURNING);
            game.assertTrue(Burning.LAVA_BUCKET.withValue(lavalBucket - speed, context).equals(storage.getBurning(context)),
                    "Expected " + Burning.LAVA_BUCKET.withValue(lavalBucket - speed, context)
                            + " Burning, got " + storage.getBurning(context) + " instead");
        });

        game.startSequence()
                .thenIdle(10)
                .thenSucceed();
    }
}
