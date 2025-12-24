package niv.burning.impl;

import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import niv.burning.api.BurningContext;
import niv.burning.api.base.SimpleBurningContext;

public class BurningContexts {

    public static final BurningContext DEFAULT;

    public static final BurningContext HALVED;

    public static final BurningContext SQUARED;

    static {
        var map = new Object2IntOpenHashMap<Item>(64);
        map.put(Items.LAVA_BUCKET, 20000);
        map.put(Blocks.COAL_BLOCK.asItem(), 16000);
        map.put(Items.BLAZE_ROD, 2400);
        map.put(Items.COAL, 1600);
        map.put(Items.CHARCOAL, 1600);
        map.put(Blocks.BAMBOO_MOSAIC.asItem(), 300);
        map.put(Blocks.BAMBOO_MOSAIC_STAIRS.asItem(), 300);
        map.put(Blocks.BAMBOO_MOSAIC_SLAB.asItem(), 150);
        map.put(Blocks.NOTE_BLOCK.asItem(), 300);
        map.put(Blocks.BOOKSHELF.asItem(), 300);
        map.put(Blocks.CHISELED_BOOKSHELF.asItem(), 300);
        map.put(Blocks.LECTERN.asItem(), 300);
        map.put(Blocks.JUKEBOX.asItem(), 300);
        map.put(Blocks.CHEST.asItem(), 300);
        map.put(Blocks.TRAPPED_CHEST.asItem(), 300);
        map.put(Blocks.CRAFTING_TABLE.asItem(), 300);
        map.put(Blocks.DAYLIGHT_DETECTOR.asItem(), 300);
        map.put(Items.BOW, 300);
        map.put(Items.FISHING_ROD, 300);
        map.put(Blocks.LADDER.asItem(), 300);
        map.put(Items.WOODEN_SHOVEL, 200);
        map.put(Items.WOODEN_SWORD, 200);
        map.put(Items.WOODEN_HOE, 200);
        map.put(Items.WOODEN_AXE, 200);
        map.put(Items.WOODEN_PICKAXE, 200);
        map.put(Items.STICK, 100);
        map.put(Items.BOWL, 100);
        map.put(Blocks.DRIED_KELP_BLOCK.asItem(), 4001);
        map.put(Items.CROSSBOW, 300);
        map.put(Blocks.BAMBOO.asItem(), 50);
        map.put(Blocks.DEAD_BUSH.asItem(), 100);
        map.put(Blocks.SCAFFOLDING.asItem(), 50);
        map.put(Blocks.LOOM.asItem(), 300);
        map.put(Blocks.BARREL.asItem(), 300);
        map.put(Blocks.CARTOGRAPHY_TABLE.asItem(), 300);
        map.put(Blocks.FLETCHING_TABLE.asItem(), 300);
        map.put(Blocks.SMITHING_TABLE.asItem(), 300);
        map.put(Blocks.COMPOSTER.asItem(), 300);
        map.put(Blocks.AZALEA.asItem(), 100);
        map.put(Blocks.FLOWERING_AZALEA.asItem(), 100);
        map.put(Blocks.MANGROVE_ROOTS.asItem(), 300);

        DEFAULT = new SimpleBurningContext(map);

        var halved = new Object2IntOpenHashMap<Item>(map.size());
        var squared = new Object2IntOpenHashMap<Item>(map.size());

        map.forEach((item, value) -> {
            if (item != null && value > 0) {
                halved.put(item, value / 2);
                squared.put(item, value * value);
            }
        });

        HALVED = new SimpleBurningContext(halved);
        SQUARED = new SimpleBurningContext(squared);
    }
}
