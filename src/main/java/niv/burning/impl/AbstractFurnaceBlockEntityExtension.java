package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import net.minecraft.world.item.Item;
import niv.burning.api.base.BurningStorageBlockEntity;

@Internal
@NullMarked
public interface AbstractFurnaceBlockEntityExtension extends BurningStorageBlockEntity {

    Item getInternalBurningFuel();

    void setInternalBurningFuel(@Nullable Item fuel);
}
