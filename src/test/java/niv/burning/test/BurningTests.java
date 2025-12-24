package niv.burning.test;

import static niv.burning.impl.BurningContexts.DEFAULT;
import static niv.burning.impl.BurningContexts.HALVED;
import static niv.burning.impl.BurningContexts.SQUARED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.spongepowered.include.com.google.common.base.Objects;

import net.minecraft.SharedConstants;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.Items;
import niv.burning.api.Burning;

class BurningTests {

    @BeforeAll
    static void setup() {
        SharedConstants.tryDetectVersion();
        Bootstrap.bootStrap();
    }

    @Test
    void testBurningBuilder() {
        var burningOneOptional = Burning.ofOptional(Items.LAVA_BUCKET, DEFAULT);
        assertTrue(burningOneOptional.isPresent());

        var burningOne = burningOneOptional.get().withValue(10000, DEFAULT);
        assertEquals(Items.LAVA_BUCKET, burningOne.getFuel());
        assertEquals(.5d, burningOne.getPercent());

        var burningTwoOptional = Burning.ofOptional(Items.LAVA_BUCKET, HALVED);
        assertTrue(burningTwoOptional.isPresent());

        var burningTwo = burningTwoOptional.get().withValue(5000, HALVED);
        assertEquals(Items.LAVA_BUCKET, burningTwo.getFuel());
        assertEquals(10000, burningTwo.getValue(DEFAULT).intValue());

        assertTrue(Objects.equal(burningOne, burningTwo));
    }

    @Test
    void testOperations() {
        final var coal16 = Burning.COAL.withValue(1600, DEFAULT);
        final var coal6 = Burning.COAL.withValue(600, DEFAULT);
        final var coal4 = Burning.COAL.withValue(400, DEFAULT);
        final var coal0 = Burning.COAL.withValue(0, DEFAULT);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000, DEFAULT);

        final var add1 = Burning.add(coal6, blaze10, DEFAULT);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, DEFAULT);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, DEFAULT);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, DEFAULT);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, DEFAULT));
        assertEquals(coal4, Burning.minValue(coal6, coal4, DEFAULT));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, DEFAULT));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, DEFAULT));
    }

    @Test
    void testOperationsHalf() {
        final var coal16 = Burning.COAL.withValue(1600 / 2, HALVED);
        final var coal6 = Burning.COAL.withValue(600 / 2, HALVED);
        final var coal4 = Burning.COAL.withValue(400 / 2, HALVED);
        final var coal0 = Burning.COAL.withValue(0, HALVED);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000 / 2, HALVED);

        final var add1 = Burning.add(coal6, blaze10, HALVED);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, HALVED);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, HALVED);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, HALVED);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, HALVED));
        assertEquals(coal4, Burning.minValue(coal6, coal4, HALVED));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, HALVED));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, HALVED));
    }

    @Test
    void testOperationsSquare() {
        final var coal16 = Burning.COAL.withValue(1600 * 1600, SQUARED);
        final var coal6 = Burning.COAL.withValue(600 * 1600, SQUARED);
        final var coal4 = Burning.COAL.withValue(400 * 1600, SQUARED);
        final var coal0 = Burning.COAL.withValue(0, SQUARED);

        final var blaze10 = Burning.BLAZE_ROD.withValue(1000 * 1600, SQUARED);

        final var add1 = Burning.add(coal6, blaze10, SQUARED);
        assertEquals(coal16, add1);

        final var add2 = Burning.add(blaze10, coal6, SQUARED);
        assertEquals(coal16, add2);

        assertTrue(Objects.equal(add1, add2));

        final var sub1 = Burning.subtract(blaze10, coal6, SQUARED);
        assertEquals(coal4, sub1);

        final var sub2 = Burning.subtract(coal6, blaze10, SQUARED);
        assertEquals(coal0, sub2);

        assertFalse(Objects.equal(sub1, sub2));

        assertEquals(coal4, Burning.minValue(coal4, coal6, SQUARED));
        assertEquals(coal4, Burning.minValue(coal6, coal4, SQUARED));

        assertEquals(coal6, Burning.maxValue(coal4, coal6, SQUARED));
        assertEquals(coal6, Burning.maxValue(coal6, coal4, SQUARED));
    }
}
