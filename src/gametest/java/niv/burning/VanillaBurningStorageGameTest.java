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
import niv.burning.api.BurningStorage;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;

public class VanillaBurningStorageGameTest {

    private static final String STRING_BURNING = "Burning";
    private static final String STRING_SHOULD_BE_BURNING = "BurningStorage should be burning";
    private static final String STRING_SHOULD_NOT_BE_BURNING = "BurningStorage should not be burning";

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

        var storage = (FurnaceStorage) BurningStorage.SIDED.find(game.getLevel(), game.absolutePos(POS), null);
        game.assertFalse(storage == null, literal("BurningStorage not found, it should have"));

        game.runAtTickTime(1, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.FALSE);
            game.assertTrue(storage.isResourceBlank(), literal(STRING_SHOULD_NOT_BE_BURNING));
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
            game.assertTrue(storage.isResourceBlank(), literal(STRING_SHOULD_NOT_BE_BURNING));
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
            game.assertFalse(storage.isResourceBlank(), literal(STRING_SHOULD_BE_BURNING));
            game.assertTrue(storage.getAmount() == 1L * 1600 * speed, literal(STRING_BURNING));
        });

        game.runAtTickTime(6, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);
            game.assertFalse(storage.isResourceBlank(), literal(STRING_SHOULD_BE_BURNING));
            game.assertTrue(storage.getAmount() == 1L * (1600 - 1) * speed, literal(STRING_BURNING));
        });

        game.runAtTickTime(7, () -> {
            try (var transaction = Transaction.openOuter()) {
                storage.insert(FuelVariant.LAVA_BUCKET, 1L * 20000 * speed, transaction);
                transaction.commit();
            }
        });

        game.runAtTickTime(8, () -> {
            game.assertBlockProperty(POS, BlockStateProperties.LIT, Boolean.TRUE);
            game.assertFalse(storage.isResourceBlank(), literal(STRING_SHOULD_BE_BURNING));
            game.assertTrue(storage.getAmount() == 1L * (20000 - 1) * speed, literal(STRING_BURNING));
        });

        game.startSequence()
                .thenIdle(10)
                .thenSucceed();

        game.succeed();
    }

    private static final String literal(String string) {
        return string;
    }
}
