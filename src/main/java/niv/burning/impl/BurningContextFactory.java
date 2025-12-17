package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningContext;

@Internal
public final class BurningContextFactory {

    private static final BurningContext DEFAULT = new BurningContext() {

        @Override
        public boolean isFuel(Item item) {
            return AbstractFurnaceBlockEntity.isFuel(new ItemStack(item));
        }

        @Override
        public boolean isFuel(ItemStack itemStack) {
            return AbstractFurnaceBlockEntity.isFuel(itemStack);
        }

        @Override
        public int burnDuration(Item item) {
            return AbstractFurnaceBlockEntity.getFuel().getOrDefault(item, 0);
        }

        @Override
        public int burnDuration(ItemStack itemStack) {
            return itemStack.isEmpty() ? 0 : AbstractFurnaceBlockEntity.getFuel().getOrDefault(itemStack.getItem(), 0);
        }
    };

    private BurningContextFactory() {
    }

    public static BurningContext defaultContext() {
        return DEFAULT;
    }

    public static BurningContext worldlyContext(Level level) {
        return DEFAULT;
    }
}
