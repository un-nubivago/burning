package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import niv.burning.api.BurningStorage;

@Internal
public final class Burning {

    public static final String MOD_ID;

    public static final String MOD_NAME;

    public static final Logger LOGGER;

    static {
        MOD_ID = "burning";
        MOD_NAME = "Burning";
        LOGGER = LoggerFactory.getLogger(MOD_NAME);

        /*
         * Register a dynamic registry for DynamicBurningStorageProvider.
         */
        DynamicRegistries.register(DynamicFurnaceStorageProvider.REGISTRY, DynamicFurnaceStorageProvider.CODEC);

        /*
         * Register as providers all loaded DynamicBurningStorageProviders.
         */
        ServerLifecycleEvents.SERVER_STARTING.register(server -> server.registryAccess()
                .registry(DynamicFurnaceStorageProvider.REGISTRY).stream()
                .flatMap(Registry::stream)
                .forEach(provider -> BurningStorage.SIDED
                        .registerForBlockEntity(provider::getBurningStorage, provider.type)));
    }

    private Burning() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}
