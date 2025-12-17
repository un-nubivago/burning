package niv.burning.impl;

import static niv.burning.impl.BurningImpl.MOD_ID;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

final class BurningTags {

    public static final TagKey<Block> BLACKLIST;

    static {
        BLACKLIST = TagKey.create(Registries.BLOCK, ResourceLocation.tryBuild(MOD_ID, "blacklist"));
    }

    private BurningTags() {
    }

    public static final void initialize() {
        // Trigger static initialization
    }
}
