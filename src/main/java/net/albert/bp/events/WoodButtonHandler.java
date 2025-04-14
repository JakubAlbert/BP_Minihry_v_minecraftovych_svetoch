package net.albert.bp.events;

import net.minecraft.block.Blocks;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.Items;

public class WoodButtonHandler {
    private static final BlockPos BUTTON_POS = new BlockPos(-1, -62, 9); // Pozícia dreveného tlačidla
    private static final BlockPos CHEST_POS = new BlockPos(-2, -63, 9); // Pozícia truhlice s leverom
    private static final BlockPos TRAPDOOR_POS = new BlockPos(-2, -60, 10); // Pozícia železného poklopu

    //reakcia na stlačenie tlačidla, informovanie hráča
    public static void onButtonPress(ServerPlayerEntity player, ServerWorld world, BlockPos clickedPos) {
        if (clickedPos.equals(BUTTON_POS) && world.getBlockState(clickedPos).isOf(Blocks.OAK_BUTTON)) {
            player.sendMessage(Text.literal("Duch: Poklop je zatvorený, asi si zabudol páčku na otvorenie. Nájdi ju a daj do truhlice."), false);
        }
    }

    //kontrola obsahu truhlice, či obsahuje páčku
    public static void checkChest(ServerWorld world) {
        if (world.getBlockEntity(CHEST_POS) instanceof ChestBlockEntity chest) {
            Inventory inventory = (Inventory) chest;
            boolean hasLever = false;

            // skontrolujeme, či sa v truhlici nachádza lever
            for (int i = 0; i < inventory.size(); i++) {
                ItemStack stack = inventory.getStack(i);
                if (!stack.isEmpty() && stack.getItem() == Items.LEVER) {
                    hasLever = true;
                    break;
                }
            }

            // ak je lever v truhlici, otvoríme poklop
            if (hasLever) {
                world.setBlockState(TRAPDOOR_POS, world.getBlockState(TRAPDOOR_POS).with(Properties.OPEN, true));
            } else {
                // ak lever zmizne, zatvoríme poklop
                world.setBlockState(TRAPDOOR_POS, world.getBlockState(TRAPDOOR_POS).with(Properties.OPEN, false));
            }
        }
    }
}
