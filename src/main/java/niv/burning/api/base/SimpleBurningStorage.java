package niv.burning.api.base;

import java.util.function.IntUnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.storage.StoragePreconditions;
import net.fabricmc.fabric.api.transfer.v1.storage.base.SingleSlotStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
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
public class SimpleBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements SingleSlotStorage<FuelVariant> {

    public static final record Snapshot(FuelVariant resource, long amount) {
    }

    public static final Codec<Snapshot> SNAPSHOT_CODEC = Codec
            .lazyInitialized(() -> RecordCodecBuilder.create(instance -> instance.group(
                    FuelVariant.CODEC.fieldOf("resource").forGetter(Snapshot::resource),
                    Codec.LONG.fieldOf("amount").forGetter(Snapshot::amount))
                    .apply(instance, Snapshot::new)));

    protected final IntUnaryOperator operator;

    protected FuelVariant resource;

    protected long amount;

    /**
     * Class constructor.
     */
    public SimpleBurningStorage() {
        this(null);
    }

    /**
     * Class constructor with custom burn duration operator.
     *
     * @param operator operator to apply to every obtained burn duration
     */
    public SimpleBurningStorage(@Nullable IntUnaryOperator operator) {
        this.operator = operator == null ? IntUnaryOperator.identity() : operator;
        this.resource = FuelVariant.BLANK;
        this.amount = 0L;
    }

    // SingleSlotStorage

    @Override
    public long insert(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var oldCapacity = getCapacity();
        var newCapacity = this.operator.applyAsInt(resource.getDuration());
        var oldAmount = getAmount();
        var newAmount = Math.clamp(oldAmount + (maxAmount * newCapacity / resource.getDuration()),
                0, Math.max(oldCapacity, newCapacity));
        if (newAmount <= oldAmount)
            return 0L;

        updateSnapshots(transaction);

        if (newAmount > oldCapacity) {
            this.resource = resource;
        }
        this.amount = newAmount;

        if (this.amount <= 0)
            this.resource = FuelVariant.BLANK;

        return (newAmount - oldAmount) * resource.getDuration() / newCapacity;
    }

    @Override
    public long extract(FuelVariant resource, long maxAmount, TransactionContext transaction) {
        StoragePreconditions.notBlankNotNegative(resource, maxAmount);

        var oldCapacity = getCapacity();
        var newCapacity = this.operator.applyAsInt(resource.getDuration());
        var oldAmount = getAmount();
        var newAmount = Math.clamp(oldAmount - (maxAmount * newCapacity / resource.getDuration()),
                0, Math.max(oldCapacity, newCapacity));
        if (newAmount >= oldAmount)
            return 0L;

        updateSnapshots(transaction);

        if (oldCapacity > newCapacity && newAmount <= newCapacity)
            this.resource = resource;
        this.amount = newAmount;

        if (this.amount <= 0)
            this.resource = FuelVariant.BLANK;

        return (oldAmount - newAmount) * resource.getDuration() / newCapacity;
    }

    @Override
    public boolean isResourceBlank() {
        return this.resource.isBlank();
    }

    @Override
    public FuelVariant getResource() {
        return this.resource;
    }

    @Override
    public long getAmount() {
        return this.amount;
    }

    @Override
    public long getCapacity() {
        return this.operator.applyAsInt(this.resource.getDuration());
    }

    // SnapshotParticipant

    @Override
    public Snapshot createSnapshot() {
        return new Snapshot(getResource(), getAmount());
    }

    @Override
    public void readSnapshot(Snapshot snapshot) {
        this.resource = snapshot.resource();
        this.amount = snapshot.amount();
    }

    @Override
    protected void onFinalCommit() {
        // Default abstract method
    }
}
