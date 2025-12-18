package niv.burning.impl;

import java.util.function.Supplier;

import com.google.common.base.Suppliers;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntMaps;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ItemLike;
import net.minecraft.world.level.block.Blocks;
import niv.burning.api.BurningContext;

final class LegacyBurningContext implements BurningContext {

    static final Supplier<LegacyBurningContext> LAZY_SINGLETON = Suppliers.memoize(LegacyBurningContext::new);

    private final Object2IntMap<Item> map;

    private LegacyBurningContext() {
        this.map = Object2IntMaps.unmodifiable(legacyMap());
    }

    @Override
    public boolean isFuel(Item item) {
        return this.map.containsKey(item);
    }

    @Override
    public boolean isFuel(ItemStack itemStack) {
        return !itemStack.isEmpty() && this.map.containsKey(itemStack.getItem());
    }

    @Override
    public int burnDuration(Item item) {
        return this.map.getOrDefault(item, 0);
    }

    @Override
    public int burnDuration(ItemStack itemStack) {
        return itemStack.isEmpty() ? 0 : this.map.getOrDefault(itemStack.getItem(), 0);
    }

    private static final Object2IntMap<Item> legacyMap() {
        var result = new Object2IntOpenHashMap<Item>();
        add(result, Items.LAVA_BUCKET, 20000);
        add(result, Blocks.COAL_BLOCK, 16000);
        add(result, Items.BLAZE_ROD, 2400);
        add(result, Items.COAL, 1600);
        add(result, Items.CHARCOAL, 1600);
        add(result, ItemTags.LOGS, 300);
        add(result, ItemTags.BAMBOO_BLOCKS, 300);
        add(result, ItemTags.PLANKS, 300);
        add(result, Blocks.BAMBOO_MOSAIC, 300);
        add(result, ItemTags.WOODEN_STAIRS, 300);
        add(result, Blocks.BAMBOO_MOSAIC_STAIRS, 300);
        add(result, ItemTags.WOODEN_SLABS, 150);
        add(result, Blocks.BAMBOO_MOSAIC_SLAB, 150);
        add(result, ItemTags.WOODEN_TRAPDOORS, 300);
        add(result, ItemTags.WOODEN_PRESSURE_PLATES, 300);
        add(result, ItemTags.WOODEN_FENCES, 300);
        add(result, ItemTags.FENCE_GATES, 300);
        add(result, Blocks.NOTE_BLOCK, 300);
        add(result, Blocks.BOOKSHELF, 300);
        add(result, Blocks.CHISELED_BOOKSHELF, 300);
        add(result, Blocks.LECTERN, 300);
        add(result, Blocks.JUKEBOX, 300);
        add(result, Blocks.CHEST, 300);
        add(result, Blocks.TRAPPED_CHEST, 300);
        add(result, Blocks.CRAFTING_TABLE, 300);
        add(result, Blocks.DAYLIGHT_DETECTOR, 300);
        add(result, ItemTags.BANNERS, 300);
        add(result, Items.BOW, 300);
        add(result, Items.FISHING_ROD, 300);
        add(result, Blocks.LADDER, 300);
        add(result, ItemTags.SIGNS, 200);
        add(result, ItemTags.HANGING_SIGNS, 800);
        add(result, Items.WOODEN_SHOVEL, 200);
        add(result, Items.WOODEN_SWORD, 200);
        add(result, Items.WOODEN_HOE, 200);
        add(result, Items.WOODEN_AXE, 200);
        add(result, Items.WOODEN_PICKAXE, 200);
        add(result, ItemTags.WOODEN_DOORS, 200);
        add(result, ItemTags.BOATS, 1200);
        add(result, ItemTags.WOOL, 100);
        add(result, ItemTags.WOODEN_BUTTONS, 100);
        add(result, Items.STICK, 100);
        add(result, ItemTags.SAPLINGS, 100);
        add(result, Items.BOWL, 100);
        add(result, ItemTags.WOOL_CARPETS, 67);
        add(result, Blocks.DRIED_KELP_BLOCK, 4001);
        add(result, Items.CROSSBOW, 300);
        add(result, Blocks.BAMBOO, 50);
        add(result, Blocks.DEAD_BUSH, 100);
        add(result, Blocks.SCAFFOLDING, 50);
        add(result, Blocks.LOOM, 300);
        add(result, Blocks.BARREL, 300);
        add(result, Blocks.CARTOGRAPHY_TABLE, 300);
        add(result, Blocks.FLETCHING_TABLE, 300);
        add(result, Blocks.SMITHING_TABLE, 300);
        add(result, Blocks.COMPOSTER, 300);
        add(result, Blocks.AZALEA, 100);
        add(result, Blocks.FLOWERING_AZALEA, 100);
        add(result, Blocks.MANGROVE_ROOTS, 300);
        return result;
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
