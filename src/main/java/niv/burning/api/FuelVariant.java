package niv.burning.api;

import org.jetbrains.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.item.Item;
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

    Codec<FuelVariant> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("fuel").forGetter(FuelVariant::getRegistryEntry))
            .apply(instance, DefaultFuelVariant::of));

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
     * Retrieves a instance if {@code fuel} is actually a fuel.
     *
     * @param fuel a non-null object
     * @return a non-null, non-blank fuel varian if {@code fuel} is a fuel, {@link #BLANK} otherwise.
     */
    static FuelVariant of(ItemLike fuel) {
        return DefaultFuelVariant.of(fuel);
    }

    Item getFuel();

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
        result.putString("item", BuiltInRegistries.ITEM.getKey(getFuel()).toString());
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

    @SuppressWarnings("deprecation")
    default Holder<Item> getRegistryEntry() {
        return getFuel().builtInRegistryHolder();
    }
}
