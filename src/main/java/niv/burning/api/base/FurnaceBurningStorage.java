package niv.burning.api.base;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

/**
 * Provides a burning storage implementation suited for block entities extending
 * vanilla's AbstractFurnaceBlockEntity and, thus, that already have an internal
 * fuel burning logic.
 * <p>
 * Note: When queried for burning storages, block entities extending
 * AbstractFurnaceBlockEntity return instances of this class unless registered
 * otherwise.
 *
 * @see {@link BurningStorageBlockEntity}
 * @since 2.0
 */
public class FurnaceBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    private final AbstractFurnaceBlockEntity target;

    public FurnaceBurningStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    private Burning getZero() {
        var fuel = this.target.getInternalBurningFuel();
        return fuel == null ? Burning.MIN_VALUE : Burning.ofZero(fuel);
    }

    private void setZero(Burning zero) {
        this.target.setInternalBurningFuel(zero.getFuel());
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
    public boolean supportsExtraction() {
        return false;
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        return burning.zero();
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
        BurningStorageBlockEntity.tryUpdateLitProperty(this.target, this);
        this.target.setChanged();
    }

    protected final class Context implements BurningContext {

        private final AbstractFurnaceBlockEntity target;

        private final FuelValues fuelValues;

        public Context(AbstractFurnaceBlockEntity target, BurningContext context) {
            this.target = target;
            this.fuelValues = new Adapter(context);
        }

        @Override
        public boolean isFuel(Item item) {
            return this.fuelValues.isFuel(new ItemStack(item));
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return this.fuelValues.isFuel(itemStack);
        }

        @Override
        public int burnDuration(Item item) {
            return this.target.getBurnDuration(this.fuelValues, new ItemStack(item));
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.target.getBurnDuration(this.fuelValues, itemStack);
        }
    }

    private final class Adapter extends FuelValues {

        private final BurningContext context;

        Adapter(BurningContext context) {
            super(null);
            this.context = context;
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return this.context.isFuel(itemStack);
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.context.burnDuration(itemStack);
        }
    }
}
