package niv.burning.api;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup.BlockApiProvider;
import net.fabricmc.fabric.api.transfer.v1.storage.Storage;
import net.fabricmc.fabric.api.transfer.v1.storage.base.InsertionOnlyStorage;
import net.fabricmc.fabric.api.transfer.v1.transaction.Transaction;
import net.fabricmc.fabric.api.transfer.v1.transaction.TransactionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;

public class BurningStorageUtil {
    private BurningStorageUtil() {
    }

    static final Function<BiConsumer<BlockPos, ToLongFunction<TransactionContext>>, Function<BlockApiLookup<Storage<FuelVariant>, Direction>, BlockApiProvider<Storage<FuelVariant>, Direction>>> INSERTION_PROXY_BUILDER;

    static {
        INSERTION_PROXY_BUILDER = consumer -> lookup -> (world, pos, state, entity, side) -> {
            var storage = lookup.find(world, pos, state, entity, side);
            return storage == null ? null : new InsertionOnlyStorage<FuelVariant>() {
                @Override
                public long insert(FuelVariant variant, long amount, TransactionContext any) {
                    consumer.accept(pos, tx -> storage.insert(variant, amount, tx));
                    return 0L;
                }
            };
        };
    }

    public static final long breadthFirstRecursiveInsert(
            ToLongFunction<TransactionContext> origin, TransactionContext transaction) {
        var open = new LinkedList<ToLongFunction<TransactionContext>>();
        var closed = new HashSet<BlockPos>();
        var proxy = INSERTION_PROXY_BUILDER.apply((pos, fn) -> {
            if (closed.add(pos))
                open.offer(fn);
        });

        ToLongFunction<TransactionContext> elem;
        long inserted = 0L;

        ToLongFunction<TransactionContext> bestElem = null;
        long maxInserted = 0L;

        for (elem = origin; elem != null; elem = open.poll()) {
            BurningStorage.GUARD.set(proxy);
            try (var nested = Transaction.openNested(transaction)) {
                inserted = elem.applyAsLong(nested);
            }
            BurningStorage.GUARD.remove();

            if (inserted > maxInserted) {
                bestElem = elem;
                maxInserted = inserted;
            }
        }

        return bestElem == null ? 0L : bestElem.applyAsLong(transaction);
    }
}
