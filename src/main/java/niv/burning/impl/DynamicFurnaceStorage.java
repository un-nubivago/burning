package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;
import niv.burning.api.base.BurningStorageBlockEntity;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

@Internal
public class DynamicFurnaceStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements FurnaceStorage {

    private final DynamicFurnaceStorageProvider provider;

    private final BlockEntity target;

    private Item fuel = Items.AIR;

    DynamicFurnaceStorage(DynamicFurnaceStorageProvider provider, BlockEntity target) {
        this.provider = provider;
        this.target = target;
    }

    // FurnaceStorage

    @Override
    public long insert(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var oldCapacity = getCapacity();
        var newCapacity = Burning.fuelValues().burnDuration(new ItemStack(resource.getFuel()));
        var oldAmount = getAmount();
        var newAmount = Math.clamp(oldAmount + maxAmount, 0, Math.max(oldCapacity, newCapacity));
        if (newAmount <= oldAmount)
            return 0L;

        updateSnapshots(transaction);

        if (newAmount > oldCapacity) {
            this.fuel = resource.getFuel();
            this.provider.litDuration.set(this.target, .0 + newCapacity);
        }
        this.provider.litTime.set(this.target, .0 + newAmount);

        if (this.provider.litTime.get(this.target) <= 0)
            this.fuel = Items.AIR;

        return newAmount - oldAmount;
    }

    @Override
    public FuelVariant getResource() {
        return FuelVariant.of(this.fuel);
    }

    @Override
    public long getAmount() {
        return this.provider.litTime.get(target).longValue();
    }

    @Override
    public long getCapacity() {
        return this.provider.litDuration.get(target).longValue();
    }

    // SnapshotParticipant

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(getResource(), getAmount());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.fuel = snapshot.resource().getFuel();
        this.provider.litDuration.set(target, .0 + snapshot.resource().getDuration());
        this.provider.litTime.set(target, .0 + snapshot.amount());
    }

    @Override
    protected void onFinalCommit() {
        if (this.target.hasLevel()) {
            BurningStorageBlockEntity.tryUpdateLitProperty(this.target, this.provider.litTime.get(this.target) > 0);
            this.target.setChanged();
        }
    }
}
