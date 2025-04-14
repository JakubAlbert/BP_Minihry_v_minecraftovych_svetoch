package net.albert.bp.events;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import net.albert.bp.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.*;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.vehicle.ChestMinecartEntity;
import net.minecraft.entity.vehicle.MinecartEntity;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.block.Blocks;
import net.minecraft.state.property.Properties;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.util.math.Box;
import net.minecraft.util.Identifier;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.util.math.Direction;

import java.util.List;


public class ResetRoomCommand {
    //kompletný reset minihry
    public static void registerCommand(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("resetroom")
                .requires(source -> source.hasPermissionLevel(2))
                .executes(context -> {
                    ServerCommandSource source = context.getSource();
                    ServerWorld world = source.getWorld();

                    resetRoom(world);

                    source.sendFeedback(() -> Text.literal("✅ Miestnosť bola resetovaná!"), true);
                    return Command.SINGLE_SUCCESS;
                }));
    }

    // Hlavná metóda, ktorá vykoná reset všetkých herných prvkov v miestnosti
    public static void resetRoom(ServerWorld world) {
        //  Vyprázdnime truhlice a hoppery
        clearInventory(world, new BlockPos(4, -60, -25)); // Chestka
        clearInventory(world, new BlockPos(42, -60, -26)); // Hopper
        clearInventory(world, new BlockPos(30, -60, 1)); // Chestka

        // Zbúrame Redstone
        BlockPos redstoneToRemove = new BlockPos(20, -59, -22);
        if (world.getBlockState(redstoneToRemove).isOf(Blocks.REDSTONE_WIRE)) {
            world.setBlockState(redstoneToRemove, Blocks.AIR.getDefaultState());
        }
        rotateSoulTorchesLeft(world);


        // Reset Armor Stand
        resetArmorStand(world, new BlockPos(41, -60, -29));

        // Naplníme Cauldron vodou
        BlockPos cauldronPos = new BlockPos(37, -60, 1);
        if (world.getBlockState(cauldronPos).isOf(Blocks.CAULDRON)) {
            world.setBlockState(cauldronPos, Blocks.WATER_CAULDRON.getDefaultState().with(Properties.LEVEL_3, 3));
        }

        //Uhasíme oheň
        BlockPos firePos = new BlockPos(23, -59, 5);
        if (world.getBlockState(firePos).isOf(Blocks.FIRE)) {
            world.setBlockState(firePos, Blocks.AIR.getDefaultState());
        }

        //Nastavenie Suspicious Sand s lootom
        setSuspiciousSandWithLoot(world, new BlockPos(36, -60, 20), "suspicious_sand_emerald"); // 💎 Emerald
        setSuspiciousSandWithLoot(world, new BlockPos(31, -60, 21), "suspicious_sand_iron"); // ⚙️ Iron Ingot

        //Pridanie Fire Resistance Potion do sudov
        addRenamedPotion(world, new BlockPos(26, -62, 5));
        addRenamedPotion(world, new BlockPos(20, -62, -7));
        addRenamedPotion(world, new BlockPos(20, -60, 10));

        //Vymazanie všetkých potionov z Brewing Stand
        clearBrewingStand(world, new BlockPos(20, -60, 0));

        //Doplníme klúče do truhlíc a sudov

        addItems(world);

        //vyčistenie od itemov vyhodených vo svete

        clearDroppedItems(world);

        //zatvorenie dverí
        closeBothDoors(world);

        //minecart spawn
        spawnMinecartWithChest(world, new BlockPos(29, -60, 11));

        //vyčistenie inventáru

        for (ServerPlayerEntity player : world.getPlayers()) {
            clearPlayerInventory(player);
        }

        handleLeverChestFishing(world);

        //Vyčistenie chiseled bookshelf
        resetChiseledBookshelf(world, new BlockPos(-1, -58, 19));


        //Doplníme itemy do dispenserov
        addItemToDispenser(world, new BlockPos(46, -50, -26), new ItemStack(ModItems.ORANGE_SKULL_KEY, 1));

        ItemStack flintAndSteel = new ItemStack(Items.FLINT_AND_STEEL, 1);
        flintAndSteel.setDamage(63); // Nastavenie tak, aby mal len 1 použitie
        addItemToDispenser(world, new BlockPos(23, -50, 5), flintAndSteel);
        BlockPos dirtPos = new BlockPos(50, -61, -7);
        world.setBlockState(dirtPos, Blocks.DIRT.getDefaultState());
    }
    private static void handleLeverChestFishing(ServerWorld world) {
        //Doplníme Lever do sudu
        BlockPos barrelPosLever = new BlockPos(-1, -63, 17);
        if (world.getBlockEntity(barrelPosLever) instanceof Inventory inventoryLever) {
            inventoryLever.setStack(0, new ItemStack(Items.LEVER));
        }

        // Vymažeme obsah truhlice
        BlockPos chestClearPos = new BlockPos(-2, -63, 9);
        if (world.getBlockEntity(chestClearPos) instanceof Inventory inventoryClear) {
            for (int i = 0; i < inventoryClear.size(); i++) {
                inventoryClear.setStack(i, ItemStack.EMPTY);
            }
        }
        //last room
        BlockPos chestClearPos2 = new BlockPos(-8, -60, -22);
        if (world.getBlockEntity(chestClearPos2) instanceof Inventory inventoryClear) {
            for (int i = 0; i < inventoryClear.size(); i++) {
                inventoryClear.setStack(i, ItemStack.EMPTY);
            }
        }
        BlockPos chestClearPos3 = new BlockPos(-3, -60, -11);
        if (world.getBlockEntity(chestClearPos3) instanceof Inventory inventoryClear) {
            for (int i = 0; i < inventoryClear.size(); i++) {
                inventoryClear.setStack(i, ItemStack.EMPTY);
            }
        }
        BlockPos chestClearPos4 = new BlockPos(-18, -60, -2);
        if (world.getBlockEntity(chestClearPos4) instanceof Inventory inventoryClear) {
            for (int i = 0; i < inventoryClear.size(); i++) {
                inventoryClear.setStack(i, ItemStack.EMPTY);
            }
        }
        BlockPos chestClearPos5 = new BlockPos(-20, -60, -11);
        if (world.getBlockEntity(chestClearPos5) instanceof Inventory inventoryClear) {
            for (int i = 0; i < inventoryClear.size(); i++) {
                inventoryClear.setStack(i, ItemStack.EMPTY);
            }
        }

        // Doplníme Fishing Rod do Barellu
        BlockPos barrelPosRod = new BlockPos(50, -61, -8);
        if (world.getBlockEntity(barrelPosRod) instanceof Inventory inventoryRod) {
            inventoryRod.setStack(0, new ItemStack(Items.FISHING_ROD));
        }
    }

    //čistenie inventáru
    private static void clearInventory(ServerWorld world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof Inventory inventory) {
            for (int i = 0; i < inventory.size(); i++) {
                inventory.setStack(i, ItemStack.EMPTY);
            }
        }
    }

    //pridanie predmetov do dispenserov
    private static void addItemToDispenser(ServerWorld world, BlockPos pos, ItemStack itemStack) {
        if (world.getBlockEntity(pos) instanceof DispenserBlockEntity dispenser) {
            Inventory inventory = (Inventory) dispenser;
            inventory.setStack(0, itemStack);
        }
    }


    // vyprázdnenie knižnice
    private static void resetChiseledBookshelf(ServerWorld world, BlockPos pos) {
        world.setBlockState(pos, Blocks.AIR.getDefaultState()); // Odstráni existujúci blok
        world.setBlockState(pos, Blocks.CHISELED_BOOKSHELF.getDefaultState().with(Properties.HORIZONTAL_FACING, Direction.EAST)); // Otočí blok smerom na juh (k hráčovi)
    }

    //umiestnenie armor standu
    private static void resetArmorStand(ServerWorld world, BlockPos pos) {
        List<ArmorStandEntity> armorStands = world.getEntitiesByClass(ArmorStandEntity.class, new Box(pos), entity -> true);
        for (ArmorStandEntity armorStand : armorStands) {
            armorStand.discard();
        }

        ArmorStandEntity newArmorStand = new ArmorStandEntity(world, pos.getX(), pos.getY(), pos.getZ());
        newArmorStand.setYaw(271f);
        newArmorStand.equipStack(EquipmentSlot.MAINHAND, new ItemStack(ModItems.BLUE_SKULL_KEY));
        newArmorStand.equipStack(EquipmentSlot.FEET, new ItemStack(Items.NETHERITE_BOOTS));
        newArmorStand.equipStack(EquipmentSlot.LEGS, new ItemStack(Items.NETHERITE_LEGGINGS));
        newArmorStand.equipStack(EquipmentSlot.CHEST, new ItemStack(Items.NETHERITE_CHESTPLATE));
        newArmorStand.equipStack(EquipmentSlot.HEAD, new ItemStack(Items.SKELETON_SKULL));
        world.spawnEntity(newArmorStand);
    }

    //umiestnenie piesku
    private static void setSuspiciousSandWithLoot(ServerWorld world, BlockPos pos, String lootTableName) {
        world.setBlockState(pos, Blocks.SUSPICIOUS_SAND.getDefaultState());

        if (world.getBlockEntity(pos) instanceof BrushableBlockEntity sandEntity) {
            Identifier customLootTable = new Identifier("bpalbert", "gameplay/" + lootTableName);
            sandEntity.setLootTable(customLootTable, world.random.nextLong());
        }
    }

    private static void addRenamedPotion(ServerWorld world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof Inventory inventory) {
            ItemStack potion = new ItemStack(Items.POTION, 1);

            // Nastavenie typu lektvaru - Fire Resistance (3 min)
            NbtCompound tag = potion.getOrCreateNbt();
            NbtCompound display = new NbtCompound();
            display.putString("Name", "{\"text\":\"Nepiť!\",\"color\":\"red\"}");
            tag.put("display", display);

            // Nastavenie efektu Fire Resistance na 3 minúty
            NbtCompound potionTag = new NbtCompound();
            potionTag.putString("Potion", "minecraft:fire_resistance");
            tag.put("tag", potionTag);

            potion.setNbt(tag);
            inventory.setStack(0, potion);
        }
    }

    private static void clearDroppedItems(ServerWorld world) {
        // Definovanie oblasti, kde budeme hľadať entity
        Box searchBox = new Box(-3000, -3000, -3000, 3000, 3000, 3000);

        // Odstránenie všetkých dropnutých predmetov
        List<ItemEntity> droppedItems = world.getEntitiesByClass(ItemEntity.class, searchBox, entity -> true);
        for (ItemEntity item : droppedItems) {
            item.discard();
        }

        // Odstránenie všetkých vozíkov (normálne, s chestkou, hopperom atď.)
        List<ChestMinecartEntity> minecarts = world.getEntitiesByClass(ChestMinecartEntity.class, searchBox, entity -> true);
        for (ChestMinecartEntity minecart : minecarts) {
            minecart.discard();
        }
    }


    private static void spawnMinecartWithChest(ServerWorld world, BlockPos pos) {
        // Vytvorenie Minecart with Chest
        ChestMinecartEntity minecart = new ChestMinecartEntity(EntityType.CHEST_MINECART, world);

        // Nastavenie polohy na požadované súradnice
        minecart.setPosition(pos.getX() + 0.5, pos.getY() + 0.0625, pos.getZ() + 0.5);

        // Pridanie Leveru do inventára
        Inventory inventory = (Inventory) minecart;
        inventory.setStack(0, new ItemStack(Items.LEVER, 1)); // 1× Lever

        // Spawne Minecart vo svete
        world.spawnEntity(minecart);
    }



    //vyčistenie brewing standu
    private static void clearBrewingStand(ServerWorld world, BlockPos pos) {
        if (world.getBlockEntity(pos) instanceof BrewingStandBlockEntity brewingStand) {
            for (int i = 0; i < brewingStand.size(); i++) {
                brewingStand.setStack(i, ItemStack.EMPTY);
            }
        }

    }

    //čistenie inventáru
    private static void clearPlayerInventory(ServerPlayerEntity player) {
        if (player != null) {
            player.getInventory().clear();
        }
    }


    //pridávanie predmetov do sudov
