package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.api.BurningStorageHelper;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

@Internal
public class AbstractFurnaceBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    private final AbstractFurnaceBlockEntity target;

    public AbstractFurnaceBurningStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    private Burning getZero() {
        var fuel = this.target.burning_getFuel();
        return fuel == null ? Burning.MIN_VALUE : Burning.ofZero(fuel);
    }

    private void setZero(Burning zero) {
        this.target.burning_setFuel(zero.getFuel());
    }

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(this.target, context);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(
                Math.max(this.target.litDuration, fuelTime) - this.target.litTime,
                burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.target.litTime += value;
        if ((this.target.litDuration > fuelTime && this.target.litTime <= fuelTime)
                || this.target.litTime > this.target.litDuration) {
            this.target.litDuration = fuelTime;
            setZero(burning);
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(this.target, context);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(this.target.litTime, burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.target.litTime -= value;
        if (this.target.litDuration > fuelTime && this.target.litTime <= fuelTime) {
            this.target.litDuration = fuelTime;
            this.setZero(burning);
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        context = new Context(this.target, context);
        return this.getZero().withValue(this.target.litTime, context);
    }

    @Override
    public boolean isBurning() {
        return this.target.litTime > 0;
    }

    @Override
    protected Snapshot createSnapshot() {
        return new Snapshot(
                this.target.litTime,
                this.target.litDuration,
                this.getZero());
    }

    @Override
    protected void readSnapshot(Snapshot snapshot) {
        this.target.litTime = snapshot.currentBurning();
        this.target.litDuration = snapshot.maxBurning();
        this.setZero(snapshot.zero());
    }

    @Override
    protected void onFinalCommit() {
        BurningStorageHelper.tryUpdateLitProperty(this.target, this);
        this.target.setChanged();
    }

    protected final class Context implements BurningContext {

        private final AbstractFurnaceBlockEntity target;

        private final BurningContext source;

        public Context(AbstractFurnaceBlockEntity target, BurningContext context) {
            this.target = target;
            this.source = context;
        }

        @Override
        public boolean isFuel(Item item) {
            return this.source.isFuel(new ItemStack(item));
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return this.source.isFuel(itemStack);
        }

        @Override
        public int burnDuration(Item item) {
            return this.target.getBurnDuration(new FuelValuesAdapter(this.source), new ItemStack(item));
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.target.getBurnDuration(new FuelValuesAdapter(this.source), itemStack);
        }
    }
}
