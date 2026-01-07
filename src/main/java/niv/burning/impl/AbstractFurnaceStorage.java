package niv.burning.impl;

import static java.lang.Math.clamp;

import java.util.Collections;
import java.util.Iterator;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.StorageView;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.FuelVariant;
import niv.burning.api.base.BurningStorageBlockEntity;

abstract class AbstractFurnaceStorage<T extends BlockEntity>
        extends SnapshotParticipant<ResourceAmount<FuelVariant>>
        implements SingleSlotStorage<FuelVariant>, InsertionOnlyStorage<FuelVariant> {

    protected final T target;

    AbstractFurnaceStorage(T target) {
        this.target = target;
    }

    protected abstract void setResource(FuelVariant resource);

    protected abstract void setAmount(long amount);

    @Override
    public long getCapacity() {
        return getResource().getDuration();
    }

    @Override
    public boolean isResourceBlank() {
        return getResource().isBlank();
    }

    @Override
    public boolean supportsInsertion() {
        return true;
    }

    @Override
    public long insert(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var oldCapacity = getCapacity();
        var newCapacity = resource.getDuration();
        var oldAmount = getAmount();
        var newAmount = clamp(oldAmount + maxAmount, 0, Math.max(oldCapacity, newCapacity));
        if (newAmount <= oldAmount)
            return 0L;

        updateSnapshots(transaction);

        if (newAmount > oldCapacity)
            setResource(resource);
        setAmount(newAmount);

        if (getAmount() <= 0)
            setResource(FuelVariant.BLANK);

        return newAmount - oldAmount;
    }

    @Override
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public long extract(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        return 0L;
    }

    @Override
    public Iterator<StorageView<FuelVariant>> iterator() {
        return Collections.emptyIterator();
    }

    @Override
    protected ResourceAmount<FuelVariant> createSnapshot() {
        return new ResourceAmount<>(getResource(), getAmount());
    }

    @Override
    protected void readSnapshot(ResourceAmount<FuelVariant> snapshot) {
        setResource(snapshot.resource());
        setAmount(snapshot.amount());
    }

    @Override
    protected void onFinalCommit() {
        if (this.target.hasLevel()) {
            BurningStorageBlockEntity.tryUpdateLitProperty(target, getAmount() > 0);
            this.target.setChanged();
        }
    }
}
