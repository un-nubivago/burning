package niv.burning.impl;

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
        this.target.litDuration = this.target.getBurnDuration(new ItemStack(resource.getFuel()));
    }

    @Override
    protected void setAmount(long amount) {
        var capacity = getCapacity();
        this.target.litTime = clamp(capacity == 0
                ? amount
                : amount * this.target.litDuration / capacity,
                0, Integer.MAX_VALUE);
    }

    // FurnaceStorage

    @Override
    public FuelVariant getResource() {
        return FuelVariant.of(this.target.getInternalBurningFuel());
    }

    @Override
    public long getAmount() {
        return this.target.litDuration == 0
                ? this.target.litTime
                : this.target.litTime * getCapacity() / this.target.litDuration;
    }

    private static final int clamp(long amount, long min, long max) {
        if (amount < min)
            return (int) min;
        else if (amount > max)
            return (int) max;
        else
            return (int) amount;
    }
}
