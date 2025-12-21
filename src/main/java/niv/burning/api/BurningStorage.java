package niv.burning.api;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import niv.burning.impl.BurningImpl;

/**
 * Represents a storage for {@link Burning} fuel values, supporting insertion,
 * extraction,
 * and querying of burning state. Implementations may represent block entities
 * or other
 * in-world objects that can store and transfer burning energy.
 *
 * @since 1.0
 */
public interface BurningStorage {

    /**
     * Sided block access to burning storages.
     * <p>
     * The {@code Direction} parameter may be null, meaning that the full storage
     * (ignoring side restrictions) should be queried.
     * Refer to {@link BlockApiLookup} for documentation on how to use this field.
     */
    BlockApiLookup<BurningStorage, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryBuild(BurningImpl.MOD_ID, "burning_storage"),
            BurningStorage.class, Direction.class);

    /**
     * An immutable instance that is always empty and does not support insertion or
     * extraction.
     */
    BurningStorage EMPTY = new BurningStorage() {
        @Override
        public boolean supportsInsertion() {
            return false;
        }

        @Override
        public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
            return burning.zero();
        }

        @Override
        public boolean supportsExtraction() {
            return false;
        }

        @Override
        public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
            return burning.zero();
        }

        @Override
        public Burning getBurning(BurningContext context) {
            return Burning.MIN_VALUE;
        }

        @Override
        public boolean isBurning() {
            return false;
        }
    };

    /**
     * Indicates whether this storage supports insertion of {@link Burning} values.
     * <p>
     * Returns false if calling {@link #insert} will always return a zeroed
     * {@link Burning},
     * true otherwise or in doubt.
     * <p>
     * Note: This function is meant to be used by pipes or other devices that can
     * transfer {@link Burning} to know if they should interact with this storage at
     * all.
     *
     * @return true if {@link #insert} can return something other than a zeroed
     *         {@link Burning}, false otherwise
     */
    default boolean supportsInsertion() {
        return true;
    }

    /**
     * Attempts to insert the provided {@link Burning} into this storage.
     *
     * @param burning     the {@link Burning} to insert
     * @param context     the {@link BurningContext} to use
     * @param transaction the transaction this operation is part of
     * @return an instance of {@link Burning} with the same fuel and less than or
     *         equal percentage
     *         than the one passed as argument: the amount that was inserted
     */
    Burning insert(Burning burning, BurningContext context, TransactionContext transaction);

    /**
     * Indicates whether this storage supports extraction of {@link Burning} values.
     * <p>
     * Returns false if calling {@link #extract} will always return a zeroed
     * {@link Burning},
     * true otherwise or in doubt.
     * <p>
     * Note: This function is meant to be used by pipes or other devices that can
     * transfer {@link Burning} to know if they should interact with this storage at
     * all.
     *
     * @return true if {@link #extract} can return something other than a zeroed
     *         {@link Burning}, false otherwise
     */
    default boolean supportsExtraction() {
        return true;
    }

    /**
     * Attempts to extract up to the provided {@link Burning} from this storage.
     *
     * @param burning     the {@link Burning} to extract
     * @param context     the {@link BurningContext} to use
     * @param transaction the transaction this operation is part of
     * @return an instance of {@link Burning} with the same fuel and less than or
     *         equal percentage
     *         than the one passed as argument: the amount that was extracted
     */
    Burning extract(Burning burning, BurningContext context, TransactionContext transaction);

    /**
     * Returns the currently contained {@link Burning} in this storage.
     *
     * @param context the {@link BurningContext} to use
     * @return the currently contained {@link Burning}
     */
    Burning getBurning(BurningContext context);

    /**
     * Indicates whether this storage is currently in a burning state.
     * <p>
     * This typically means whether the storage contains a non-zero {@link Burning}
     * value
     * that is presently active or being consumed/used for burning operations.
     * Implementations
     * should return true if they should be considered actively burning (e.g., a
     * generator
     * is currently producing energy from fuel), false otherwise.
     * </p>
     *
     * @return true if this storage is actively burning, false if not
     */
    boolean isBurning();
}
