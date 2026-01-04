package niv.burning.impl;

import java.util.function.Supplier;
import java.util.stream.Stream;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.flag.FeatureFlagSet;
import net.minecraft.world.flag.FeatureFlags;
import net.minecraft.world.level.block.entity.FuelValues;
import niv.burning.api.BurningStorage;

@Internal
public final class Burning {

    static final String MOD_ID;

    static final String MOD_NAME;

    static final Logger LOGGER;

    static Supplier<FuelValues> fuelValuesGetter;

    static {
        MOD_ID = "burning";
        MOD_NAME = "Burning";
        LOGGER = LoggerFactory.getLogger(MOD_NAME);

        fuelValuesGetter = () -> {
            final var getter = FuelValues.vanillaBurnTimes(HolderLookup.Provider.create(
                    Stream.of(BuiltInRegistries.ITEM)),
                    FeatureFlagSet.of(FeatureFlags.VANILLA));
            fuelValuesGetter = () -> getter;
            return getter;
        };

        /*
         * Register a dynamic registry for DynamicBurningStorageProvider.
         */
        DynamicRegistries.register(DynamicFurnaceStorageProvider.REGISTRY, DynamicFurnaceStorageProvider.CODEC);

        /*
         * Register as providers all loaded DynamicBurningStorageProviders.
         */
        ServerLifecycleEvents.SERVER_STARTING.register(server -> server.registryAccess()
                .lookup(DynamicFurnaceStorageProvider.REGISTRY).stream()
                .flatMap(Registry::stream)
                .forEach(provider -> BurningStorage.SIDED
                        .registerForBlockEntity(provider::getBurningStorage, provider.type)));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> fuelValuesGetter = server::fuelValues);
    }

    private Burning() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }

    static final FuelValues fuelValues() {
        return fuelValuesGetter.get();
    }
}
