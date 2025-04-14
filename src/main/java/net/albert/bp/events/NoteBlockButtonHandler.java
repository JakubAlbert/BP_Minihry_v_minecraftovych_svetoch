package net.albert.bp.events;

import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.UUID;

public class NoteBlockButtonHandler {
    //tlačídlá s rôznymi melodiami
    private static final BlockPos BUTTON_1 = new BlockPos(5, -59, 18);
    private static final BlockPos BUTTON_2 = new BlockPos(5, -59, 20);
    private static final BlockPos BUTTON_3 = new BlockPos(5, -59, 17);
    private static final BlockPos BUTTON_4 = new BlockPos(5, -59, 19);

    //sledovanie progresu
    private static final HashMap<UUID, Integer> playerProgress = new HashMap<>();

    public static boolean onButtonPress(ServerPlayerEntity player, ServerWorld world, BlockPos clickedPos) {
        UUID playerUUID = player.getUuid();
        int progress = playerProgress.getOrDefault(playerUUID, 0);

        // Správna postupnosť tlačidiel
        if (clickedPos.equals(BUTTON_1) && progress == 0) {
            playerProgress.put(playerUUID, 1);
        } else if (clickedPos.equals(BUTTON_2) && progress == 1) {
            playerProgress.put(playerUUID, 2);
        } else if (clickedPos.equals(BUTTON_3) && progress == 2) {
            playerProgress.put(playerUUID, 3);
        } else if (clickedPos.equals(BUTTON_4) && progress == 3) {
            playerProgress.put(playerUUID, 0); // Reset po úspešnom dokončení

            // Hráč dostane  knihy
            player.getInventory().insertStack(new ItemStack(Items.BOOK, 6));


            return true; // Úspešné dokončenie – vypíše "HOTOVO!"
        } else {
            playerProgress.put(playerUUID, 0); // Reset pri nesprávnom tlačidle
        }

        return false; // Ešte nie je hotovo, nič nevypisuje
    }
}
