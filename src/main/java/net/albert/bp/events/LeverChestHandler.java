package net.albert.bp.events;

import net.minecraft.block.Blocks;
import net.minecraft.block.enums.DoubleBlockHalf;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.Items;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import net.minecraft.state.property.Properties;
import net.minecraft.block.BlockState;

public class LeverChestHandler {

    private static final BlockPos CHEST_POS = new BlockPos(30, -60, 1);     // Pozícia truhlice
    private static final BlockPos DOOR_1_POS = new BlockPos(28, -60, 0);    // Prvé dvere (spodná časť)
    private static final BlockPos DOOR_2_POS = new BlockPos(28, -60, -1);   // Druhé dvere (spodná časť)

    //metoda na kontrolu obsahu truhlice
    public static void checkChest(ServerWorld world) {
        if (world.getBlockEntity(CHEST_POS) instanceof ChestBlockEntity chest) {
            Inventory inventory = chest;
            boolean hasLever = false;

            // Skontrolujeme, či sa v truhlici nachádza Lever
            for (int i = 0; i < inventory.size(); i++) {
                if (inventory.getStack(i).getItem() == Items.LEVER) {
                    hasLever = true;
                    break;
                }
            }

            // Otvorenie alebo zatvorenie dverí podľa otoho, či je lever v nej
            setDoorState(world, hasLever);
        }
    }

    private static void setDoorState(ServerWorld world, boolean open) {
        BlockState door1Lower = world.getBlockState(DOOR_1_POS);
        BlockState door2Lower = world.getBlockState(DOOR_2_POS);

        // pozície dverí na otvorenie
        if (door1Lower.isOf(Blocks.IRON_DOOR) && door2Lower.isOf(Blocks.IRON_DOOR)) {
            world.setBlockState(DOOR_1_POS, door1Lower.with(Properties.OPEN, open), 3);
            world.setBlockState(DOOR_2_POS, door2Lower.with(Properties.OPEN, open), 3);

            // Nastavenie hornej polovice dverí
            BlockPos door1UpperPos = DOOR_1_POS.up();
            BlockPos door2UpperPos = DOOR_2_POS.up();

            BlockState door1Upper = world.getBlockState(door1UpperPos);
            BlockState door2Upper = world.getBlockState(door2UpperPos);

            if (door1Upper.isOf(Blocks.IRON_DOOR) && door2Upper.isOf(Blocks.IRON_DOOR)) {
                world.setBlockState(door1UpperPos, door1Upper.with(Properties.OPEN, open), 3);
                world.setBlockState(door2UpperPos, door2Upper.with(Properties.OPEN, open), 3);
            }
        }
    }
}
