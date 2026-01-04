package niv.burning.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.core.BlockPos;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;
import niv.burning.api.FuelVariant;
import niv.burning.api.FurnaceStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;

class DynamicFurnaceStorageTests {

    private static final String TIME = "litTimeRemaining";
    private static final String DURATION = "litTotalTime";

    static DynamicFurnaceStorageProvider provider;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        TestBurning.initialize();

        provider = DynamicFurnaceStorageProvider.from(BlockEntityType.FURNACE, TIME, DURATION);
    }

    private FurnaceStorage newInstance() {
        return new DynamicFurnaceStorage(provider,
                new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState()));
    }

    @Test
    void testEmptyStorage() {
        var storage = newInstance();

        assertTrue(storage.supportsInsertion());
        assertFalse(storage.supportsExtraction());
        assertTrue(storage.isResourceBlank());
        assertTrue(storage.getResource().isBlank());

        assertEquals(Items.AIR, storage.getResource().getFuel());
        assertEquals(0, storage.getAmount());
        assertEquals(0, storage.getCapacity());
    }

    @Test
    void testInsertion() {
        var storage = newInstance();

        // test cancelled insertion
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1800, storage.insert(FuelVariant.of(Items.BLAZE_ROD), 1800, transaction));

            assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
            assertEquals(1800, storage.getAmount());
            assertEquals(2400, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }
        assertEquals(Items.AIR, storage.getResource().getFuel());
        assertEquals(0, storage.getAmount());
        assertEquals(0, storage.getCapacity());

        assertTrue(storage.isResourceBlank());
        assertTrue(storage.getResource().isBlank());

        // test insertion
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1800, storage.insert(FuelVariant.of(Items.BLAZE_ROD), 1800, transaction));
            transaction.commit();
        }

        assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
        assertEquals(1800, storage.getAmount());
        assertEquals(2400, storage.getCapacity());

        assertFalse(storage.isResourceBlank());
        assertFalse(storage.getResource().isBlank());

        // test insertion into full
        ((DynamicFurnaceStorage) storage).readSnapshot(new Snapshot(FuelVariant.BLAZE_ROD, 2400));
        try (var transaction = Transaction.openOuter()) {
            assertEquals(0, storage.insert(FuelVariant.of(Items.BLAZE_ROD), 1200, transaction));
            transaction.commit();
        }

        assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
        assertEquals(2400, storage.getAmount());
        assertEquals(2400, storage.getCapacity());

        assertFalse(storage.isResourceBlank());
        assertFalse(storage.getResource().isBlank());
    }

    @Test
    void testOverInsertion() {
        var storage = newInstance();
        ((DynamicFurnaceStorage) storage).readSnapshot(new Snapshot(FuelVariant.BLAZE_ROD, 1800));

        // test over-insertion with same variant
        try (var transaction = Transaction.openOuter()) {
            assertEquals(600, storage.insert(FuelVariant.of(Items.BLAZE_ROD), 1800, transaction));

            assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
            assertEquals(2400, storage.getAmount());
            assertEquals(2400, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }

        // test over-insertion with less powerful varian
        try (var transaction = Transaction.openOuter()) {
            assertEquals(600, storage.insert(FuelVariant.of(Items.COAL), 1800, transaction));

            assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
            assertEquals(2400, storage.getAmount());
            assertEquals(2400, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }

        // test over-insertion with more powerful varian
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1800, storage.insert(FuelVariant.of(Items.LAVA_BUCKET), 1800, transaction));

            assertEquals(Items.LAVA_BUCKET, storage.getResource().getFuel());
            assertEquals(3600, storage.getAmount());
            assertEquals(20000, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }
    }

    @Test
    void testExtraction() {
        var storage = newInstance();
        ((DynamicFurnaceStorage) storage).readSnapshot(new Snapshot(FuelVariant.BLAZE_ROD, 1800));

        // test unsupported extraction
        try (var transaction = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FuelVariant.of(Items.BLAZE_ROD), 1200, transaction));
            transaction.commit();
        }
        assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
        assertEquals(1800, storage.getAmount());
        assertEquals(2400, storage.getCapacity());

        assertFalse(storage.isResourceBlank());
        assertFalse(storage.getResource().isBlank());

        // test unsupported extraction from empty
        ((DynamicFurnaceStorage) storage).readSnapshot(new Snapshot(FuelVariant.BLAZE_ROD, 0));
        try (var transaction = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FuelVariant.of(Items.BLAZE_ROD), 1200, transaction));
            transaction.commit();
        }

        assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
        assertEquals(0, storage.getAmount());
        assertEquals(2400, storage.getCapacity());

        assertFalse(storage.isResourceBlank());
        assertFalse(storage.getResource().isBlank());
    }
}
