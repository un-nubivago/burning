package niv.burning.api.base;

import java.util.Map;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.AbstractFurnaceBlockEntity;
import niv.burning.api.BurningContext;

/**
 * Provides a simple and immutable burning context backed by an
 * {@link Object2IntMap} between items and burn times (ticks).
 * <p>
 * Items not mapped have 0 burn time and are not fuel.
 */
public class SimpleBurningContext implements BurningContext {

    private static SimpleBurningContext lazyLegacyInstance;

    private final Object2IntMap<Item> values;

    /**
     * Creates an empty context (no items are fuel).
     */
    public SimpleBurningContext() {
        this.values = new Object2IntOpenHashMap<>();
    }

    /**
     * Creates a context using a copy of the given map.
     *
     * @param values item-to-burn duration map (null = empty)
     */
    public SimpleBurningContext(Map<Item, Integer> values) {
        this();
        if (values != null)
            this.values.putAll(values);
    }

    @Override
    public boolean isFuel(Item item) {
        return this.values.getInt(item) > 0;
    }

    @Override
    public boolean isFuel(ItemStack stack) {
        return this.values.getInt(stack.getItem()) > 0;
    }

    @Override
    public int burnDuration(Item item) {
        return this.values.getInt(item);
    }

    @Override
    public int burnDuration(ItemStack stack) {
        return this.values.getInt(stack.getItem());
    }

    /**
     * Returns a singleton instance whose backing map is identical to that returned
     * by the {@link AbstractFurnaceBlockEntity#getFuel()} method. Method that, as
     * of Minecraft 1.21.2, no longer exists.
     *
     * @return a lazy initialized singleton instance
     */
    public static final SimpleBurningContext legacyInstance() {
        if (lazyLegacyInstance == null) {
            var map = new Object2IntOpenHashMap<Item>();
            add(map, Items.LAVA_BUCKET, 20000);
            add(map, Blocks.COAL_BLOCK, 16000);
            add(map, Items.BLAZE_ROD, 2400);
            add(map, Items.COAL, 1600);
            add(map, Items.CHARCOAL, 1600);
            add(map, ItemTags.LOGS, 300);
            add(map, ItemTags.BAMBOO_BLOCKS, 300);
            add(map, ItemTags.PLANKS, 300);
            add(map, Blocks.BAMBOO_MOSAIC, 300);
            add(map, ItemTags.WOODEN_STAIRS, 300);
            add(map, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
            add(map, ItemTags.WOODEN_SLABS, 150);
            add(map, Blocks.BAMBOO_MOSAIC_SLAB, 150);
            add(map, ItemTags.WOODEN_TRAPDOORS, 300);
            add(map, ItemTags.WOODEN_PRESSURE_PLATES, 300);
            add(map, ItemTags.WOODEN_FENCES, 300);
            add(map, ItemTags.FENCE_GATES, 300);
            add(map, Blocks.NOTE_BLOCK, 300);
            add(map, Blocks.BOOKSHELF, 300);
            add(map, Blocks.CHISELED_BOOKSHELF, 300);
            add(map, Blocks.LECTERN, 300);
            add(map, Blocks.JUKEBOX, 300);
            add(map, Blocks.CHEST, 300);
            add(map, Blocks.TRAPPED_CHEST, 300);
            add(map, Blocks.CRAFTING_TABLE, 300);
            add(map, Blocks.DAYLIGHT_DETECTOR, 300);
            add(map, ItemTags.BANNERS, 300);
            add(map, Items.BOW, 300);
            add(map, Items.FISHING_ROD, 300);
            add(map, Blocks.LADDER, 300);
            add(map, ItemTags.SIGNS, 200);
            add(map, ItemTags.HANGING_SIGNS, 800);
            add(map, Items.WOODEN_SHOVEL, 200);
            add(map, Items.WOODEN_SWORD, 200);
            add(map, Items.WOODEN_HOE, 200);
            add(map, Items.WOODEN_AXE, 200);
            add(map, Items.WOODEN_PICKAXE, 200);
            add(map, ItemTags.WOODEN_DOORS, 200);
            add(map, ItemTags.BOATS, 1200);
            add(map, ItemTags.WOOL, 100);
            add(map, ItemTags.WOODEN_BUTTONS, 100);
            add(map, Items.STICK, 100);
            add(map, ItemTags.SAPLINGS, 100);
            add(map, Items.BOWL, 100);
            add(map, ItemTags.WOOL_CARPETS, 67);
            add(map, Blocks.DRIED_KELP_BLOCK, 4001);
            add(map, Items.CROSSBOW, 300);
            add(map, Blocks.BAMBOO, 50);
            add(map, Blocks.DEAD_BUSH, 100);
            add(map, Blocks.SCAFFOLDING, 50);
            add(map, Blocks.LOOM, 300);
            add(map, Blocks.BARREL, 300);
            add(map, Blocks.CARTOGRAPHY_TABLE, 300);
            add(map, Blocks.FLETCHING_TABLE, 300);
            add(map, Blocks.SMITHING_TABLE, 300);
            add(map, Blocks.COMPOSTER, 300);
            add(map, Blocks.AZALEA, 100);
            add(map, Blocks.FLOWERING_AZALEA, 100);
            add(map, Blocks.MANGROVE_ROOTS, 300);
            lazyLegacyInstance = new SimpleBurningContext(map);
        }
        return lazyLegacyInstance;
    }

    private static final void add(Object2IntMap<Item> map, ItemLike itemLike, int i) {
        var item = itemLike.asItem();
        if (isFurnaceFuel(item))
            map.put(item, i);
    }

    private static final void add(Object2IntMap<Item> map, TagKey<Item> tag, int i) {
        for (var holder : BuiltInRegistries.ITEM.getTagOrEmpty(tag))
            if (isFurnaceFuel(holder.value()))
                map.put(holder.value(), i);
    }

    private static final boolean isFurnaceFuel(Item item) {
        return BuiltInRegistries.ITEM.wrapAsHolder(item).is(ItemTags.NON_FLAMMABLE_WOOD);
    }
}
