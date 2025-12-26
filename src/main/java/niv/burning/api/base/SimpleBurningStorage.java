package niv.burning.api.base;

import java.util.function.IntUnaryOperator;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.fabricmc.fabric.api.transfer.v1.transaction.base.SnapshotParticipant;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.Burning;
import niv.burning.api.BurningContext;
import niv.burning.api.BurningStorage;

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
public abstract class SimpleBurningStorage
        extends SnapshotParticipant<SimpleBurningStorage.Snapshot>
        implements BurningStorage {

    public static final record Snapshot(int currentBurning, int maxBurning, Burning zero) {
    }

    protected final IntUnaryOperator operator;

    protected int currentBurning;

    protected int maxBurning;

    protected Burning zero;

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
        this.currentBurning = 0;
        this.maxBurning = 0;
        this.zero = Burning.MIN_VALUE;
    }

    /**
     * Gets this storage remaining burning time.
     *
     * @return a non-negative integer
     */
    public int getCurrentBurning() {
        return currentBurning;
    }

    /**
     * Sets this storage remaining burning time to <code>value</code>, to 0 if
     * <code>value</code> is negative, or to the current maximum burning duration if
     * <code>value</code> is greater than that.
     *
     * @param value the new remaining burning time, clamped to fit between 0 and the
     *              current maximum burning duration
     */
    public void setCurrentBurning(int value) {
        value = value < 0 ? 0 : value;
        value = value > this.maxBurning ? this.maxBurning : value;
        this.currentBurning = value;
    }

    /**
     * Gets the last-set maximum burning duration.
     *
     * @return a non-negative integer
     */
    public int getMaxBurning() {
        return maxBurning;
    }

    /**
     * Sets a new maximum burning duration to <code>value</code>, or to 0 if
     * <code>value</code> is negative.
     * <p>
     * Also sets the current remaining burning time to the new maximum is the former
     * is greater than the latter.
     *
     * @param value the new maximum
     */
    public void setMaxBurning(int value) {
        this.maxBurning = Math.max(0, value);
        if (this.currentBurning > this.maxBurning) {
            this.currentBurning = this.maxBurning;
        }
    }

    /**
     * Loads this burning storage internal status from <code>input</code> using the
     * the tag name "BurningStorage".
     *
     * @param input
     */
    public void load(CompoundTag compoundTag) {
        var snapshotTag = compoundTag.getCompound("BurningSnapshot");
        this.readSnapshot(new Snapshot(
                snapshotTag.getInt("CurrentBurning"),
                snapshotTag.getInt("MaxBurning"),
                Burning.ofZero(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(snapshotTag.getString("Zero"))))));
    }

    /**
     * Saves this burning storage internal status to <code>output</code> using the
     * the tag name "BurningStorage".
     *
     * @param output
     */
    public void save(CompoundTag compoundTag) {
        var snapshotTag = new CompoundTag();
        var snapshot = this.createSnapshot();
        snapshotTag.putInt("CurrentBurning", snapshot.currentBurning());
        snapshotTag.putInt("MaxBurning", snapshot.maxBurning());
        snapshotTag.putString("Zero", BuiltInRegistries.ITEM.getKey(snapshot.zero().getFuel()).toString());
        compoundTag.put("BurningSnapshot", snapshotTag);
    }

    // From {@link SnapshotParticipant}

    @Override
    public Snapshot createSnapshot() {
        return new Snapshot(this.currentBurning, this.maxBurning, this.zero);
    }

    @Override
    public void readSnapshot(Snapshot snapshot) {
        this.currentBurning = snapshot.currentBurning;
        this.maxBurning = snapshot.maxBurning;
        this.zero = snapshot.zero;
    }

    @Override
    protected abstract void onFinalCommit();

    // From {@link BurningStorage}

    @Override
    public Burning insert(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(context, this.operator);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(
                Math.max(this.maxBurning, fuelTime) - this.currentBurning,
                burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.currentBurning += value;
        if ((this.maxBurning > fuelTime && this.currentBurning <= fuelTime) || this.currentBurning > this.maxBurning) {
            this.maxBurning = fuelTime;
            this.zero = burning.zero();
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning extract(Burning burning, BurningContext context, TransactionContext transaction) {
        context = new Context(context, this.operator);
        int fuelTime = burning.getBurnDuration(context);
        int value = Math.min(this.currentBurning, burning.getValue(context).intValue());
        updateSnapshots(transaction);
        this.currentBurning -= value;
        if (this.maxBurning > fuelTime && this.currentBurning <= fuelTime) {
            this.maxBurning = fuelTime;
            this.zero = burning.zero();
        }
        return burning.withValue(value, context);
    }

    @Override
    public Burning getBurning(BurningContext context) {
        context = new Context(context, this.operator);
        return this.zero.withValue(this.currentBurning, context);
    }

    @Override
    public boolean isBurning() {
        return this.currentBurning > 0;
    }

    private static final class Context implements BurningContext {

        private final BurningContext source;

        private final IntUnaryOperator operator;

        public Context(BurningContext source, IntUnaryOperator operator) {
            this.source = source;
            this.operator = operator;
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return this.source.isFuel(itemStack);
        }

        @Override
        public boolean isFuel(Item item) {
            return this.source.isFuel(item);
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return this.operator.applyAsInt(this.source.burnDuration(itemStack));
        }

        @Override
        public int burnDuration(Item item) {
            return this.operator.applyAsInt(this.source.burnDuration(item));
        }
    }
}
