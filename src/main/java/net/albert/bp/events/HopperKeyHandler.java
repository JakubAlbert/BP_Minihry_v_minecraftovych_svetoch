package net.albert.bp.events;

import net.albert.bp.item.ModItems;
import net.minecraft.block.BlockState;
import net.minecraft.block.DoorBlock;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class HopperKeyHandler {

    private static final BlockPos HOPPER_POS = new BlockPos(42, -60, -26); // Súradnice hopperu v hrobke
    private static final BlockPos DOOR_POS = new BlockPos(40, -60, -21);   // Súradnice železných dverí

    // Vyžadované kľúče
    private static final Set<Item> REQUIRED_KEYS = Set.of(
            ModItems.PIRATE_KEY,
            ModItems.GOLDEN_KEY,
            ModItems.BLUE_SKULL_KEY,
            ModItems.ORANGE_SKULL_KEY
    );

    // Sledujeme, ktorí hráči už dvere otvorili
    private static final Set<UUID> playersWhoOpenedDoor = new HashSet<>();

    public static void checkHopper(ServerWorld world) {
        if (!(world.getBlockEntity(HOPPER_POS) instanceof HopperBlockEntity hopper)) return;

        Inventory inventory = hopper;
        Set<Item> foundKeys = new HashSet<>();

        for (int i = 0; i < inventory.size(); i++) {
            ItemStack stack = inventory.getStack(i);
            if (!stack.isEmpty() && REQUIRED_KEYS.contains(stack.getItem())) {
                foundKeys.add(stack.getItem());
            }
        }

        // Ak sú všetky kľúče – otvoríme dvere
        if (foundKeys.containsAll(REQUIRED_KEYS)) {
            openDoor(world);
        } else {
            closeDoor(world);
        }
    }

    //otvorenie dverí
    private static void openDoor(ServerWorld world) {
        BlockState doorState = world.getBlockState(DOOR_POS);
        if (doorState.getBlock() instanceof DoorBlock) {
            world.setBlockState(DOOR_POS, doorState.with(DoorBlock.OPEN, true));
        }
    }
    //zatorenie dverí
    private static void closeDoor(ServerWorld world) {
        BlockState doorState = world.getBlockState(DOOR_POS);
        if (doorState.getBlock() instanceof DoorBlock) {
            world.setBlockState(DOOR_POS, doorState.with(DoorBlock.OPEN, false));
        }
    }
}
