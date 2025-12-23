package niv.burning.api.base;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import niv.burning.api.BurningStorage;

/**
 * <b>Optional</b> helper class that can be implemented on block entities that
 * wish to provide a sided burning storage without having to register a provider
 * for each block entity type.
 * <p>
 * How it works is that this library registers fallback providers for instances
 * of this interface. This can be used for convenient storage registration, but
 * please always use the SIDED lookups for queries:
 *
 * <pre>{@code
 * BurningStorage maybeBurningStorage = BurningStorage.SIDED.find(level, pos, direction);
 * if (maybeBurningStorage != null) {
 *     // use it
 * }
 * }</pre>
 *
 * @since 2.0
 */
public interface BurningStorageBlockEntity {

    /**
     * Return a burning storage if available on the queried side, or null otherwise.
     *
     * @param side The side of the storage to query, {@code null} means that the
     *             full storage without the restriction should be returned instead.
     */
    default @Nullable BurningStorage getBurningStorage(@Nullable Direction direction) {
        return null;
    }

    /**
     * Tries to update the {@link BlockStateProperties#LIT LIT} property of the
     * given {@code entity} to match the {@link BurningStorage#isBurning() burning}
     * status of the given {@code storage}.
     *
     * @param entity  the block entity
     * @param storage the burning storage
     * @return true if the LIT property has changed; false otherwise
     */
    static boolean tryUpdateLitProperty(BlockEntity entity, BurningStorage storage) {
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
