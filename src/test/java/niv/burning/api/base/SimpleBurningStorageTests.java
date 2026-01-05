package niv.burning.api.base;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import niv.burning.api.FuelVariant;
import niv.burning.impl.CommonUtils;

class SimpleBurningStorageTests {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        CommonUtils.initialize();
    }

    @Test
    void testEmptyStorage() {
        var storage = new SimpleBurningStorage();

        assertTrue(storage.supportsInsertion());
        assertTrue(storage.supportsExtraction());
        assertTrue(storage.isResourceBlank());
        assertTrue(storage.getResource().isBlank());

        assertEquals(Items.AIR, storage.getResource().getFuel());
        assertEquals(0, storage.getAmount());
        assertEquals(0, storage.getCapacity());
    }

    @Test
    void testInsertion() {
        var storage = new SimpleBurningStorage();

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
        storage.variant = FuelVariant.BLAZE_ROD;
        storage.amount = 2400;
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
        var storage = new SimpleBurningStorage();
        storage.variant = FuelVariant.BLAZE_ROD;
        storage.amount = 1800;

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
        var storage = new SimpleBurningStorage();
        storage.variant = FuelVariant.BLAZE_ROD;
        storage.amount = 1800;

        // test cancelled extraction
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1200, storage.extract(FuelVariant.of(Items.BLAZE_ROD), 1200, transaction));

            assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
            assertEquals(600, storage.getAmount());
            assertEquals(2400, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }
        assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
        assertEquals(1800, storage.getAmount());
        assertEquals(2400, storage.getCapacity());

        assertFalse(storage.isResourceBlank());
        assertFalse(storage.getResource().isBlank());

        // test extraction
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1200, storage.extract(FuelVariant.of(Items.BLAZE_ROD), 1200, transaction));
            transaction.commit();
        }

        assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
        assertEquals(600, storage.getAmount());
        assertEquals(2400, storage.getCapacity());

        assertFalse(storage.isResourceBlank());
        assertFalse(storage.getResource().isBlank());

        // test extraction from empty
        storage = new SimpleBurningStorage();
        try (var transaction = Transaction.openOuter()) {
            assertEquals(0, storage.extract(FuelVariant.of(Items.BLAZE_ROD), 1200, transaction));
            transaction.commit();
        }

        assertEquals(Items.AIR, storage.getResource().getFuel());
        assertEquals(0, storage.getAmount());
        assertEquals(0, storage.getCapacity());

        assertTrue(storage.isResourceBlank());
        assertTrue(storage.getResource().isBlank());
    }

    @Test
    void testUnderExtraction() {
        var storage = new SimpleBurningStorage();
        storage.variant = FuelVariant.BLAZE_ROD;
        storage.amount = 1800;

        // test over-extraction with same variant
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1800, storage.extract(FuelVariant.of(Items.BLAZE_ROD), 2400, transaction));

            assertEquals(Items.AIR, storage.getResource().getFuel());
            assertEquals(0, storage.getAmount());
            assertEquals(0, storage.getCapacity());

            assertTrue(storage.isResourceBlank());
            assertTrue(storage.getResource().isBlank());
        }

        // test under-extraction with less powerful varian
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1200, storage.extract(FuelVariant.of(Items.COAL), 1200, transaction));

            assertEquals(Items.COAL, storage.getResource().getFuel());
            assertEquals(600, storage.getAmount());
            assertEquals(1600, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }

        // test under-extraction with lesser powerful varian
        try (var transaction = Transaction.openOuter()) {
            assertEquals(300, storage.extract(FuelVariant.of(Items.BOOKSHELF), 300, transaction));

            assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
            assertEquals(1500, storage.getAmount());
            assertEquals(2400, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }

        // test under-extraction with more powerful varian
        try (var transaction = Transaction.openOuter()) {
            assertEquals(1200, storage.extract(FuelVariant.of(Items.LAVA_BUCKET), 1200, transaction));

            assertEquals(Items.BLAZE_ROD, storage.getResource().getFuel());
            assertEquals(600, storage.getAmount());
            assertEquals(2400, storage.getCapacity());

            assertFalse(storage.isResourceBlank());
            assertFalse(storage.getResource().isBlank());
        }
    }
}
