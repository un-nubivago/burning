package niv.burning.api;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Triple;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import niv.burning.impl.BurningImpl;

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
            ResourceLocation.tryBuild(BurningImpl.MOD_ID, "burning_propagator"),
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
     * Finds all burning storages the block at position
     * <code>startingBlockPos</code> can propagate to through burning propagators
     * blocks.
     * <p>
     * Uses a breath-first search algorithm to search the propagators graph and
     * searches up to 64 blocks on every path.
     * <p>
     * For each new discovered block besides the first, remembers the direction of
     * the previous block and uses its opposite as a parameter for sided access.
     *
     * @param level            the level the starting block is in and in which the
     *                         search happens
     * @param startingBlockPos the position the starting block is at
     * @param callback         a bi-consumer called whenever a burning storage is
     *                         found. It consume the position at which it is found
     *                         and the burning storage itself
     */
    static void searchBurningStorages(Level level, BlockPos startingBlockPos,
            BiConsumer<BlockPos, BurningStorage> callback) {
        var queue = new LinkedList<Triple<Direction, BlockPos, Integer>>();
        var visited = new HashSet<BlockPos>();
        for (var pair = Triple.of((Direction) null, startingBlockPos, 64); pair != null; pair = queue.pollFirst()) {
            var from = pair.getLeft();
            var pos = pair.getMiddle();
            var hops = pair.getRight() - 1;

            BurningPropagator propagator = null;

            if (hops > 0)
                propagator = BurningPropagator.SIDED.find(level, pos, from);

            if (propagator == null)
                propagator = (a, b) -> Set.of();

            for (var direction : propagator.evalPropagationTargets(level, pos)) {
                var relative = pos.relative(direction);
                if (visited.contains(relative))
                    break;
                queue.add(Triple.of(direction.getOpposite(), relative, hops));
            }

            var storage = BurningStorage.SIDED.find(level, pos, from);

            if (storage != null)
                callback.accept(pos, storage);

            visited.add(pos);
        }
    }
}
