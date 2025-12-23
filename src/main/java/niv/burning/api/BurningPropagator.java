package niv.burning.api;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiPredicate;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

/**
 * Provides a common interfaces for blocks able to propagate the "burning"
 * generated somewhere to nearby blocks.
 * <p>
 * Assumes the use of a BFS traversal algorithm to find all the burning
 * storages connected through blocks providing this interface.
 *
 * @since 2.0
 */
public interface BurningPropagator {

    /**
     * Sided block access to burning propagators.
     * <p>
     * The {@code Direction} parameter may be null, meaning that a full instance
     * (ignoring side restrictions) should be queried. Refer to
     * {@link BlockApiLookup} for documentation on how to use this field.
     * <p>
     * Blocks implementing this interface are automatically registered.
     */
    BlockApiLookup<BurningPropagator, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryParse("burning:sided_propagator"),
            BurningPropagator.class, Direction.class);

    /**
     * Finds all directions towards which this block is able to propagate.
     * <p>
     * Implementations may query this block internal state the decide, for each
     * direction, whether to return it or not.
     *
     * @param level    the level this block is in
     * @param blockPos the position this block is at
     * @return a set of vetted directions
     */
    Set<Direction> evalPropagationTargets(Level level, BlockPos blockPos);

    /**
     * Finds all burning storages the block at position {@code start} can propagate
     * to through burning propagators blocks, and calls {@code shallReturn} for each
     * of those storages and their respective positions.
     * <p>
     * Returns early if/when {@code shallReturn} returns {@code true}.
     * <p>
     * The order at which burning storages are found isn't guaranteed.
     *
     * @param level       the level the starting block is in and in which the
     *                    search happens
     * @param start       the position the starting block is at
     * @param shallReturn a non-null bi-predicate, used for callback and cancelling
     */
    static void searchBurningStorages(Level level, BlockPos start,
            BiPredicate<BlockPos, BurningStorage> shallReturn) {
        var open = new LinkedList<Triple<Direction, BlockPos, Integer>>();
        var closed = HashSet.newHashSet(64);
        closed.add(start);
        for (var elem = Triple.of((Direction) null, start, 64); elem != null; elem = open.poll()) {
            var from = elem.getLeft();
            var pos = elem.getMiddle();

            var storage = BurningStorage.SIDED.find(level, pos, from);
            if (storage != null && shallReturn.test(pos, storage))
                return;

            BurningPropagator propagator = null;
            var hops = elem.getRight() - 1;

            if (hops > 0)
                propagator = BurningPropagator.SIDED.find(level, pos, from);

            if (propagator == null)
                continue;

            var dirs = propagator.evalPropagationTargets(level, pos).toArray(Direction[]::new);
            for (int i = dirs.length - 1; i > 0; i--) {
                int j = level.random.nextInt(i + 1);
                var d = dirs[j];
                dirs[j] = dirs[i];
                dirs[i] = d;
            }

            for (var dir : dirs) {
                var relative = pos.relative(dir);
                if (closed.add(relative))
                    open.add(Triple.of(dir.getOpposite(), relative, hops));
            }
        }
    }
}
