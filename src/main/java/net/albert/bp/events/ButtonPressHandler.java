package net.albert.bp.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.text.Text;
import net.minecraft.world.GameMode;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class ButtonPressHandler {
    // Uchováva tickový stav dialógu
    private static final HashMap<UUID, Integer> dialogueTimers = new HashMap<>();
    //metóda zavolaná pomocou tlačidla v štartovacej miestnosti, teleportuje, dá  zvuk a efekt slepoty
    public static void onButtonPressed(ServerPlayerEntity player, ServerWorld world) {
        // Prehrá zvuk
        world.playSound(null, player.getBlockPos(), SoundEvents.BLOCK_PORTAL_TRIGGER, SoundCategory.PLAYERS, 1.0F, 1.0F);

        // Nastaví hráča do Adventure a dá mu Blindness
        player.changeGameMode(GameMode.ADVENTURE);
        player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 200, 0, false, false, true));

        // Teleportuje hráča a zobrazenie titulku
        world.getServer().execute(() -> {
            player.teleport(world, 11.5, -59, -24.5, player.getYaw(), player.getPitch());
            player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
            player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§6Začíname!")));

            // Spustenie dialógu cez tick-based plánovanie
            dialogueTimers.put(player.getUuid(), 0);
        });
    }
    //postupné zobrazovanie dialogu
    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

            Iterator<Map.Entry<UUID, Integer>> iterator = dialogueTimers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID uuid = entry.getKey();
                int ticks = entry.getValue();

                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player == null) {
                    // Ak hráč opustil hru, dialóg sa ruší
                    iterator.remove();
                    continue;
                }

                switch (ticks) {
                    //dialog každých 40 sekúnd
                    case 40 -> player.sendMessage(Text.literal("Player: Čo sa to deje? Kde to som?"), false);
                    case 80 -> player.sendMessage(Text.literal("Duch: Dostal si sa do zakliatych miestností plných hádaniek."), false);
                    case 120 -> player.sendMessage(Text.literal("Player: Vystrašil si ma! Kto si?"), false);
                    case 160 -> player.sendMessage(Text.literal("Duch: Som predsa duch hier a budem ťa sprevádzať týmito miestnosťami."), false);
                    case 200 -> player.sendMessage(Text.literal("Player: No super... A ako sa odtiaľto dostanem von?"), false);
                    case 240 -> player.sendMessage(Text.literal("Duch: Predsa musíš vyriešiť všetky hádanky!"), false);
                    case 280 -> player.sendMessage(Text.literal("Player: Tak mi povedz, ako sa k tým hádankám dostanem."), false);
                    case 320 -> player.sendMessage(Text.literal("Duch: Ideš na to nejako rýchlo. Čo keby si si najprv zasvietil? Potom ti dám prvú nápovedu.Páčku treba stlačiť dva krát"), false);
                    case 360 -> {
                        player.sendMessage(Text.literal("Player: No dobre, dobre..."), false);
                        //ukončenie
                        iterator.remove();
                        continue;
                    }
                }

                //posúvanie tikov
                entry.setValue(ticks + 1);
            }
        });
    }
}
