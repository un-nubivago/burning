package niv.burning.api;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.impl.BurningContextFactory;

/**
 * Represents a context for determining fuel status and burn duration for items
 * and item stacks.
 * <p>
 * Implementations define what counts as fuel and how long it burns.
 * </p>
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
     * Returns a singleton BurningContext instance with default item-to-fuel-value
     * mappings.
     * <p>
     * Prior to Minecraft 1.21.2, this implementation forwards to
     * {@link AbstractFurnaceBlockEntity#isFuel(ItemStack)} method and
     * {@link AbstractFurnaceBlockEntity#getFuel()} map.
     * </p>
     * <p>
     * Since Minecraft 1.21.2, this implementation embeds a map identical to the now
     * defunct {@link AbstractFurnaceBlockEntity#getFuel()} and queries it.
     * </p>
     * @deprecated Prefer {@link #worldlyContext(Level)} to respect newer versions'
     *             dynamic/world-specific fuel values.
     * @return the default legacy-based BurningContext
     */
    @SuppressWarnings("java:S1133")
    @Deprecated(since = "", forRemoval = false)
    static BurningContext defaultContext() {
        return BurningContextFactory.defaultContext();
    }

    /**
     * Returns a new or existing BurningContext bound to the provided {@link Level}
     * that queries its fuel values.
     * <p>
     * Prior to Minecraft 1.21.2, this returns the same as
     * {@link #defaultContext()}.
     * </p>
     * @param level must not be null
     * @return a level-scoped BurningContext that uses the world's fuel rules
     */
    static BurningContext worldlyContext(Level level) {
        return BurningContextFactory.worldlyContext(level);
    }
}
