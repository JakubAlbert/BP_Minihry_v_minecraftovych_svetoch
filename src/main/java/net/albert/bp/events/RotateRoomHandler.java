package net.albert.bp.events;

import net.albert.bp.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class RotateRoomHandler {

    //pozície dverí
    private static final BlockPos DOOR_POS = new BlockPos(-12, -60, 4);
    private static boolean doorOpened = false;

    private static final BlockPos FINAL_DOOR_POS = new BlockPos(-12, -60, 16);
    private static boolean finalDoorOpened = false;

    //pozicie truhlíc
    private static final BlockPos ICE_CRYSTAL_CHEST = new BlockPos(-8, -60, -22);
    private static final BlockPos SUN_CRYSTAL_CHEST = new BlockPos(-3, -60, -11);
    private static final BlockPos HEAT_CRYSTAL_CHEST = new BlockPos(-18, -60, -2);
    private static final BlockPos BLACK_CRYSTAL_CHEST = new BlockPos(-20, -60, -11);

    // Sleduje, či bol hráčovi zobrazený výpočetný príklad
    private static final Map<UUID, Boolean> playerMessageSent = new HashMap<>();

    //registrácie eventov, tlačidlá, správy, kontrola truhlíc
    public static void register() {
        //výpis správy po kliknutí na dvere
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos clickedPos = hitResult.getBlockPos();
                if (clickedPos.equals(DOOR_POS)) {
                    serverPlayer.sendMessage(Text.literal("Duch: Na odomknutie dverí potrebuješ kód."), false);
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        //reakcie na tlačídlá v kompasovej miestnosti
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos clickedPos = hitResult.getBlockPos();

                if (world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    if (clickedPos.equals(new BlockPos(-12, -59, -5))) {
                        serverPlayer.sendMessage(Text.literal("Duch: Vitaj v kompasovej miestnosti. V strede máš štyri piliere s hádankami. Odpovede píš do chatu malými písmenami."), false);
                        return ActionResult.SUCCESS;
                    } else if (clickedPos.equals(new BlockPos(-11, -59, -10))) {
                        serverPlayer.sendMessage(Text.literal("Duch: Touto svetovou stranou začína deň. Ako sa volá? [Odpoveď uveď do chatu]"), false);
                        return ActionResult.SUCCESS;
                    } else if (clickedPos.equals(new BlockPos(-13, -59, -10))) {
                        serverPlayer.sendMessage(Text.literal("Duch: Svetová strana, ktorá sa vyznačuje tým, že je tam teplejšie. [Odpoveď uveď do chatu]"), false);
                        return ActionResult.SUCCESS;
                    } else if (clickedPos.equals(new BlockPos(-11, -59, -12))) {
                        serverPlayer.sendMessage(Text.literal("Duch: Tu je zima! [Odpoveď uveď do chatu]"), false);
                        return ActionResult.SUCCESS;
                    } else if (clickedPos.equals(new BlockPos(-13, -59, -12))) {
                        serverPlayer.sendMessage(Text.literal("Duch: Táto strana nie je v miestnosti vyobrazená. Ako sa volá? [Odpoveď uveď do chatu]"), false);
                        return ActionResult.SUCCESS;
                    }
                }
      
            }
            return ActionResult.PASS;
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

            //kontrola truhlíc
            boolean hasIce = chestContainsItem(world, ICE_CRYSTAL_CHEST, ModItems.ICE_CRYSTAL);
            boolean hasSun = chestContainsItem(world, SUN_CRYSTAL_CHEST, ModItems.SUN_CRYSTAL);
            boolean hasHeat = chestContainsItem(world, HEAT_CRYSTAL_CHEST, ModItems.HEAT_CRYSTAL);
            boolean hasBlack = chestContainsItem(world, BLACK_CRYSTAL_CHEST, ModItems.BLACK_CRYSTAL);

            for (ServerPlayerEntity player : world.getPlayers()) {
                UUID uuid = player.getUuid();

                boolean allPresent = hasIce && hasSun && hasHeat && hasBlack;
                boolean alreadySent = playerMessageSent.getOrDefault(uuid, false);

                if (allPresent && !alreadySent) {
                    player.sendMessage(Text.literal("Duch: Super. Ešte vyrátaj tento príklad výsledok použi ako kód k posledným dverám. 100*1+5+41+10"), false);
                    playerMessageSent.put(uuid, true);
                } else if (!allPresent) {
                    playerMessageSent.put(uuid, false); //reset ak crystals zmiznu
                }
            }
        });

        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            ServerPlayerEntity player = sender.getCommandSource().getPlayer();
            if (player != null) {
                String content = message.getContent().getString().trim();

                //odokmnutie dverí cez číselný kód
                if (!doorOpened && content.equals("35")) {
                    ServerWorld world = player.getServerWorld();

                    BlockState doorState = Blocks.IRON_DOOR.getDefaultState()
                            .with(Properties.HORIZONTAL_FACING, world.getBlockState(DOOR_POS).get(Properties.HORIZONTAL_FACING))
                            .with(Properties.OPEN, true)
                            .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);

                    world.setBlockState(DOOR_POS, doorState);
                    player.sendMessage(Text.literal("Duch: Správne, dvere sú otvorené."), false);
                    doorOpened = true;
                }

                if (!finalDoorOpened && content.equals("156")) {
                    ServerWorld world = player.getServerWorld();

                    BlockState doorState = Blocks.IRON_DOOR.getDefaultState()
                            .with(Properties.HORIZONTAL_FACING, world.getBlockState(FINAL_DOOR_POS).get(Properties.HORIZONTAL_FACING))
                            .with(Properties.OPEN, true)
                            .with(Properties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);

                    world.setBlockState(FINAL_DOOR_POS, doorState);
                    player.sendMessage(Text.literal("Duch: Posledné dvere sú otvorené!"), false);
                    finalDoorOpened = true;
                }

                //zisk crystals na zaklade kľúčového slova
                switch (content) {
                    case "západ", "zapad" -> {
                        if (!player.getInventory().contains(new ItemStack(ModItems.BLACK_CRYSTAL))) {
                            player.getInventory().insertStack(new ItemStack(ModItems.BLACK_CRYSTAL));
                            player.sendMessage(Text.literal("Získal si Black Crystal!"), false);
                        }
                    }
                    case "sever" -> {
                        if (!player.getInventory().contains(new ItemStack(ModItems.ICE_CRYSTAL))) {
                            player.getInventory().insertStack(new ItemStack(ModItems.ICE_CRYSTAL));
                            player.sendMessage(Text.literal("Získal si Ice Crystal!"), false);
                        }
                    }
                    case "vychod", "východ" -> {
                        if (!player.getInventory().contains(new ItemStack(ModItems.SUN_CRYSTAL))) {
                            player.getInventory().insertStack(new ItemStack(ModItems.SUN_CRYSTAL));
                            player.sendMessage(Text.literal("Získal si Sun Crystal!"), false);
                        }
                    }
                    case "juh" -> {
                        if (!player.getInventory().contains(new ItemStack(ModItems.HEAT_CRYSTAL))) {
                            player.getInventory().insertStack(new ItemStack(ModItems.HEAT_CRYSTAL));
                            player.sendMessage(Text.literal("Získal si Heat Crystal!"), false);
                        }
                    }
                }
            }
            return true;
        });
    }
//kontrola truhlíc
    private static boolean chestContainsItem(ServerWorld world, BlockPos pos, Item item) {
        if (world.getBlockEntity(pos) instanceof Inventory inventory) {
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty() && stack.getItem().equals(item)) {
                    return true;
                }
            }
        }
        return false;
    }
}
