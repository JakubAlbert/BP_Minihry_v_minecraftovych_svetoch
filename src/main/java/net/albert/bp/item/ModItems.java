package net.albert.bp.item;

import net.fabricmc.fabric.api.item.v1.FabricItemSettings;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroupEntries;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroups;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.util.Identifier;
import net.albert.bp.BakalarskaPracaAlbert;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;

public class ModItems {
    // ✅ Vytvorenie nových kníh

    public static final Item BOOK_OF_LIGHT = registerItem("book_of_light", new Item(new  FabricItemSettings()));
    public static final Item BOOK_OF_SHADOWS = registerItem("book_of_shadows", new Item(new  FabricItemSettings()));
    public static final Item BOOK_OF_WISDOM = registerItem("book_of_wisdom", new Item(new  FabricItemSettings()));
    public static final Item PIRATE_KEY = registerItem("pirate_key", new Item(new FabricItemSettings()));
    public static final Item GOLDEN_KEY = registerItem("golden_key", new Item(new FabricItemSettings()));
    public static final Item BLUE_SKULL_KEY = registerItem("blue_skull_key", new Item(new FabricItemSettings()));
    public static final Item ORANGE_SKULL_KEY = registerItem("orange_skull_key", new Item(new FabricItemSettings()));

    public static final Item BLACK_CRYSTAL = registerItem("black_crystal", new Item(new FabricItemSettings()));
    public static final Item ICE_CRYSTAL = registerItem("ice_crystal", new Item(new FabricItemSettings()));
    public static final Item HEAT_CRYSTAL = registerItem("heat_crystal", new Item(new FabricItemSettings()));
    public static final Item SUN_CRYSTAL = registerItem("sun_crystal", new Item(new FabricItemSettings()));

    // ✅ Pridanie kníh do kreatívneho inventára (Ingredients Tab)
    private static void addItemsToIngredientTabItemGroup(FabricItemGroupEntries entries) {
        entries.add(BOOK_OF_LIGHT);
        entries.add(BOOK_OF_SHADOWS);
        entries.add(BOOK_OF_WISDOM);
        entries.add(PIRATE_KEY);
        entries.add(GOLDEN_KEY);
        entries.add(BLUE_SKULL_KEY);
        entries.add(ORANGE_SKULL_KEY);
        entries.add(BLACK_CRYSTAL);
        entries.add(ICE_CRYSTAL);
        entries.add(HEAT_CRYSTAL);
        entries.add(SUN_CRYSTAL);
    }

    // ✅ Registrácia itemov v Minecraft registri
    private static Item registerItem(String name, Item item) {
        return Registry.register(Registries.ITEM, new Identifier(BakalarskaPracaAlbert.MOD_ID, name), item);
    }

    // ✅ Registrácia itemov a pridanie do inventára
    public static void registerModItems() {
        BakalarskaPracaAlbert.LOGGER.info("Registering Mod Items for " + BakalarskaPracaAlbert.MOD_ID);
        ItemGroupEvents.modifyEntriesEvent(ItemGroups.INGREDIENTS).register(ModItems::addItemsToIngredientTabItemGroup);
    }
}
