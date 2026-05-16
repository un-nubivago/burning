package niv.burning.impl;

import static niv.burning.impl.Burning.LOGGER;
import static niv.burning.impl.Burning.MOD_NAME;

import java.util.Objects;
import java.util.Optional;
import java.util.function.BiFunction;

import org.apache.commons.lang3.reflect.FieldUtils;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;

import net.fabricmc.fabric.mixin.lookup.BlockEntityTypeAccessor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

@NullMarked
final class DynamicFurnaceStorageProvider
        implements BiFunction<@NonNull BlockEntity, @Nullable Direction, DynamicFurnaceStorage> {

    public static final ResourceKey<Registry<DynamicFurnaceStorageProvider>> REGISTRY = ResourceKey
            .createRegistryKey(Identifier.parse("burning:dynamic_storage"));

    @SuppressWarnings("null")
    public static final Codec<DynamicFurnaceStorageProvider> CODEC = RecordCodecBuilder.create(instance -> instance
            .group(
                    BuiltInRegistries.BLOCK_ENTITY_TYPE.byNameCodec().fieldOf("type").forGetter(src -> src.type),
                    Codec.STRING.fieldOf("lit_time").forGetter(src -> src.litTime.getName()),
                    Codec.STRING.fieldOf("lit_duration").forGetter(src -> src.litDuration.getName()))
            .apply(instance, DynamicFurnaceStorageProvider::from));

    final BlockEntityType<?> type;

    final DynamicField litTime;

    final DynamicField litDuration;

    private DynamicFurnaceStorageProvider(BlockEntityType<?> type, DynamicField litTime, DynamicField litDuration) {
        this.type = type;
        this.litTime = litTime;
        this.litDuration = litDuration;
    }

    @Override
    public DynamicFurnaceStorage apply(BlockEntity entity, @Nullable Direction side) {
        return new DynamicFurnaceStorage(this, entity);
    }

    @SuppressWarnings("null")
    static final @Nullable DynamicFurnaceStorageProvider from(
            BlockEntityType<?> type, String litTime, String litDuration) {
        Optional<@NonNull Class<?>> optional = ((BlockEntityTypeAccessor) type).getBlocks()
                .stream().findAny()
                .map(Block::defaultBlockState)
                .map(state -> type.create(BlockPos.ZERO, state))
                .map(Object::getClass);

        var typeName = LOGGER.isInfoEnabled()
                ? Objects.toString(BuiltInRegistries.BLOCK_ENTITY_TYPE.getKey(type))
                : null;

        if (optional.isEmpty()) {
            LOGGER.warn("[{}] Failed to load dynamic storage for type {}, failed to get block entity class instance",
                    MOD_NAME, typeName);
            return null;
        }

        var clazz = optional.get();

        var litTimeField = Optional.ofNullable(FieldUtils
                .getField(clazz, litTime, true))
                .flatMap(DynamicField::of);

        if (litTimeField.isEmpty()) {
            LOGGER.warn(
                    "[{}] Failed to load dynamic storage for type {}, field {} of class {} not found",
                    MOD_NAME, typeName, litTime, clazz.getCanonicalName());
            return null;
        }

        var litDurationField = Optional.ofNullable(FieldUtils
                .getField(clazz, litDuration, true))
                .flatMap(DynamicField::of);

        if (litDurationField.isEmpty()) {
            LOGGER.warn(
                    "[{}] Failed to load dynamic storage for type {}, field {} of class {} not found",
                    MOD_NAME, typeName, litDuration, clazz.getCanonicalName());
            return null;
        }

        LOGGER.info("[{}] Dynamic storage for type {} successfully loaded",
                MOD_NAME, typeName);

        return new DynamicFurnaceStorageProvider(type, litTimeField.get(), litDurationField.get());
    }
}
