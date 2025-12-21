package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

/**
 * Provides a burning storage with an infinite reserve of "burning". Doesn't support insertion.
 * <p>
 * Useful for creative storages implementations.
 */
public class InfiniteBurningStorage implements BurningStorage {

    /**
     * Singleton and stateless instance.
     */
    public static final InfiniteBurningStorage INSTANCE = new InfiniteBurningStorage();

    @Override
    public boolean supportsInsertion() {
        return false;
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning.zero();
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning;
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return Burning.MAX_VALUE;
    }

    @Override
    public boolean isBurning() {
        return true;
    }
}
