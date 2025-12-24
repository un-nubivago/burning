package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;
import net.minecraft.core.Registry;
import niv.burning.api.BurningStorage;
import niv.burning.api.base.BurningStorageBlockEntity;

@Internal
public final class BurningImpl {

    static final String MOD_ID;

    static final String MOD_NAME;

    static final Logger LOGGER;

    static {
        MOD_ID = "burning";
        MOD_NAME = "Burning";
        LOGGER = LoggerFactory.getLogger(MOD_NAME);

        /*
         * Register a dynamic registry for DynamicBurningStorageProvider.
         */
        DynamicRegistries.register(DynamicBurningStorageProvider.REGISTRY, DynamicBurningStorageProvider.CODEC);

        /*
         * Register fallback for block entities implementing the
         * BurningStorageBlockEntity interface.
         *
         * Note: AbstractFurnaceBlockEntity implements BurningStorageBlockEntity.
         */
        BurningStorage.SIDED.registerFallback((level, pos, state, entity, side) -> {
            if (entity instanceof BurningStorageBlockEntity instance) {
                return instance.getBurningStorage(side);
            } else {
                return null;
            }
        });

        /*
         * Register as providers all loaded DynamicBurningStorageProviders.
         */
        ServerLifecycleEvents.SERVER_STARTING.register(server -> server.registryAccess()
                .lookup(DynamicBurningStorageProvider.REGISTRY).stream()
                .flatMap(Registry::stream)
                .forEach(provider -> BurningStorage.SIDED
                        .registerForBlockEntity(provider::getBurningStorage, provider.type)));
    }

    private BurningImpl() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}
