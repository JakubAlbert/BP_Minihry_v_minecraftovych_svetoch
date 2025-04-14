package net.albert.bp.events;

import net.minecraft.block.Blocks;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

import java.util.HashSet;
import java.util.UUID;

public class FireAltarHandler {
    private static final BlockPos FIRE_ALTAR_POS = new BlockPos(23, -59, 5); // Pozícia oltára
    private static final HashSet<UUID> playersCompleted = new HashSet<>();

    //handler pre aktiváciu oltára pri zapálení
    public static void checkFireAltar(ServerPlayerEntity player, ServerWorld world) {
        if (world.getBlockState(FIRE_ALTAR_POS).isOf(Blocks.FIRE)) {
            if (!playersCompleted.contains(player.getUuid())) {
                playersCompleted.add(player.getUuid()); // Uložíme hráča do zoznamu

                //efekt Blindness na 5 sekúnd
                player.addStatusEffect(new StatusEffectInstance(StatusEffects.BLINDNESS, 100, 0, false, false, true));

                //teleportácia po skončení efektu
                world.getServer().execute(() -> {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    // Teleportujeme hráča
                    player.teleport(world, 3.5, -60, 21.5, player.getYaw(), player.getPitch());

                    // Po teleportácii zobrazíme správu
                    player.sendMessage(Text.literal("Duch: Teraz si otestujeme tvoj sluch. Myslím si, že to zvládneš."), false);
                });
            }
        }
    }
}
