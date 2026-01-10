package niv.burning.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import niv.burning.api.FuelVariant;

@Internal
public final class DefaultFuelVariant implements FuelVariant {

    private static final Map<Item, DefaultFuelVariant> INTERN = new ConcurrentHashMap<>();

    private final Item fuel;
    private final int hashCode;

    DefaultFuelVariant(Item fuel) {
        this.fuel = fuel;
        this.hashCode = Objects.hash(fuel);
    }

    @Override
    public Item getFuel() {
        return this.fuel;
    }

    @Override
    public int getDuration() {
        return Burning.fuelValues().burnDuration(new ItemStack(fuel));
    }

    @Override
    public String toString() {
        return "FuelVariant{fuel=" + this.fuel + ", duration=" + getDuration() + '}';
    }

    @Override
    public boolean equals(Object other) {
        return this == other
                || (other instanceof DefaultFuelVariant that && this.hashCode == that.hashCode && this.fuel == that.fuel);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Internal
    public static FuelVariant of(Item item) {
        return Burning.fuelValues().isFuel(new ItemStack(item))
                ? INTERN.computeIfAbsent(item, DefaultFuelVariant::new)
                : FuelVariant.BLANK;
    }

    @Internal
    public static FuelVariant of(ItemStack stack) {
        return Burning.fuelValues().isFuel(stack)
                ? INTERN.computeIfAbsent(stack.getItem(), DefaultFuelVariant::new)
                : FuelVariant.BLANK;
    }

    @Internal
    public static boolean isFuel(Item item) {
        return Burning.fuelValues().isFuel(new ItemStack(item));
    }

    @Internal
    public static boolean isFuel(ItemStack stack) {
        return Burning.fuelValues().isFuel(stack);
    }
}
