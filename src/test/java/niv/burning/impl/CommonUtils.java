package niv.burning.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import niv.burning.api.FurnaceStorage;

public class CommonUtils {
    private CommonUtils() {
    }

    private static final String TIME;
    private static final String DURATION;

    private static final DynamicFurnaceStorageProvider PROVIDER;

    static {

        TIME = "litTime";
        DURATION = "litDuration";

        PROVIDER = DynamicFurnaceStorageProvider.from(BlockEntityType.FURNACE, TIME, DURATION);

    }

    public static FurnaceStorage newDynamicFurnace() {
        return new DynamicFurnaceStorage(PROVIDER,
                new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState()));
    }

    public static final void initialize() {
        // Trigger stating initialization
    }
}
