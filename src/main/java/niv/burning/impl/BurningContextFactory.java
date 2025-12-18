package niv.burning.impl;

import org.jetbrains.annotations.ApiStatus.Internal;

import net.minecraft.world.level.Level;
import niv.burning.api.BurningContext;

@Internal
public final class BurningContextFactory {

    private BurningContextFactory() {
    }

    public static BurningContext defaultContext() {
        return LegacyBurningContext.LAZY_SINGLETON.get();
    }

    public static BurningContext worldlyContext(Level level) {
        return WorldlyBurningContext.newOrExistingInstance(level);
    }
}
