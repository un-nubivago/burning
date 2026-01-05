package niv.burning.api;

import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import niv.burning.api.base.BurningStorageBlockEntity;

public final class BurningStorage {

    public static final BlockApiLookup<Storage<FuelVariant>, @Nullable Direction> SIDED = new RecursionSafeWrapper(
            BlockApiLookup.get(
                    ResourceLocation.tryParse("burning:sided_storage"),
                    Storage.asClass(), Direction.class));

    static final ThreadLocal<Function<BlockApiLookup<Storage<FuelVariant>, @Nullable Direction>, BlockApiProvider<Storage<FuelVariant>, @Nullable Direction>>> GUARD = ThreadLocal
            .withInitial(() -> lookup -> lookup::find);

    static {
        /*
         * Register fallback for block entities implementing the
         * BurningStorageBlockEntity interface.
         *
         * Note: AbstractFurnaceBlockEntity implements BurningStorageBlockEntity.
         */
        SIDED.registerFallback((world, pos, state, entity, side) -> {
            if (entity instanceof BurningStorageBlockEntity getter) {
                return getter.getBurningStorage(side);
            } else {
                return null;
            }
        });
    }

    private BurningStorage() {
    }

    private static final record RecursionSafeWrapper(BlockApiLookup<Storage<FuelVariant>, @Nullable Direction> lookup)
            implements BlockApiLookup<Storage<FuelVariant>, @Nullable Direction> {

        @Override
        public @Nullable Storage<FuelVariant> find(Level world, BlockPos pos,
                @Nullable BlockState state, @Nullable BlockEntity blockEntity, @Nullable Direction context) {
            return GUARD.get().apply(this.lookup).find(world, pos, state, blockEntity, context);
        }

        @Override
        public void registerSelf(BlockEntityType<?>... blockEntityTypes) {
            this.lookup.registerSelf(blockEntityTypes);
        }

        @Override
        public void registerForBlocks(
                BlockApiProvider<Storage<FuelVariant>, @Nullable Direction> provider,
                Block... blocks) {
            this.lookup.registerForBlocks(provider, blocks);
        }

        @Override
        public void registerForBlockEntities(
                BlockEntityApiProvider<Storage<FuelVariant>, @Nullable Direction> provider,
                BlockEntityType<?>... blockEntityTypes) {
            this.lookup.registerForBlockEntities(provider, blockEntityTypes);
        }

        @Override
        public void registerFallback(
                BlockApiProvider<Storage<FuelVariant>, @Nullable Direction> fallbackProvider) {
            this.lookup.registerFallback(fallbackProvider);
        }

        @Override
        public ResourceLocation getId() {
            return this.lookup.getId();
        }

        @Override
        public Class<Storage<FuelVariant>> apiClass() {
            return this.lookup.apiClass();
        }

        @Override
        public Class<@Nullable Direction> contextClass() {
            return this.lookup.contextClass();
        }

        @Override
        public @Nullable BlockApiProvider<Storage<FuelVariant>, @Nullable Direction> getProvider(Block block) {
            return this.lookup.getProvider(block);
        }
    }
}
