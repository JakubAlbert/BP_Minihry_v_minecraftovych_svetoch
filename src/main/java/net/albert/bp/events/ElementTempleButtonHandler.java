package net.albert.bp.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.block.Blocks;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.ActionResult;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ElementTempleButtonHandler {
    private static final BlockPos BUTTON_POS_1 = new BlockPos(33, -59, -12); // Prvé tlačidlo
    private static final BlockPos BUTTON_POS_2 = new BlockPos(42, -58, 2); // Druhé tlačidlo
    private static final BlockPos BUTTON_POS_3 = new BlockPos(20, -59, 0); // Tlačidlo pre lektvary
    private static final BlockPos BUTTON_POS_4 = new BlockPos(36, -59, 1); // Tlačidlo pre železo a vodu
    private static final BlockPos BUTTON_POS_5 = new BlockPos(33, -58, 23); // Tlačidlo pre štetec a piesok
    private static final BlockPos BUTTON_POS_6 = new BlockPos(-2, -59, 10); // tlačidlo pre miestnosť s iteam frame lockom

    private static final HashMap<UUID, Integer> dialogueTimers = new HashMap<>(); // Dialógy pre hráčov

    public static void registerButtonEvent() {
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos clickedPos = hitResult.getBlockPos();

                // Overíme, ktoré tlačidlo bolo stlačené a spustíme dialóg
                if (clickedPos.equals(BUTTON_POS_1) && world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    dialogueTimers.put(serverPlayer.getUuid(), 0);
                    return ActionResult.SUCCESS;
                }
                if (clickedPos.equals(BUTTON_POS_2) && world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    dialogueTimers.put(serverPlayer.getUuid(), 1000);
                    return ActionResult.SUCCESS;
                }
                if (clickedPos.equals(BUTTON_POS_3) && world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    serverPlayer.sendMessage(Text.literal("Duch: Nájdi a vlož všetky lektvary. Hlavne ich nepi!"), false);
                    return ActionResult.SUCCESS;
                }

                if (clickedPos.equals(BUTTON_POS_4) && world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    serverPlayer.sendMessage(Text.literal("Duch: Nájdi všetky železá, vyrob si kyblík a naber vodu."), false);
                    return ActionResult.SUCCESS;
                }

                if (clickedPos.equals(BUTTON_POS_5) && world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    serverPlayer.sendMessage(Text.literal("Duch: Nájdi štetec a opráš nejaký zvláštny piesok. Možno niečo objavíš."), false);
                    return ActionResult.SUCCESS;
                }

                if (clickedPos.equals(BUTTON_POS_6) && world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    serverPlayer.sendMessage(Text.literal("Duch: Otoč itemy do správnej polohy. Jeden ti zasvieti vtedy, keď je správne. Na zvyšok musíš prísť sám."), false);
                    return ActionResult.SUCCESS;
                }

            }
            return ActionResult.PASS;
        });
    }


    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

            Iterator<Map.Entry<UUID, Integer>> iterator = dialogueTimers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID uuid = entry.getKey();
                int ticks = entry.getValue();

                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player == null) continue;

                if (ticks < 1000) {
                    switch (ticks) {
                        case 40 -> player.sendMessage(Text.literal("Duch: Vitaj v chráme troch elementov – voda, zem a oheň."), false);
                        case 100 -> player.sendMessage(Text.literal("Duch: Zem a oheň sú zatvorené. Musíš prísť na to, ako ich odomknúť."), false);
                        case 160 -> {
                            player.sendMessage(Text.literal("Duch: Možno ti niekde nechám nápovedu..."), false);
                            iterator.remove();
                        }
                    }
                } else {
                    switch (ticks) {
                        case 1040 -> player.sendMessage(Text.literal("Duch: Vidím crafting table a pole, ktoré nie je celé poorané."), false);
                        case 1100 -> iterator.remove();
                    }
                }


                entry.setValue(ticks + 1);
            }
        });
    }
}