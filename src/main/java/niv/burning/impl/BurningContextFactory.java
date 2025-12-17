package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import niv.burning.api.BurningContext;

@Internal
public final class BurningContextFactory {

    private static final BurningContext EMPTY = new BurningContext() {

        @Override
        public boolean isFuel(Item item) {
            return false;
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return false;
        }

        @Override
        public int burnDuration(Item item) {
            return 0;
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return 0;
        }
    };

    private BurningContextFactory() {
    }

    public static BurningContext defaultContext() {
        return EMPTY;
    }

    public static BurningContext worldlyContext(Level level) {
        return new FuelValuesBurningContext(level.fuelValues());
    }
}
