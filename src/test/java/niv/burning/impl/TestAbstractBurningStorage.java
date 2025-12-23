package niv.burning.impl;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import niv.burning.api.base.FurnaceBurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

public class TestAbstractBurningStorage extends FurnaceBurningStorage {

    public TestAbstractBurningStorage(BlockEntityType<? extends AbstractFurnaceBlockEntity> type, Block block) {
        super(type.create(BlockPos.ZERO, block.defaultBlockState()));
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
