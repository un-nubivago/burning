package niv.burning.api;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.fabricmc.fabric.api.transfer.v1.storage.TransferVariant;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponentMap;
import net.minecraft.core.component.DataComponentPatch;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import niv.burning.impl.DefaultFuelVariant;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

/**
 * Provides an immutable wrapper around a fuel item, with no data components.
 *
 * <p>
 * Do not implement, use the static {@code of(...)} functions instead.
 */
@NullMarked
public interface FuelVariant extends TransferVariant<Item> {
    @SuppressWarnings("null")
    Codec<@Nullable FuelVariant> CODEC = RecordCodecBuilder.create(instance ->
        instance
            .group(
                BuiltInRegistries.ITEM.holderByNameCodec()
                    .fieldOf("fuel")
                    .forGetter(FuelVariant::typeHolder)
            )
            .apply(instance, FuelVariant::of)
    );

    @SuppressWarnings("null")
    StreamCodec<RegistryFriendlyByteBuf, @Nullable FuelVariant> STREAM_CODEC =
        StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM),
            FuelVariant::typeHolder,
            FuelVariant::of
        );

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
     * @param item an item
     * @return a non-blank instance if {@code item} is a fuel, {@link #BLANK}
     *         otherwise.
     */
    static FuelVariant of(@Nullable Item item) {
        return DefaultFuelVariant.of(item);
    }

    /**
     * Retrieves a instance if {@code item} is actually a fuel.
     *
     * @param item an item
     * @return a non-blank instance if {@code item} is a fuel, {@link #BLANK}
     *         otherwise.
     */
    static FuelVariant of(@Nullable ItemLike item) {
        return DefaultFuelVariant.of(item == null ? null : item.asItem());
    }

    /**
     * Retrieves a instance if {@code stack} is actually a fuel.
     *
     * @param stack a stack
     * @return a non-blank instance if {@code stack} is a fuel, {@link #BLANK}
     *         otherwise.
     */
    static FuelVariant of(@Nullable ItemStack stack) {
        return DefaultFuelVariant.of(stack);
    }

    /**
     * Retrieves a instance if {@code item} is actually a fuel.
     *
     * @param item an item
     * @return a non-blank instance if {@code item} is a fuel, {@link #BLANK}
     *         otherwise.
     */
    static FuelVariant of(@Nullable Holder<Item> item) {
        return DefaultFuelVariant.of(item == null ? null : item.value());
    }

    /**
     * Check wether {@code item} is a fuel.
     *
     * @param item an item
     * @return tru if {@code item} is a fuel, false otherwise
     */
    static boolean isFuel(@Nullable Item item) {
        return DefaultFuelVariant.isFuel(item);
    }

    /**
     * Check wether {@code item} is a fuel.
     *
     * @param item an item
     * @return tru if {@code item} is a fuel, false otherwise
     */
    static boolean isFuel(@Nullable ItemLike item) {
        return DefaultFuelVariant.isFuel(item == null ? null : item.asItem());
    }

    /**
     * Check wether {@code stack} is a fuel.
     *
     * @param stack a stack
     * @return tru if {@code stack} is a fuel, false otherwise
     */
    static boolean isFuel(@Nullable ItemStack stack) {
        return DefaultFuelVariant.isFuel(stack);
    }

    /**
     * Check wether {@code item} is a fuel.
     *
     * @param item an item
     * @return tru if {@code item} is a fuel, false otherwise
     */
    static boolean isFuel(@Nullable Holder<Item> item) {
        return DefaultFuelVariant.isFuel(item == null ? null : item.value());
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
    default DataComponentPatch getComponentsPatch() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    default DataComponentMap getComponents() {
        return DataComponentMap.EMPTY;
    }

    @Override
    default boolean hasComponents() {
        return false;
    }

    @Override
    default boolean componentsMatch(@Nullable DataComponentPatch other) {
        return false;
    }

    @Override
    default Holder<Item> typeHolder() {
        return BuiltInRegistries.ITEM.wrapAsHolder(getFuel());
    }
}
