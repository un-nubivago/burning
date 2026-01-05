package niv.burning.api.base;

import static java.lang.Math.clamp;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleVariantStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import niv.burning.api.FuelVariant;

/**
 * Provides a simple burning storage implementation that supports insertion and
 * extraction, enable snapshotting, and has easy load and save methods.
 * <p>
 * Example usage:
 *
 * <pre>
 * public class MyBlockEntity extends BlockEntity {
 *   // Add a SimpleBurningStorage in the block entity class
 *   public final SimpleBurningStorage burningStorage = new SimpleBurningStorage() {
 *      {@literal @}Override
 *      <p>
 *      protected void onFinalCommit() {
 *          setChanged();
 *      }
 *   };
 *
 *   // Use the storage internally, for example in tick()
 *   public void tick() {
 *     BurningContext context = BurningContext.worldlyContext(this.level);
 *     if (!this.level.isClientSide && this.burningStorage.isBurning()) {
 *       try (Transaction transaction = Transaction.openOuter()) {
 *          Burning extracted = this.burningStorage.extract(Burning.COAL.withValue(200, context), context, transaction);
 *          // do something with burning just extracted
 *          transaction.commit();
 *       }
 *     }
 *   }
 *
 *   // Don't forget to save/read the energy in the block entity NBT.
 *
 * }
 *
 * // Don't forget to register the burning storage. Make sure to call this after you create the block entity type.
 * BlockEntityType<MyBlockEntity> MY_BLOCK_ENTITY;
 * BurningStorage.SIDED.registerForBlockEntity((myBlockEntity, direction) -> myBlockEntity.burningStorage, MY_BLOCK_ENTITY);
 *
 * </pre>
 * <p>
 *
 * @since 1.0
 */
public class SimpleBurningStorage extends SingleVariantStorage<FuelVariant> {

    /**
     * Class constructor.
     */
    public SimpleBurningStorage() {
        this.variant = FuelVariant.BLANK;
        this.amount = 0L;
    }

    // SingleVariantStorage

    @Override
    protected FuelVariant getBlankVariant() {
        return FuelVariant.BLANK;
    }

    @Override
    protected long getCapacity(FuelVariant variant) {
        return variant.getDuration();
    }

    // SingleSlotStorage

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

        if (newAmount > oldCapacity) {
            this.variant = resource;
        }
        this.amount = newAmount;

        if (this.amount <= 0)
            this.variant = FuelVariant.BLANK;

        return (newAmount - oldAmount) * resource.getDuration() / newCapacity;
    }

    @Override
    public long extract(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var oldCapacity = getCapacity();
        var newCapacity = resource.getDuration();
        var oldAmount = getAmount();
        var newAmount = clamp(oldAmount - maxAmount, 0, Math.max(oldCapacity, newCapacity));
        if (newAmount >= oldAmount)
            return 0L;

        updateSnapshots(transaction);

        if (oldCapacity > newCapacity && newAmount <= newCapacity)
            this.variant = resource;
        this.amount = newAmount;

        if (this.amount <= 0)
            this.variant = FuelVariant.BLANK;

        return (oldAmount - newAmount) * resource.getDuration() / newCapacity;
    }
}
