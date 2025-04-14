package net.albert.bp.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.message.v1.ServerMessageEvents;
import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.network.packet.s2c.play.TitleFadeS2CPacket;
import net.minecraft.network.packet.s2c.play.TitleS2CPacket;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.state.property.Properties;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.*;

public class FinalHandler {

    private static final BlockPos TRIGGER_POS = new BlockPos(-12, -60, 17);
    private static final BlockPos DOOR_TO_CLOSE = new BlockPos(-12, -60, 16);
    private static final BlockPos DOOR_TO_OPEN = new BlockPos(-12, -60, 18);
    private static final BlockPos END_TRIGGER_POS = new BlockPos(-12, -60, 22);
    private static final BlockPos BEDROOM_POS = new BlockPos(164, -59, 185);

    //časovanie udalostí
    private static final Map<UUID, Integer> finalDialogueTimers = new HashMap<>();
    private static final Map<UUID, Integer> wakeupTimers = new HashMap<>();
    private static final Set<UUID> hasTriggered = new HashSet<>();

    //Tick handler, ktorý spracováva záverečné scény a "prebudenie"
    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

            for (ServerPlayerEntity player : world.getPlayers()) {
                BlockPos playerPos = player.getBlockPos();
                UUID uuid = player.getUuid();

                // Spustenie záverečnej scény len raz
                if (playerPos.equals(TRIGGER_POS) && !hasTriggered.contains(uuid)) {
                    hasTriggered.add(uuid);

                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 60, 0, false, false, true));
                    world.setBlockState(DOOR_TO_CLOSE, Blocks.IRON_DOOR.getDefaultState().with(Properties.OPEN, false));

                    world.playSoundFromEntity(null, player, net.minecraft.sound.SoundEvents.MUSIC_DISC_FAR,
                            net.minecraft.sound.SoundCategory.MUSIC, 1.0f, 1.0f);

                    finalDialogueTimers.put(uuid, 0);
                }

                // Spustenie prebudenia
                if (playerPos.equals(END_TRIGGER_POS)) {
                    player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 120, 0, false, false, true));
                    wakeupTimers.put(uuid, 0);
                    player.teleport(world, BEDROOM_POS.getX(), BEDROOM_POS.getY(), BEDROOM_POS.getZ(), player.getYaw(), player.getPitch());
                }
            }

            // Spracovanie záverečného dialógu
            Iterator<Map.Entry<UUID, Integer>> finalIt = finalDialogueTimers.entrySet().iterator();
            while (finalIt.hasNext()) {
                Map.Entry<UUID, Integer> entry = finalIt.next();
                UUID uuid = entry.getKey();
                int ticks = entry.getValue();

                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player == null) {
                    finalIt.remove();
                    continue;
                }

                switch (ticks) {
                    case 40 -> player.sendMessage(Text.literal("Player: Haló? Prečo si ma tu zamkol?"), false);
                    case 140 -> player.sendMessage(Text.literal("Duch: Gratulujem, toto bola posledná hádanka, ale povedz mi, je toto podľa teba realita alebo sen? [Odpoveď uveďte do chatu]"), false);
                    case 240 -> finalIt.remove();
                }

                entry.setValue(ticks + 1);
            }

            // Spracovanie prebudenia
            Iterator<Map.Entry<UUID, Integer>> wakeIt = wakeupTimers.entrySet().iterator();
            while (wakeIt.hasNext()) {
                Map.Entry<UUID, Integer> entry = wakeIt.next();
                UUID uuid = entry.getKey();
                int ticks = entry.getValue();

                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player == null) {
                    wakeIt.remove();
                    continue;
                }

                switch (ticks) {
                    case 120 -> player.sendMessage(Text.literal("Player: Veď to je moja izba! Ako je to možné? Toto celé bol naozaj iba sen?"), false);
                    case 200 -> {
                        player.networkHandler.sendPacket(new TitleFadeS2CPacket(10, 70, 20));
                        player.networkHandler.sendPacket(new TitleS2CPacket(Text.literal("§l§6The End")));
                        wakeIt.remove();
                    }
                }

                entry.setValue(ticks + 1);
            }
        });

        // Odpoveď na otázku: "sen alebo realita"
        ServerMessageEvents.ALLOW_CHAT_MESSAGE.register((message, sender, params) -> {
            ServerPlayerEntity player = sender.getCommandSource().getPlayer();
            if (player == null) return true;

            String content = message.getContent().getString().trim().toLowerCase();
            if (content.contains("sen") || content.contains("realita")) {
                player.sendMessage(Text.literal("Duch: Ako myslíš. Otváram ti dvere a pokračuj do prázdna."), false);
                ServerWorld world = player.getServerWorld();
                world.setBlockState(DOOR_TO_OPEN, Blocks.IRON_DOOR.getDefaultState().with(Properties.OPEN, true));
            }

            return true;
        });
    }

    // Reset pre testovanie
    public static void resetFinalTrigger(ServerPlayerEntity player) {
        hasTriggered.remove(player.getUuid());
    }
}
