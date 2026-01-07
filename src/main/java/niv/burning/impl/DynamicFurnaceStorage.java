package niv.burning.impl;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.FuelVariant;

final class DynamicFurnaceStorage extends AbstractFurnaceStorage<BlockEntity> {

    private final DynamicFurnaceStorageProvider provider;

    private Item fuel = Items.AIR;

    DynamicFurnaceStorage(DynamicFurnaceStorageProvider provider, BlockEntity target) {
        super(target);
        this.provider = provider;
    }

    // AbstractFurnaceStorage

    @Override
    protected void setResource(FuelVariant resource) {
        this.fuel = resource.getFuel();
        this.provider.litDuration.set(this.target, .0 + getCapacity());

    }

    @Override
    protected void setAmount(long amount) {
        this.provider.litTime.set(this.target, .0 + amount);
    }

    // FurnaceStorage

    @Override
    public FuelVariant getResource() {
        return FuelVariant.of(this.fuel);
    }

    @Override
    public long getAmount() {
        return this.provider.litTime.get(target).longValue();
    }
}
