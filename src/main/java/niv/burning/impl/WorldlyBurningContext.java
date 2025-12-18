package niv.burning.impl;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import niv.burning.api.BurningContext;

final class WorldlyBurningContext implements BurningContext {

    private static final Map<Level, WorldlyBurningContext> CACHE = WeakHashMap.newWeakHashMap(3);

    private final Level level;

    private WorldlyBurningContext(Level level) {
        this.level = level;
    }

    @Override
    public boolean isFuel(Item item) {
        return this.level.fuelValues().isFuel(new ItemStack(item));
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return this.level.fuelValues().isFuel(itemStack);
    }

    @Override
    public int burnDuration(Item item) {
        return this.level.fuelValues().burnDuration(new ItemStack(item));
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return this.level.fuelValues().burnDuration(itemStack);
    }

    static final WorldlyBurningContext newOrExistingInstance(Level level) {
        return CACHE.computeIfAbsent(level, WorldlyBurningContext::new);
    }
}
