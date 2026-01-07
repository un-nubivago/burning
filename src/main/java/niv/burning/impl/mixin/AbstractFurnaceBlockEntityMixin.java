package niv.burning.impl.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.BurningTags;
import niv.burning.api.FuelVariant;
import niv.burning.impl.AbstractFurnaceBlockEntityExtension;
import niv.burning.impl.DefaultFurnaceStorage;

@Mixin(AbstractFurnaceBlockEntity.class)
class AbstractFurnaceBlockEntityMixin implements AbstractFurnaceBlockEntityExtension {

    private static final String BLOCK_POS = "Lnet/minecraft/core/BlockPos;";
    private static final String BLOCK_STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String ENTITY = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;";
    private static final String FUEL_VALUES = "Lnet/minecraft/world/level/block/entity/FuelValues;";
    private static final String ITEM_STACK = "Lnet/minecraft/world/item/ItemStack;";
    private static final String SERVER_LEVEL = "Lnet/minecraft/server/level/ServerLevel;";

    @Unique
    private Storage<FuelVariant> internalBurningStorage;

    @Unique
    private Item internalLastBurnedFuel = Items.AIR;

    @Unique
    @Override
    public Item getInternalBurningFuel() {
        return this.internalLastBurnedFuel;
    }

    @Unique
    @Override
    public void setInternalBurningFuel(Item fuel) {
        this.internalLastBurnedFuel = fuel == null ? Items.AIR : fuel;
    }

    @Override
    public @Nullable Storage<FuelVariant> getBurningStorage(@Nullable Direction direction) {
        if (((BlockEntity) (Object) this).getBlockState().is(BurningTags.BLACKLIST))
            return null;
        if (this.internalBurningStorage == null)
            this.internalBurningStorage = new DefaultFurnaceStorage(((AbstractFurnaceBlockEntity) (Object) this));
        return this.internalBurningStorage;
    }

    @Inject( //
            method = "serverTick(" + SERVER_LEVEL + BLOCK_POS + BLOCK_STATE + ENTITY + ")V", //
            at = @At(value = "INVOKE", shift = Shift.AFTER, //
                    target = ENTITY + "getBurnDuration(" + FUEL_VALUES + ITEM_STACK + ")I"))
    private static void injectAfterGetBurnDuration(CallbackInfo info,
            @Local AbstractFurnaceBlockEntity entity,
            @Local(ordinal = 0) ItemStack itemStack) {
        entity.setInternalBurningFuel(itemStack.getItem());
    }
}
