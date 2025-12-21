package niv.burning.api;

import static java.util.function.Predicate.not;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.function.BiConsumer;

import org.apache.commons.lang3.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import net.fabricmc.fabric.api.lookup.v1.block.BlockApiLookup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import niv.burning.impl.BurningImpl;

public interface BurningPropagator {

    BlockApiLookup<BurningPropagator, @Nullable Direction> SIDED = BlockApiLookup.get(
            ResourceLocation.tryBuild(BurningImpl.MOD_ID, "burning_propagator"),
            BurningPropagator.class, Direction.class);

    Set<Direction> evalPropagationTargets(Level level, BlockPos blockPos);

    static void searchBurningStorages(Level level, BlockPos startingBlockPos,
            BiConsumer<BlockPos, BurningStorage> callback) {
        var queue = new LinkedList<Pair<BlockPos, Integer>>();
        var visited = new HashSet<BlockPos>();
        for (var pair = Pair.of(startingBlockPos, 64); pair != null; pair = queue.pollFirst()) {
            var pos = pair.getLeft();
            var hops = pair.getRight() - 1;
            if (hops >= 0) {
                var propagator = BurningPropagator.SIDED.find(level, pos, null);
                if (propagator != null)
                    propagator.evalPropagationTargets(level, pos).stream()
                            .map(pos::relative)
                            .filter(not(visited::contains))
                            .map(value -> Pair.of(value, hops))
                            .forEach(queue::addLast);
            }
            var storage = BurningStorage.SIDED.find(level, pos, null);
            if (storage != null)
                callback.accept(pos, storage);
            visited.add(pos);
        }
    }
}
