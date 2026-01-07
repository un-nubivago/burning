package niv.burning.api;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import niv.burning.api.base.BurningStorageBlockEntity;
import niv.burning.api.base.SimpleBurningStorage;

/**
 * Access to {@link Storage Storage&lt;FuelVariant&gt;} instances.
 */
public final class BurningStorage {

    /**
     * Sided block access to fuel variant storages.
     * The {@code Direction} parameter may be null, meaning that the full inventory
     * (ignoring side restrictions) should be queried.
     * Refer to {@link BlockApiLookup} for documentation on how to use this field.
     *
     * <p>
     * When the operations supported by a storage change,
     * that is if the return value of {@link Storage#supportsInsertion} or
     * {@link Storage#supportsExtraction} changes,
     * the storage should notify its neighbors with a block update so that they can
     * refresh their connections if necessary.
     *
     * <p>
     * Block entities directly implementing {@link BurningStorageBlockEntity} are
     * automatically handled by a fallback provider, and don't need to do anything.
     *
     * @see {@link SimpleBurningStorage}
     */
    public static final BlockApiLookup<Storage<FuelVariant>, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryParse("burning:sided_storage"),
            Storage.asClass(), Direction.class);

    static {
        /*
         * Register fallback for block entities implementing the
         * BurningStorageBlockEntity interface.
         *
         * Note: AbstractFurnaceBlockEntity implements BurningStorageBlockEntity.
         */
        SIDED.registerFallback((world, pos, state, entity, side) -> {
            if (entity instanceof BurningStorageBlockEntity getter) {
                return getter.getBurningStorage(side);
            } else {
                return null;
            }
        });
    }

    private BurningStorage() {
    }
}
