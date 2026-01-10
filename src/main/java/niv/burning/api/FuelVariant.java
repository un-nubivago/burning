package niv.burning.api;

import static java.util.Objects.requireNonNull;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import niv.burning.impl.DefaultFuelVariant;

/**
 * Provides an immutable wrapper around a fuel item, with no data components.
 *
 * <p>
 * Do not implement, use the static {@code of(...)} functions instead.
 */
public interface FuelVariant extends TransferVariant<Item> {

    /**
     * Shortcut to {@code FuelVarian.of(Items.LAVA_BUCKET)}
     */
    FuelVariant LAVA_BUCKET = of(Items.LAVA_BUCKET);

    /**
     * Shortcut to {@code FuelVarian.of(Items.BLAZE_ROD)}
     */
    FuelVariant BLAZE_ROD = of(Items.BLAZE_ROD);

    /**
     * Shortcut to {@code FuelVarian.of(Items.COAL)}
     */
    FuelVariant COAL = of(Items.COAL);

    /**
     * A singleton blank instance
     */
    FuelVariant BLANK = new FuelVariant() {
        @Override
        public Item getFuel() {
            return Items.AIR;
        }

        @Override
        public int getDuration() {
            return 0;
        }

        @Override
        public boolean isBlank() {
            return true;
        }
    };

    /**
     * Retrieves a blank ItemVariant.
     *
     * <p>
     * Usable to pass {@code FuelVarian::blank} as a supplier argument instead of
     * {@code () -> FuelVarian.BLANK}.
     *
     * @return a non-null, blank fuel variant
     */
    @SuppressWarnings("java:S1845")
    static FuelVariant blank() {
        return BLANK;
    }

    /**
     * Retrieves a instance if {@code item} is actually a fuel.
     *
     * @param item a non-null item
     * @return a non-blank instance if {@code item} is a fuel, {@link #BLANK} otherwise.
     */
    static FuelVariant of(Item item) {
        return DefaultFuelVariant.of(requireNonNull(item));
    }

    /**
     * Retrieves a instance if {@code item} is actually a fuel.
     *
     * @param item a non-null item
     * @return a non-blank instance if {@code item} is a fuel, {@link #BLANK} otherwise.
     */
    static FuelVariant of(ItemLike item) {
        return DefaultFuelVariant.of(requireNonNull(item).asItem());
    }

    /**
     * Retrieves a instance if {@code stack} is actually a fuel.
     *
     * @param stack a non-null stack
     * @return a non-blank instance if {@code stack} is a fuel, {@link #BLANK} otherwise.
     */
    static FuelVariant of(ItemStack stack) {
        return DefaultFuelVariant.of(requireNonNull(stack));
    }

    /**
     * Retrieves a instance if {@code item} is actually a fuel.
     *
     * @param item a non-null item
     * @return a non-blank instance if {@code item} is a fuel, {@link #BLANK} otherwise.
     */
    static FuelVariant of(Holder<Item> item) {
        return DefaultFuelVariant.of(requireNonNull(item).value());
    }

    /**
     * Check wether {@code item} is a fuel.
     *
     * @param item a non-null item
     * @return tru if {@code item} is a fuel, false otherwise
     */
    static boolean isFuel(Item item) {
        return DefaultFuelVariant.isFuel(requireNonNull(item));
    }

    /**
     * Check wether {@code item} is a fuel.
     *
     * @param item a non-null item
     * @return tru if {@code item} is a fuel, false otherwise
     */
    static boolean isFuel(ItemLike item) {
        return DefaultFuelVariant.isFuel(requireNonNull(item).asItem());
    }

    /**
     * Check wether {@code stack} is a fuel.
     *
     * @param stack a non-null stack
     * @return tru if {@code stack} is a fuel, false otherwise
     */
    static boolean isFuel(ItemStack stack) {
        return DefaultFuelVariant.isFuel(requireNonNull(stack));
    }

    /**
     * Check wether {@code item} is a fuel.
     *
     * @param item a non-null item
     * @return tru if {@code item} is a fuel, false otherwise
     */
    static boolean isFuel(Holder<Item> item) {
        return DefaultFuelVariant.isFuel(requireNonNull(item).value());
    }

    /**
     * Return the immutable item instance of this variant.
     *
     * @return a non-null item instance
     */
    Item getFuel();

    /**
     * Return the commutated burn duration of this variant.
     *
     * @return a non-negative integer
     */
    int getDuration();

    @Override
    default boolean isBlank() {
        return false;
    }

    @Override
    default Item getObject() {
        return getFuel();
    }

    @Override
    default @Nullable CompoundTag getNbt() {
        return null;
    }

    @Override
    default CompoundTag toNbt() {
        var result = new CompoundTag();
        result.putString("fuel", BuiltInRegistries.ITEM.getKey(getFuel()).toString());
        return result;
    }

    @Override
    default void toPacket(FriendlyByteBuf buf) {
        if (isBlank()) {
            buf.writeBoolean(false);
        } else {
            buf.writeBoolean(true);
            buf.writeVarInt(Item.getId(getFuel()));
        }
    }

    default Holder<Item> getRegistryEntry() {
        return BuiltInRegistries.ITEM.wrapAsHolder(getFuel());
    }

    /**
     * Deserialize a variant from an NBT compound tag, assuming it was serialized
     * using {@link #toNbt}. If an error occurs during deserialization, it will be
     * logged with the DEBUG level, and a blank variant will be returned.
     */
    static FuelVariant fromNbt(CompoundTag compoundTag) {
        return DefaultFuelVariant.fromNbt(compoundTag);
    }

    /**
     * Write a variant from a packet byte buffer, assuming it was serialized using
     * {@link #toPacket}.
     */
    static FuelVariant fromPacket(FriendlyByteBuf buf) {
        return DefaultFuelVariant.fromPacket(buf);
    }
}
