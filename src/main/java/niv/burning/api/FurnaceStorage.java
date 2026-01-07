package niv.burning.api;

import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.jetbrains.annotations.UnmodifiableView;

import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.impl.DefaultFurnaceStorage;

public interface FurnaceStorage extends SingleSlotStorage<FuelVariant>, InsertionOnlyStorage<FuelVariant> {

    static FurnaceStorage of(AbstractFurnaceBlockEntity entity) {
        return DefaultFurnaceStorage.of(entity);
    }

    @Override
    default boolean supportsExtraction() {
        return false;
    }

    @Override
    default long extract(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        return 0L;
    }

    @Override
    default boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    default @UnmodifiableView List<SingleSlotStorage<FuelVariant>> getSlots() {
        return List.of((SingleSlotStorage<FuelVariant>) this);
    }

    @Override
    default Iterator<StorageView<FuelVariant>> iterator() {
        return Collections.emptyIterator();
    }
}
