package niv.burning.api;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.include.com.google.common.base.Objects;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * Provides some utility methods for managing burning storages and what orbit
 * them.
 */
public final class BurningStorageUtil {

    private BurningStorageUtil() {
    }

    /**
     * Transfers {@link Burning} between two burning storages, and returns the
     * amount that was successfully transferred.
     *
     * @param from        the source storage (may be null)
     * @param to          the target storage (may be null)
     * @param burning     the maximum burning that may be moved
     * @param context     the {@link BurningContext} to use
     * @param transaction the transaction this transfer is part of,
     *                    or {@code null} if a transaction should be opened just for
     *                    this transfer
     * @return the amount of {@link Burning} that was successfully transferred
     * @since 1.0
     */
    public static Burning transfer(
            @Nullable BurningStorage from, @Nullable BurningStorage to,
            Burning burning, BurningContext context, @Nullable TransactionContext transaction) {
        if (from != null && to != null) {
            Burning extracted;
            try (var test = Transaction.openNested(transaction)) {
                extracted = from.extract(burning, context, test);
            }
            try (var actual = Transaction.openNested(transaction)) {
                var inserted = to.insert(extracted, context, actual);
                if (Objects.equal(inserted, from.extract(inserted, context, actual))) {
                    actual.commit();
                    return inserted;
                }
            }
        }
        return burning.zero();
    }

    /**
     * Tries to update the {@link BlockStateProperties#LIT LIT} property of the
     * given <code>entity</entity> depending on whether the entity has it in the
     * first place and the burning storage {@link BurningStorage#isBurning()
     * isBurning} method is returning something different than what the LIT property
     * indicates.
     *
     * @param entity  a non-null block entity
     * @param storage a non-null burning storage related with the entity
     * @return true if the LIT property gets updated; false otherwise
     *
     * @since 2.0
     */
    public static final boolean tryUpdateLitProperty(BlockEntity entity, BurningStorage storage) {
        var level = entity.level;
        var pos = entity.worldPosition;
        if (level == null || pos == null)
            return false;

        var state = level.getBlockState(pos);
        var wasBurning = state.getOptionalValue(BlockStateProperties.LIT).orElse(null);
        if (wasBurning == null)
            return false;

        var isBurning = storage.isBurning();
        if (wasBurning.equals(isBurning))
            return false;

        state = state.setValue(BlockStateProperties.LIT, isBurning);
        level.setBlockAndUpdate(pos, state);
        return true;
    }
}
