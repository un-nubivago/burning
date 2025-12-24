package niv.burning.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;

public class TestDynamicBurningStorage extends DynamicBurningStorage {

    private static final String TIME = "litTimeRemaining";
    private static final String DURATION = "litTotalTime";

    public TestDynamicBurningStorage(BlockEntityType<? extends AbstractFurnaceBlockEntity> type, Block block) {
        super(
                DynamicBurningStorageProvider.from(type, TIME, DURATION),
                type.create(BlockPos.ZERO, block.defaultBlockState()));
    }

    @Override
    protected void onFinalCommit() {
        // do nothing
    }

    @Override
    public void readSnapshot(Snapshot snapshot) {
        super.readSnapshot(snapshot);
    }
}
