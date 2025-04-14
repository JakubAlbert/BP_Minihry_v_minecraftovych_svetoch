package net.albert.bp.events;

import net.albert.bp.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class LeverShadowsHandler {

    private static final BlockPos LEVER_POS = new BlockPos(22, -58, -28); // Pozícia páčky na získanie Book of Shadows

    private static final Set<UUID> playersWhoReceived = new HashSet<>();             // Hráči, ktorí už získali knihu
    private static final Map<UUID, Integer> dialogueTimers = new HashMap<>();        // Dialóg po získaní knihy


    //ziskanie knihy, dialog a overenie aby dostal len 1 krát knihu
    public static void onLeverPulled(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (pos.equals(LEVER_POS) && world.getBlockState(pos).isOf(Blocks.LEVER)) {
            UUID uuid = player.getUuid();

            if (!playersWhoReceived.contains(uuid)) {
                // Hráč dostane Book of Shadows len raz
                player.getInventory().insertStack(new ItemStack(ModItems.BOOK_OF_SHADOWS));
                player.sendMessage(Text.literal("Získal si Book of Shadows!"), false);

                playersWhoReceived.add(uuid);
                dialogueTimers.put(uuid, 0); // Spustenie dialógu
            } else {
                player.sendMessage(Text.literal("Už si získal Book of Shadows."), false);
            }
        }
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

                switch (ticks) {
                    case 40 -> player.sendMessage(Text.literal("Duch: Vidím, že si pochopil moju hádanku. Ako sa cítiš v tmavej miestnosti, za rukou?"), false);
                    case 80 -> player.sendMessage(Text.literal("Player: Cítim sa tam asi tak... ako v malej tmavej miestnosti."), false);
                    case 120 -> player.sendMessage(Text.literal("Player: Už mi konečne povedz, čo s tými knihami."), false);
                    case 160 -> player.sendMessage(Text.literal("Duch: Vôbec sa ti nechce komunikovať. Nevadí."), false);
                    case 200 -> player.sendMessage(Text.literal("Duch: Vidíš v miestnosti tú truhlicu? Ulož do nej všetky knihy v správnom poradí."), false);
                    case 240 -> {
                        player.sendMessage(Text.literal("Duch: Záleží na roku... ale v akom poradí, to ti už nepoviem!"), false);
                        iterator.remove();
                    }
                }


                entry.setValue(ticks + 1);
            }
        });
    }
}
