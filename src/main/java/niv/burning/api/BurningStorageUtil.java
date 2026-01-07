package niv.burning.api;

import java.util.ArrayList;
import java.util.PriorityQueue;
import java.util.function.Function;
import java.util.function.ToLongFunction;

import it.unimi.dsi.fastutil.Pair;
import it.unimi.dsi.fastutil.longs.LongObjectPair;
import it.unimi.dsi.fastutil.objects.Object2LongOpenHashMap;
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

    public static final long recursiveInsert(
            BlockPos originPos, ToLongFunction<TransactionContext> origin, TransactionContext transaction) {
        Function<BlockApiLookup<Storage<FuelVariant>, Direction>, BlockApiProvider<Storage<FuelVariant>, Direction>> proxy;

        var open = new PriorityQueue<LongObjectPair<Pair<BlockPos, ToLongFunction<TransactionContext>>>>(
                (a, b) -> Long.compare(a.keyLong(), b.keyLong()));
        open.add(LongObjectPair.of(0, Pair.of(originPos, origin)));

        var candidates = new ArrayList<Pair<BlockPos, ToLongFunction<TransactionContext>>>(6);

        var closed = new Object2LongOpenHashMap<BlockPos>();

        proxy = lookup -> (world, pos, state, entity, side) -> {
            var storage = lookup.find(world, pos, state, entity, side);
            return storage == null ? null : new InsertionOnlyStorage<FuelVariant>() {
                @Override
                public long insert(FuelVariant variant, long amount, TransactionContext any) {
                    candidates.add(Pair.of(pos, tx -> storage.insert(variant, amount, tx)));
                    return 0L;
                }
            };
        };

        var inserted = 0L;
        var max = 0L;

        ToLongFunction<TransactionContext> result = tx -> 0L;

        for (var elem = open.poll(); elem != null; elem = open.poll()) {
            BurningStorage.GUARD.set(proxy);
            try (var nested = Transaction.openNested(transaction)) {
                inserted = elem.value().value().applyAsLong(nested);
            }
            BurningStorage.GUARD.remove();

            if (!candidates.isEmpty()) {
                final var newTotal = elem.firstLong() + inserted;
                closed.computeLong(elem.value().key(), (key, oldTotal) -> {
                    if (oldTotal == null || newTotal < oldTotal) {
                        candidates.stream().map(value -> LongObjectPair.of(newTotal, value)).forEach(open::add);
                        return newTotal;
                    }
                    return oldTotal;
                });
                candidates.clear();

            } else if (inserted > max) {
                result = elem.value().value();
                max = inserted;
            }
        }

        return result.applyAsLong(transaction);
    }
}
