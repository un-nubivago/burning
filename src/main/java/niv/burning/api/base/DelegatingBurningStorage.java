package niv.burning.api.base;

import java.util.Objects;
import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

/**
 * Provides a burning storage that forwards every operation to another
 * burning storage, with an optional boolean supplier to check that the backing
 * storage is still valid.
 * <p>
 * This can be used for easier item energy storage implementation, or overridden
 * for custom delegation logic.
 *
 * @since 2.0
 */
public class DelegatingBurningStorage implements BurningStorage {

    protected final Supplier<? extends BurningStorage> backingStorage;

    protected final BooleanSupplier validPredicate;

    /**
     * Class constructor using an already istanciated burning storage.
     *
     * @param backingStorage a non-null instance
     * @param validPredicate a nullable boolean predicate
     */
    public DelegatingBurningStorage(
            BurningStorage backingStorage,
            @Nullable BooleanSupplier validPredicate) {
        this(() -> backingStorage, validPredicate);
        Objects.requireNonNull(backingStorage);
    }

    /**
     * Class constructor using a supplier for the backing storage.
     *
     * @param backingStorageSupplier a non-null supplier that should never return
     *                               null
     * @param validPredicate         a nullable boolean predicate
     */
    public DelegatingBurningStorage(
            Supplier<? extends BurningStorage> backingStorageSupplier,
            @Nullable BooleanSupplier validPredicate) {
        this.backingStorage = Objects.requireNonNull(backingStorageSupplier);
        this.validPredicate = validPredicate == null ? () -> true : validPredicate;
    }

    @Override
    public boolean supportsInsertion() {
        return this.validPredicate.getAsBoolean() && this.backingStorage.get().supportsInsertion();
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        return this.validPredicate.getAsBoolean()
                ? this.backingStorage.get().insert(burning, context, transaction)
                : burning.zero();
    }

    @Override
    public boolean supportsExtraction() {
        return this.validPredicate.getAsBoolean() && this.backingStorage.get().supportsExtraction();
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return this.validPredicate.getAsBoolean()
                ? this.backingStorage.get().extract(burning, context, transaction)
                : burning.zero();
    }

    @Override
    public Burning getBurning(BurningContext context) {
        return this.validPredicate.getAsBoolean() ? this.backingStorage.get().getBurning(context) : Burning.MIN_VALUE;
    }

    @Override
    public boolean isBurning() {
        return this.validPredicate.getAsBoolean() && this.backingStorage.get().isBurning();
    }
}
