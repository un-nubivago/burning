package niv.burning.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

import com.google.common.base.Preconditions;

import net.minecraft.core.Holder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import niv.burning.api.FuelVariant;

public class DefaultFuelVariant implements FuelVariant {

    private static final Map<Item, DefaultFuelVariant> INTERN = new ConcurrentHashMap<>();

    private final Item fuel;
    private final int hashCode;

    protected DefaultFuelVariant(Item fuel) {
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

    public static @Nullable FuelVariant of(Item item) {
        Preconditions.checkNotNull(item, "Item may not be null.");
        return Burning.fuelValues().isFuel(new ItemStack(item))
                ? INTERN.computeIfAbsent(item, DefaultFuelVariant::new)
                : FuelVariant.BLANK;
    }

    public static FuelVariant of(ItemLike item) {
        return of(item.asItem());
    }

    public static FuelVariant of(Holder<Item> item) {
        return of(item.value());
    }
}
