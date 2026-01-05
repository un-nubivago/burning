package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.ResourceAmount;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;

public abstract class AbstractFurnaceStorage extends SnapshotParticipant<ResourceAmount<FuelVariant>> implements FurnaceStorage {

    protected abstract void setResource(FuelVariant resource);

    protected abstract void setAmount(long amount);

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
    public long getCapacity() {
        return getResource().getDuration();
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

    private static final long clamp(long amount, long min, long max) {
        if (amount < min)
            return min;
        else if (amount > max)
            return max;
        else
            return (int) amount;
    }
}
