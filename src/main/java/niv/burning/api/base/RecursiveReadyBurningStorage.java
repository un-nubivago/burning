package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import niv.burning.api.BurningStorage;
import niv.burning.api.FuelVariant;

public class RecursiveReadyBurningStorage implements InsertionOnlyStorage<FuelVariant> {

    protected final Level level;

    protected final BlockPos pos;

    public RecursiveReadyBurningStorage(Level level, BlockPos pos) {
        this.level = level;
        this.pos = pos;
    }

    @Override
    public long insert(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        long inserted = 0L;
        for (var direction : shuffle(this.level.random, Direction.values())) {
            var storage = BurningStorage.SIDED.find(this.level, this.pos.relative(direction), direction.getOpposite());
            if (storage != null && storage.supportsInsertion() && inserted < maxAmount)
                inserted += storage.insert(resource, maxAmount - inserted, transaction);
        }
        return inserted;
    }

    protected final <T> T[] shuffle(RandomSource random, T[] array) {
        for (var i = array.length - 1; i > 0; i--) {
            var j = random.nextInt(i + 1);
            var temp = array[i];
            array[i] = array[j];
            array[j] = temp;
        }
        return array;
    }
}
