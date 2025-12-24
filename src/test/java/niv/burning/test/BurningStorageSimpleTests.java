package niv.burning.test;

import static niv.burning.api.BurningStorage.transfer;
import static niv.burning.impl.BurningContexts.DEFAULT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import niv.burning.api.Burning;
import niv.burning.api.base.SimpleBurningStorage;
import niv.burning.api.base.SimpleBurningStorage.Snapshot;
import niv.burning.impl.BurningImpl;

class BurningStorageSimpleTests {

    private static Burning coal16;
    private static Burning coal12;
    private static Burning coal8;
    private static Burning coal4;

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
        BurningImpl.initialize();

        coal16 = Burning.COAL.one();
        coal12 = Burning.COAL.withValue(1200, DEFAULT);
        coal8 = Burning.COAL.withValue(800, DEFAULT);
        coal4 = Burning.COAL.withValue(400, DEFAULT);
    }

    private SimpleBurningStorage newInstance() {
        return new SimpleBurningStorage() {
            @Override
            protected void onFinalCommit() {
                // do nothing
            }
        };
    }

    @Test
    void testInsertion() {
        var storage = newInstance();
        assertTrue(storage.supportsInsertion());
        assertEquals(Burning.MIN_VALUE, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(coal8, DEFAULT, transaction));
        }
        assertEquals(Burning.MIN_VALUE, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(coal8, DEFAULT, transaction));
            transaction.commit();
        }
        assertEquals(coal8, storage.getBurning(DEFAULT));
    }

    @Test
    void testOverInsertion() {
        var storage = newInstance();
        storage.readSnapshot(new Snapshot(800, 1600, Burning.COAL));
        assertTrue(storage.supportsInsertion());

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(coal16, DEFAULT, transaction));
        }
        assertEquals(coal8, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.insert(coal16, DEFAULT, transaction));
            transaction.commit();
        }
        assertEquals(coal16, storage.getBurning(DEFAULT));
    }

    @Test
    void testExtraction() {
        var storage = newInstance();
        storage.readSnapshot(new Snapshot(1600, 1600, Burning.COAL));
        assertTrue(storage.supportsExtraction());
        assertEquals(coal16, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.extract(coal8, DEFAULT, transaction));
        }
        assertEquals(coal16, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.extract(coal8, DEFAULT, transaction));
            transaction.commit();
        }
        assertEquals(coal8, storage.getBurning(DEFAULT));
    }

    @Test
    void testOverExtraction() {
        var storage = newInstance();
        storage.readSnapshot(new Snapshot(800, 1600, Burning.COAL));
        assertTrue(storage.supportsExtraction());
        assertEquals(coal8, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.extract(coal16, DEFAULT, transaction));
        }
        assertEquals(coal8, storage.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal8, storage.extract(coal16, DEFAULT, transaction));
            transaction.commit();
        }
        assertEquals(Burning.COAL, storage.getBurning(DEFAULT));
    }

    @Test
    void testTransaction() {
        var source = newInstance();
        source.readSnapshot(new Snapshot(800, 1600, Burning.COAL));
        assertTrue(source.supportsExtraction());
        assertEquals(coal8, source.getBurning(DEFAULT));

        var target = newInstance();
        target.readSnapshot(new Snapshot(800, 1600, Burning.COAL));
        assertTrue(target.supportsInsertion());
        assertEquals(coal8, target.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal4, transfer(source, target, coal4, DEFAULT, transaction));
        }
        assertEquals(coal8, source.getBurning(DEFAULT));
        assertEquals(coal8, target.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal4, transfer(source, target, coal4, DEFAULT, transaction));
            transaction.commit();
        }
        assertEquals(coal4, source.getBurning(DEFAULT));
        assertEquals(coal12, target.getBurning(DEFAULT));
    }

    @Test
    void testOverTransaction() {
        var source = newInstance();
        source.readSnapshot(new Snapshot(800, 1600, Burning.COAL));
        assertTrue(source.supportsExtraction());
        assertEquals(coal8, source.getBurning(DEFAULT));

        var target = newInstance();
        target.readSnapshot(new Snapshot(1200, 1600, Burning.COAL));
        assertTrue(target.supportsInsertion());
        assertEquals(coal12, target.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal4, transfer(source, target, coal12, DEFAULT, transaction));
        }
        assertEquals(coal8, source.getBurning(DEFAULT));
        assertEquals(coal12, target.getBurning(DEFAULT));

        try (var transaction = Transaction.openOuter()) {
            assertEquals(coal4, transfer(source, target, coal12, DEFAULT, transaction));
            transaction.commit();
        }
        assertEquals(coal4, source.getBurning(DEFAULT));
        assertEquals(coal16, target.getBurning(DEFAULT));
    }
}
