package net.albert.bp.events;

import net.albert.bp.item.ModItems;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.block.entity.ChestBlockEntity;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.UUID;

public class BookPuzzleHandler {
    private static final HashMap<UUID, Boolean> playerPuzzleState = new HashMap<>(); //uchovanie informácie, či hráč vyriešil hádanku
    private static final HashMap<UUID, Integer> dialogueTimers = new HashMap<>(); // Časovač pre zobrazovanie dialógov
    private static final HashMap<UUID, Integer> teleportTimers = new HashMap<>(); // Časovač pre oneskorenú teleportáciu
    private static final BlockPos CHEST_POS = new BlockPos(4, -60, -25); // Pozícia truhlice

    //overenie obsahu truhlice
    public static void checkChest(ServerPlayerEntity player, ServerWorld world) {
        if (world.getBlockEntity(CHEST_POS) instanceof ChestBlockEntity chest) {
            Inventory inventory = chest;

            // Získanie itemov z prvých 3 slotov
            ItemStack slot1 = inventory.getStack(0);
            ItemStack slot2 = inventory.getStack(1);
            ItemStack slot3 = inventory.getStack(2);

            //Overenie správnosti poradia kníh
            boolean lightCorrect = !slot1.isEmpty() && slot1.getItem() == ModItems.BOOK_OF_LIGHT;
            boolean shadowsCorrect = !slot2.isEmpty() && slot2.getItem() == ModItems.BOOK_OF_SHADOWS;
            boolean wisdomCorrect = !slot3.isEmpty() && slot3.getItem() == ModItems.BOOK_OF_WISDOM;

            boolean isComplete = lightCorrect && shadowsCorrect && wisdomCorrect;

            // Kontrola, či sa stav hráča zmenil pre zabránenie opakovaniu
            UUID uuid = player.getUuid();
            if (!playerPuzzleState.containsKey(uuid) || playerPuzzleState.get(uuid) != isComplete) {
                playerPuzzleState.put(uuid, isComplete);

                if (isComplete) {
                    player.sendMessage(Text.literal("Hádanka vyriešená!"), false);

                    // Spustenie zvukového efektu teleportu
                    world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.0F, 1.0F);

                    // blindness efekt
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, false, true));

                    // spustenie odpočítavania teleportu
                    teleportTimers.put(uuid, 0);
                }
            }
        }
    }

    // Registrácia tick eventu – kontroluje teleport a spracúva dialógy
    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

                // Teleport hráča po 3 sekundách
            world.getPlayers().forEach(player -> {
                UUID uuid = player.getUuid();
                if (teleportTimers.containsKey(uuid)) {
                    int ticks = teleportTimers.get(uuid);
                    if (ticks >= 60) {
                        //pozicia ďalšej miestnosti
                        player.teleport(world, 55.5, -63, -26.5, player.getYaw(), player.getPitch());
                        teleportTimers.remove(uuid); //vyčistenie časovača
                        dialogueTimers.put(uuid, 0); // Spustenie dialógu
                    } else {
                        teleportTimers.put(uuid, ticks + 1);//počítanie tickov
                    }
                }
            });

            // Dialóg po teleportácií
            world.getPlayers().forEach(player -> {
                UUID playerId = player.getUuid();
                if (!dialogueTimers.containsKey(playerId)) return;

                int ticks = dialogueTimers.get(playerId);
                switch (ticks) {
                    //posielanie správ do chatu s onekorením
                    case 40 -> player.sendMessage(Text.literal("Duch: To ti to ale trvalo. Čakám ťa tu už večnosť."), false);
                    case 100 -> player.sendMessage(Text.literal("Player: Čo je to za miestnosť? Pôsobí dosť strašidelne."), false);
                    case 160 -> player.sendMessage(Text.literal("Duch: Toto je staroveká krypta. Uprostred sa nachádza hrob dávnej múmie. Skús ju nezobudiť."), false);
                    case 220 -> player.sendMessage(Text.literal("Player: To si ma veľmi nepotešil... Ale ako sa odtiaľto dostanem?"), false);
                    case 280 -> player.sendMessage(Text.literal("Duch: Musíš nájsť štyri rôzne kľúče, ktoré sú tu ukryté."), false);
                    case 340 -> player.sendMessage(Text.literal("Player: Kde ich teda nájdem?"), false);
                    case 400 -> {
                        player.sendMessage(Text.literal("Duch: Som v únikovej miestnosti ja, či ty? Hľadaj! Poradím ti len toľko – ak chceš pokračovať ďalej, hoď všetky kľúče do hrobu."), false);
                        dialogueTimers.remove(playerId); //ukončenie dialogu
                    }
                }

                dialogueTimers.put(playerId, ticks + 1); // posun dailogu na další tick
            });
        });
    }
}
