package niv.burning.impl;

import static java.util.Objects.requireNonNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import net.minecraft.core.component.DataComponentMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.FuelVariant;

@Internal
@NullMarked
public final class DefaultFuelVariant implements FuelVariant {

    private static final Map<Item, DefaultFuelVariant> INTERN = new ConcurrentHashMap<>();

    private final Item fuel;
    private final int hashCode;

    DefaultFuelVariant(@Nullable Item fuel) {
        this.fuel = requireNonNull(fuel);
        this.hashCode = Objects.hash(fuel);
    }

    @Override
    public Item getFuel() {
        return this.fuel;
    }

    @Override
    public int getDuration() {
        return Burning.fuelValues().burnDuration(new ItemStack(this.fuel));
    }

    @Override
    public String toString() {
        return "FuelVariant{fuel=" + this.fuel + ", duration=" + getDuration() + '}';
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return this == other || (other instanceof DefaultFuelVariant that
                && this.hashCode == that.hashCode
                && this.fuel == that.fuel);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Internal
    public static FuelVariant of(@Nullable Item item) {
        return item != null && isFuel(item) ? requireNonNull(INTERN.computeIfAbsent(item, DefaultFuelVariant::new)) : BLANK;
    }

    @Internal
    public static FuelVariant of(@Nullable ItemStack stack) {
        return stack != null && isFuel(stack) ? requireNonNull(INTERN.computeIfAbsent(stack.getItem(), DefaultFuelVariant::new)) : BLANK;
    }

    @Internal
    public static boolean isFuel(@Nullable Item item) {
        if (item == null)
            return false;
        @SuppressWarnings("deprecation")
        var holder = item.builtInRegistryHolder();
        if (!holder.areComponentsBound()) {
            holder.bindComponents(DataComponentMap.EMPTY);
        }
        return Burning.fuelValues().isFuel(new ItemStack(holder));
    }

    @Internal
    public static boolean isFuel(@Nullable ItemStack stack) {
        return stack != null && Burning.fuelValues().isFuel(stack);
    }
}
