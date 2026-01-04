package niv.burning.impl;

import org.spongepowered.include.com.google.common.base.Preconditions;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;
import niv.burning.api.base.BurningStorageBlockEntity;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

public class DefaultFurnaceStorage extends SnapshotParticipant<SimpleBurningStorage.Snapshot> implements FurnaceStorage {

    private final AbstractFurnaceBlockEntity target;

    public DefaultFurnaceStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    // Furnace Storage

    @Override
    public long insert(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var oldCapacity = getCapacity();
        var newCapacity = this.target.getBurnDuration(
                Burning.fuelValues(),
                new ItemStack(resource.getFuel()));
        var oldAmount = getAmount();
        var newAmount = Math.clamp(oldAmount + (maxAmount * newCapacity / resource.getDuration()),
                0, Math.max(oldCapacity, newCapacity));
        if (newAmount <= oldAmount)
            return 0L;

        updateSnapshots(transaction);

        if (newAmount > oldCapacity) {
            this.target.setInternalBurningFuel(resource.getFuel());
            this.target.litTotalTime = newCapacity;
        }
        this.target.litTimeRemaining = Math.clamp(newAmount, 0, Integer.MAX_VALUE);

        if (this.target.litTimeRemaining <= 0)
            this.target.setInternalBurningFuel(Items.AIR);

        return (newAmount - oldAmount) * resource.getDuration() / newCapacity;
    }

    @Override
    public FuelVariant getResource() {
        return FuelVariant.of(this.target.getInternalBurningFuel());
    }

    @Override
    public long getAmount() {
        return this.target.litTimeRemaining;
    }

    @Override
    public long getCapacity() {
        return this.target.litTotalTime;
    }

    // SnapshotParticipant

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(getResource(), getAmount());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.setInternalBurningFuel(snapshot.resource().getFuel());
        this.target.litTotalTime = snapshot.resource().getDuration();
        this.target.litTimeRemaining = Math.clamp(snapshot.amount(), 0, this.target.litTotalTime);
    }

    @Override
    protected void onFinalCommit() {
        if (this.target.hasLevel()) {
            BurningStorageBlockEntity.tryUpdateLitProperty(target, this.target.litTimeRemaining > 0);
            this.target.setChanged();
        }
    }

    // static

    public static final FurnaceStorage of(AbstractFurnaceBlockEntity entity) {
        Preconditions.checkNotNull(entity, "Entity may not be null.");
        return new DefaultFurnaceStorage(entity);
    }
}
