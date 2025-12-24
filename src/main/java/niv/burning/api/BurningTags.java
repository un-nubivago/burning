package niv.burning.api;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

/**
 * Provides this library own tags.
 */
public final class BurningTags {

    /**
     * Block entities extending AbstractFurnaceBlockEntity that are under this tags
     * will not provide a BurningStorage when queried unless registered otherwise.
     */
    public static final TagKey<Block> BLACKLIST;

    static {
        BLACKLIST = TagKey.create(Registries.BLOCK, ResourceLocation.tryParse("burning:blacklist"));
    }

    private BurningTags() {
    }
}
