package niv.burning.impl.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.BurningContext;

@Mixin(FuelValues.class)
class FuelValuesMixin implements BurningContext {

    @Unique
    @Override
    public boolean isFuel(Item item) {
        return ((FuelValues) (Object) this).isFuel(new ItemStack(item));
    }

    @Unique(silent = true)
    @Override
    public boolean isFuel(ItemStack itemStack) {
        return ((FuelValues) (Object) this).isFuel(itemStack);
    }

    @Unique
    @Override
    public int burnDuration(Item item) {
        return ((FuelValues) (Object) this).burnDuration(new ItemStack(item));
    }

    @Unique(silent = true)
    @Override
    public int burnDuration(ItemStack itemStack) {
        return ((FuelValues) (Object) this).burnDuration(itemStack);
    }
}
