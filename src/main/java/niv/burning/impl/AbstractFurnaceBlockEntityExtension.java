package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import niv.burning.api.base.BurningStorageBlockEntity;

@Internal
public interface AbstractFurnaceBlockEntityExtension extends BurningStorageBlockEntity {

    Item getInternalBurningFuel();

    void setInternalBurningFuel(Item fuel);
}
