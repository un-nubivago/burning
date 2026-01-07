package niv.burning.impl;

import it.unimi.dsi.fastutil.objects.Object2IntLinkedOpenHashMap;
import it.unimi.dsi.fastutil.objects.Object2IntSortedMap;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.FuelValues;
import net.minecraft.world.level.block.entity.FurnaceBlockEntity;

public class CommonUtils {
    private CommonUtils() {
    }

    private static final String TIME;
    private static final String DURATION;

    private static final DynamicFurnaceStorageProvider PROVIDER;

    static {

        TIME = "litTime";
        DURATION = "litDuration";

        PROVIDER = DynamicFurnaceStorageProvider.from(BlockEntityType.FURNACE, TIME, DURATION);

        Burning.fuelValuesGetter = () -> {
            var map = new Object2IntLinkedOpenHashMap<Item>();
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
            final var getter = new OpenFuelValues(map);
            Burning.fuelValuesGetter = () -> getter;
            return getter;
        };
    }

    private static final class OpenFuelValues extends FuelValues {
        protected OpenFuelValues(Object2IntSortedMap<Item> object2IntSortedMap) {
            super(object2IntSortedMap);
        }
    }

    public static DynamicFurnaceStorage newDynamicFurnace() {
        return new DynamicFurnaceStorage(PROVIDER,
                new FurnaceBlockEntity(BlockPos.ZERO, Blocks.FURNACE.defaultBlockState()));
    }

    public static final void initialize() {
        // Trigger stating initialization
    }
}
