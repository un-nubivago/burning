package niv.burning.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.registry.DynamicRegistries;

public final class BurningImpl {

    public static final String MOD_ID;

    public static final String MOD_NAME;

    static final Logger LOGGER;

    static {
        MOD_ID = "burning";
        MOD_NAME = "Burning";
        LOGGER = LoggerFactory.getLogger(MOD_NAME);

        /*
         * Register a dynamic registry for DynamicBurningStorageProvider.
         */
        DynamicRegistries.register(DynamicBurningStorageProvider.REGISTRY, DynamicBurningStorageProvider.CODEC);
    }

    private BurningImpl() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}
