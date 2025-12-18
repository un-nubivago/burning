package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import niv.burning.api.BurningStorage;

@Internal
@SuppressWarnings("java:S100")
public interface AbstractFurnaceBlockEntityExtension {

    Item burning_getFuel();

    void burning_setFuel(Item fuel);

    BurningStorage burning_getBurningStorage();
}