private static void addItems(ServerWorld world) {
    //kľúče
    BlockPos posChestPK = new BlockPos(51, -63, -31);
    if (world.getBlockEntity(posChestPK) instanceof Inventory inventoryChest) {
        inventoryChest.setStack(0, new ItemStack(ModItems.PIRATE_KEY, 1));
    }
    BlockPos posBarrelGK= new BlockPos(48, -56, -35);
    if (world.getBlockEntity(posBarrelGK) instanceof Inventory inventoryBarrel) {
        int lastSlot = inventoryBarrel.size() - 1;
        inventoryBarrel.setStack(lastSlot, new ItemStack(ModItems.GOLDEN_KEY, 1));
    }
    //emerald
    BlockPos posBarrel = new BlockPos(39, -61, 16);
    if (world.getBlockEntity(posBarrel) instanceof Inventory inventoryBarrel) {
        int lastSlot = inventoryBarrel.size() - 1;
        inventoryBarrel.setStack(lastSlot, new ItemStack(Items.EMERALD, 1));
    }

    // Iron ingot do truhlice
    BlockPos posChest = new BlockPos(37, -60, 8);
    if (world.getBlockEntity(posChest) instanceof Inventory inventoryChest) {
        inventoryChest.setStack(0, new ItemStack(Items.IRON_INGOT, 1));
    }


    //štetec do sudu
    BlockPos posBarrelBrush = new BlockPos(30, -61, 22);
    if (world.getBlockEntity(posBarrelBrush) instanceof Inventory inventoryBrush) {
        inventoryBrush.setStack(0, new ItemStack(Items.BRUSH, 1));
    }
    //paličky
    BlockPos posBarrelEarth1 = new BlockPos(47, -61, 1);
    if (world.getBlockEntity(posBarrelEarth1) instanceof Inventory inventoryEarth1) {
        inventoryEarth1.setStack(0, new ItemStack(Items.STICK, 2));
    }
    //kameň
    BlockPos posBarrelEarth2 = new BlockPos(53, -60, 8);
    if (world.getBlockEntity(posBarrelEarth2) instanceof Inventory inventoryEarth2) {
        inventoryEarth2.setStack(0, new ItemStack(Items.COBBLESTONE, 1));
    }
    //kameň
    BlockPos posBarrelEarth3 = new BlockPos(50, -62, -3);
    if (world.getBlockEntity(posBarrelEarth3) instanceof Inventory inventoryEarth3) {
        inventoryEarth3.setStack(0, new ItemStack(Items.COBBLESTONE, 1));
    }

}
    private static void closeBothDoors(ServerWorld world) {
        // Dvere 1
        BlockPos door1Pos = new BlockPos(-12, -60, 4);
        BlockState door1State = Blocks.IRON_DOOR.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, world.getBlockState(door1Pos).get(Properties.HORIZONTAL_FACING))
                .with(Properties.OPEN, false)
                .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        world.setBlockState(door1Pos, door1State);

        // Dvere 2
        BlockPos door2Pos = new BlockPos(-12, -60, 16);
        BlockState door2State = Blocks.IRON_DOOR.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, world.getBlockState(door2Pos).get(Properties.HORIZONTAL_FACING))
                .with(Properties.OPEN, false)
                .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        world.setBlockState(door2Pos, door2State);

        //Dvere 3
        BlockPos door3Pos = new BlockPos(-12, -60, 18);
        BlockState door3State = Blocks.IRON_DOOR.getDefaultState()
                .with(Properties.HORIZONTAL_FACING, world.getBlockState(door2Pos).get(Properties.HORIZONTAL_FACING))
                .with(Properties.OPEN, false)
                .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        world.setBlockState(door3Pos, door3State);
    }

    //tmena rotácie faklí v item framoch
    private static void rotateSoulTorchesLeft(ServerWorld world) {
        BlockPos[] positions = {
                new BlockPos(-14, -59, 8),
                new BlockPos(-14, -59, 10),
                new BlockPos(-14, -59, 12)
        };

        for (BlockPos pos : positions) {
            List<Entity> entities = world.getOtherEntities(null, new Box(pos));

            for (Entity entity : entities) {
                if (entity instanceof ItemFrameEntity itemFrame && itemFrame.getHeldItemStack().getItem() == Items.SOUL_TORCH) {
                    int currentRotation = itemFrame.getRotation();
                    int newRotation = (currentRotation + 5) % 8; // otočenie o 5
                    itemFrame.setRotation(newRotation);
                }
            }
        }
    }




}

