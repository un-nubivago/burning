package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.spongepowered.include.com.google.common.base.Preconditions;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;
import niv.burning.api.base.AbstractFurnaceStorage;
import niv.burning.api.base.BurningStorageBlockEntity;

@Internal
public class DefaultFurnaceStorage extends AbstractFurnaceStorage {

    private final AbstractFurnaceBlockEntity target;

    DefaultFurnaceStorage(AbstractFurnaceBlockEntity target) {
        this.target = target;
    }

    // AbstractFurnaceStorage

    @Override
    protected long getCapacity(FuelVariant variant) {
        return this.target.getBurnDuration(Burning.fuelValues(), new ItemStack(variant.getFuel()));
    }

    @Override
    protected void setResource(FuelVariant resource) {
        this.target.setInternalBurningFuel(resource.getFuel());
        this.target.litTotalTime = Math.clamp(getCapacity(resource), 0, Integer.MAX_VALUE);
    }

    @Override
    protected void setAmount(long amount) {
        this.target.litTimeRemaining = Math.clamp(amount, 0, Integer.MAX_VALUE);
    }

    // FurnaceStorage

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
    protected void onFinalCommit() {
        if (this.target.hasLevel()) {
            BurningStorageBlockEntity.tryUpdateLitProperty(this.target, getAmount() > 0);
            this.target.setChanged();
        }
    }

    // static

    public static final FurnaceStorage of(AbstractFurnaceBlockEntity entity) {
        Preconditions.checkNotNull(entity, "Entity may not be null.");
        return new DefaultFurnaceStorage(entity);
    }
}
