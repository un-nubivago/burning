package niv.burning.api;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.base.SimpleBurningContext;

/**
 * Provides a wrapper interfaces for different ways to determine whether an item
 * is fuel and to query its burn duration.
 *
 * @since 1.0
 */
public interface BurningContext {

    /**
     * Returns whether the provided {@link Item} is considered fuel in this context.
     *
     * @param item must not be null
     * @return true if the provided {@link Item} is a fuel according to this
     *         context, false otherwise
     */
    boolean isFuel(Item item);

    /**
     * Returns whether the provided {@link ItemStack} is considered fuel in this
     * context.
     *
     * @param itemStack must not be null
     * @return true if the provided {@link ItemStack} is a fuel according to this
     *         context, false otherwise
     */
    boolean isFuel(ItemStack itemStack);

    /**
     * Returns the burn duration for the provided {@link Item} if it is a fuel, or
     * zero otherwise.
     *
     * @param item must not be null
     * @return a non-negative integer: the burn duration for the provided
     *         {@link Item} in this context
     */
    int burnDuration(Item item);

    /**
     * Returns the burn duration for the provided {@link ItemStack} if it is a fuel,
     * or zero otherwise.
     *
     * @param itemStack must not be null
     * @return a non-negative integer: the burn duration for the provided
     *         {@link ItemStack} in this context
     */
    int burnDuration(ItemStack itemStack);

    /**
     * Returns a default burning context.
     * <p>
     * Prior to Minecraft 1.21.2, returns a wrapper around
     * {@link AbstractFurnaceBlockEntity#isFuel(ItemStack)} method and
     * {@link AbstractFurnaceBlockEntity#getFuel()} map.
     * <p>
     * Since Minecraft 1.21.2, returns a vanilla context, that is, a context with
     * only the fuels present in the most vanilla configuration.
     * <p>
     *
     * @return a default burning context
     * @see {@link SimpleBurningContext#legacyInstance()} for a prior to Minecraft
     *      1.21.2 fuel map
     * @since 2.0
     * @deprecated As of Burning 2.0, prefer {@link #worldlyContext(Level)}
     */
    @SuppressWarnings("java:S1133")
    @Deprecated(since = "2.0", forRemoval = false)
    static BurningContext defaultContext() {
        return DEFAULT;
    }

    /**
     * Returns the burning context bound to the provided {@link Level#fuelValues()
     * level} parameter.
     * <p>
     * Prior to Minecraft 1.21.2, this returns the same as
     * {@link #defaultContext()}.
     *
     * @param level must not be null
     * @since 2.0
     * @return a burning context that uses the level's fuel values
     */
    @SuppressWarnings("java:S1172")
    static BurningContext worldlyContext(Level level) {
        return DEFAULT;
    }

    @Internal
    static BurningContext DEFAULT = new BurningContext() {

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
}
