package niv.burning.impl;

import static java.lang.Math.clamp;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.FuelVariant;

@Internal
public final class DefaultFurnaceStorage extends AbstractFurnaceStorage<AbstractFurnaceBlockEntity> {

    public DefaultFurnaceStorage(AbstractFurnaceBlockEntity target) {
        super(target);
    }

    // AbstractFurnaceStorage

    @Override
    protected void setResource(FuelVariant resource) {
        this.target.setInternalBurningFuel(resource.getFuel());
        this.target.litTotalTime = this.target.getBurnDuration(Burning.fuelValues(), new ItemStack(resource.getFuel()));
    }

    @Override
    protected void setAmount(long amount) {
        var capacity = getCapacity();
        this.target.litTimeRemaining = clamp(capacity == 0
                ? amount
                : amount * this.target.litTotalTime / capacity,
                0, Integer.MAX_VALUE);
    }

    // FurnaceStorage

    @Override
    public FuelVariant getResource() {
        return FuelVariant.of(this.target.getInternalBurningFuel());
    }

    @Override
    public long getAmount() {
        return this.target.litTotalTime == 0
                ? this.target.litTimeRemaining
                : this.target.litTimeRemaining * getCapacity() / this.target.litTotalTime;
    }
}
