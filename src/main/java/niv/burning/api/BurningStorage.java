package niv.burning.api;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import niv.burning.api.base.BurningStorageBlockEntity;

public final class BurningStorage {

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
