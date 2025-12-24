package niv.burning.impl.mixin;

import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.At.Shift;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import com.llamalad7.mixinextras.sugar.Local;

import net.minecraft.core.Direction;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import niv.burning.api.BurningStorage;
import niv.burning.api.BurningTags;
import niv.burning.api.base.FurnaceBurningStorage;
import niv.burning.impl.AbstractFurnaceBlockEntityExtension;

@Mixin(AbstractFurnaceBlockEntity.class)
class AbstractFurnaceBlockEntityMixin implements AbstractFurnaceBlockEntityExtension {

    private static final String LEVEL = "Lnet/minecraft/world/level/Level;";
    private static final String BLOCK_POS = "Lnet/minecraft/core/BlockPos;";
    private static final String BLOCK_STATE = "Lnet/minecraft/world/level/block/state/BlockState;";
    private static final String ENTITY = "Lnet/minecraft/world/level/block/entity/AbstractFurnaceBlockEntity;";
    private static final String ITEM_STACK = "Lnet/minecraft/world/item/ItemStack;";

    @Unique
    private final BurningStorage internalBurningStorage = new FurnaceBurningStorage(
            (AbstractFurnaceBlockEntity) (Object) this);

    @Unique
    private Item internalLastBurnedFuel;

    @Unique
    @Override
    public Item getInternalBurningFuel() {
        return this.internalLastBurnedFuel;
    }

    @Unique
    @Override
    public void setInternalBurningFuel(Item fuel) {
        this.internalLastBurnedFuel = fuel;
    }

    @Override
    public @Nullable BurningStorage getBurningStorage(@Nullable Direction direction) {
        if (((BlockEntity) (Object) this).getBlockState().is(BurningTags.BLACKLIST)) {
            return null;
        } else {
            return this.internalBurningStorage;
        }
    }

    @Inject( //
            method = "serverTick(" + LEVEL + BLOCK_POS + BLOCK_STATE + ENTITY + ")V", //
            at = @At(value = "INVOKE", shift = Shift.AFTER, //
                    target = ENTITY + "getBurnDuration(" + ITEM_STACK + ")I"))
    private static void injectAfterGetBurnDuration(CallbackInfo info,
            @Local AbstractFurnaceBlockEntity entity,
            @Local(ordinal = 0) ItemStack itemStack) {
        entity.setInternalBurningFuel(itemStack.getItem());
    }
}
