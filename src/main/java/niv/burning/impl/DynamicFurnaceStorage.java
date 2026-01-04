package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.FuelVariant;
import niv.burning.api.base.AbstractFurnaceStorage;
import niv.burning.api.base.BurningStorageBlockEntity;

@Internal
public class DynamicFurnaceStorage extends AbstractFurnaceStorage {

    private final DynamicFurnaceStorageProvider provider;

    private final BlockEntity target;

    private Item fuel = Items.AIR;

    DynamicFurnaceStorage(DynamicFurnaceStorageProvider provider, BlockEntity target) {
        this.provider = provider;
        this.target = target;
    }

    // AbstractFurnaceStorage

    @Override
    protected long getCapacity(FuelVariant variant) {
        return Burning.fuelValues().burnDuration(new ItemStack(variant.getFuel()));
    }

    @Override
    protected void setResource(FuelVariant resource) {
        this.fuel = resource.getFuel();
        this.provider.litDuration.set(this.target, .0 + getCapacity(resource));

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

    @Override
    public long getCapacity() {
        return this.provider.litDuration.get(target).longValue();
    }

    // SnapshotParticipant

    @Override
    protected void onFinalCommit() {
        if (this.target.hasLevel()) {
            BurningStorageBlockEntity.tryUpdateLitProperty(this.target, getAmount() > 0);
            this.target.setChanged();
        }
    }
}
