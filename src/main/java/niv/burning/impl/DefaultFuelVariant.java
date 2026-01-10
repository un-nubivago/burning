package niv.burning.impl;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
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
        return AbstractFurnaceBlockEntity.getFuel().getOrDefault(this.fuel, 0);
    }

    @Override
    public String toString() {
        return "FuelVariant{fuel=" + this.fuel + ", duration=" + getDuration() + '}';
    }

    @Override
    public boolean equals(Object other) {
        return this == other || (other instanceof DefaultFuelVariant that
                && this.hashCode == that.hashCode
                && this.fuel == that.fuel);
    }

    @Override
    public int hashCode() {
        return this.hashCode;
    }

    @Internal
    public static FuelVariant of(Item item) {
        return isFuel(item) ? INTERN.computeIfAbsent(item, DefaultFuelVariant::new) : BLANK;
    }

    @Internal
    public static FuelVariant of(ItemStack stack) {
        return isFuel(stack) ? INTERN.computeIfAbsent(stack.getItem(), DefaultFuelVariant::new) : BLANK;
    }

    @Internal
    public static boolean isFuel(Item item) {
        return AbstractFurnaceBlockEntity.isFuel(new ItemStack(item));
    }

    @Internal
    public static boolean isFuel(ItemStack stack) {
        return AbstractFurnaceBlockEntity.isFuel(stack);
    }

    @Internal
    public static FuelVariant fromNbt(CompoundTag compoundTag) {
        try {
            return of(BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(compoundTag.getString("fuel"))));
        } catch (RuntimeException rex) {
            Burning.LOGGER.debug("Tried to load an invalid FuelVariant from NBT: {}", compoundTag, rex);
            return BLANK;
        }
    }

    @Internal
    public static FuelVariant fromPacket(FriendlyByteBuf buf) {
        return buf.readBoolean() ? of(Item.byId(buf.readVarInt())) : BLANK;
    }
}
