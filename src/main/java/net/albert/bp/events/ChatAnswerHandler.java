package net.albert.bp.events;

import net.albert.bp.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.item.ItemStack;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ChatAnswerHandler {
    private static final String CORRECT_ANSWER = "6";
    private static final HashMap<UUID, Integer> dialogueTimers = new HashMap<>();//ukladanie tikoveho času

    //registrovanie čítania správ, ak hráč napíše správnú odpoved a nema knihu tak ju dostane a spustí sa dialog
    public static void registerChatListener() {
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            ServerCommandSource source = sender.getCommandSource();
            ServerPlayerEntity player = source.getPlayer();

            if (player != null) {
                String playerMessage = message.getContent().getString().trim();

                // Overenie odpovede a či už hráč nemá knihu
                if (playerMessage.equals(CORRECT_ANSWER) && !dialogueTimers.containsKey(player.getUuid())) {
                    boolean hasBook = player.getInventory().contains(new ItemStack(ModItems.BOOK_OF_WISDOM));

                    if (!hasBook) {
                        // Hráč odpovedal správne a ešte nemá knihu
                        player.getInventory().insertStack(new ItemStack(ModItems.BOOK_OF_WISDOM));
                        player.sendMessage(Text.literal("Získal si Book of Wisdom!"), false);

                        // Spustíme dialóg
                        dialogueTimers.put(player.getUuid(), 0);
                    }
                }
            }

            return true;
        });
    }
    //zobrazovanie správ po tikoch
    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            Iterator<Map.Entry<UUID, Integer>> iterator = dialogueTimers.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID playerId = entry.getKey();
                int ticks = entry.getValue();

                ServerPlayerEntity player = server.getOverworld().getServer().getPlayerManager().getPlayer(playerId);
                if (player == null) {
                    iterator.remove();
                    continue;
                }

                switch (ticks) {
                    case 40 -> player.sendMessage(Text.literal("Duch: Správne. Dávam ti ďalšiu knihu."), false);
                    case 100 -> player.sendMessage(Text.literal("Player: Knihy, samé knihy... Veď ich už nemám kam dať."), false);
                    case 160 -> player.sendMessage(Text.literal("Duch: Neboj sa, o chvíľu sa ich zbavíš! Na dokončenie prvej miestnosti potrebuješ tri knihy."), false);
                    case 220 -> player.sendMessage(Text.literal("Player: Mám dve. Kde nájdem tú tretiu?"), false);
                    case 280 -> {
                        player.sendMessage(Text.literal("Duch: Neviem... To, čo najviac hľadáme, máme často priamo pod rukou. Alebo za rukou? Teoreticky aj v ruke."), false);
                        iterator.remove();
                    }
                }


                entry.setValue(ticks + 1);
            }
        });
    }
}
