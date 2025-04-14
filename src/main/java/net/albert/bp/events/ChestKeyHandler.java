package net.albert.bp.events;

import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.BlockPos;

import java.util.HashMap;
import java.util.UUID;

public class ChestKeyHandler {
    private static final BlockPos SOUND_TRIGGER_POS = new BlockPos(45, -60, -18); // Súradnice bloku na zvuk
    private static final HashMap<UUID, Long> soundTriggerTimestamps = new HashMap<>(); // Ukladanie času posledného spustenia zvuku
    private static final long SOUND_DELAY = 5000; // 5 sekúnd

    //kontrola, či hráč stojí na súradniciach pre spustenie zvuku
    public static void checkPlayerPosition(ServerPlayerEntity player, ServerWorld world) {
        BlockPos playerPos = player.getBlockPos();
        UUID playerUUID = player.getUuid();
        long currentTime = System.currentTimeMillis();

        if (playerPos.equals(SOUND_TRIGGER_POS)) {
            if (!soundTriggerTimestamps.containsKey(playerUUID) ||
                    currentTime - soundTriggerTimestamps.get(playerUUID) >= SOUND_DELAY) {

                // strašidelny zvuk pri prejdeni - spustenie
                world.playSound(null, SOUND_TRIGGER_POS, SoundEvents.AMBIENT_CAVE.value(), SoundCategory.AMBIENT, 1.0F, 1.0F);

                // aktualizujeme čas posledného spustenia zvuku, aby sa po 5s znovu mohol spustiť pri prejdení, vyhnutie sa loopu
                soundTriggerTimestamps.put(playerUUID, currentTime);
            }
        }
    }
}
