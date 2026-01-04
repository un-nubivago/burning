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
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import niv.burning.impl.DefaultFuelVariant;

public interface FuelVariant extends TransferVariant<Item> {

    Codec<FuelVariant> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BuiltInRegistries.ITEM.holderByNameCodec().fieldOf("fuel").forGetter(FuelVariant::getRegistryEntry))
            .apply(instance, DefaultFuelVariant::of));

    StreamCodec<RegistryFriendlyByteBuf, FuelVariant> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.holderRegistry(Registries.ITEM), FuelVariant::getRegistryEntry,
            DefaultFuelVariant::of);

    FuelVariant LAVA_BUCKET = of(Items.LAVA_BUCKET);

    FuelVariant BLAZE_ROD = of(Items.BLAZE_ROD);

    FuelVariant COAL = of(Items.COAL);

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
    default DataComponentPatch getComponents() {
        return DataComponentPatch.EMPTY;
    }

    @Override
    default DataComponentMap getComponentMap() {
        return DataComponentMap.EMPTY;
    }

    @Override
    default boolean hasComponents() {
        return false;
    }

    @Override
    default boolean componentsMatch(DataComponentPatch other) {
        return false;
    }

    @SuppressWarnings("deprecation")
    default Holder<Item> getRegistryEntry() {
        return getFuel().builtInRegistryHolder();
    }
}
