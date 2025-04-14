package net.albert.bp.events;

import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.entity.decoration.ItemFrameEntity;
import net.minecraft.entity.decoration.painting.PaintingEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.GameMode;

public class AdventureProtectionHandler {

    //pozícia štartu, kam sa hráč automaticky teleportuje po pripojení sa do sveta
    private static final BlockPos START_POSITION = new BlockPos(9, -60, -8);
    private static final float START_YAW = -89.8f; // natočenie hráča smer na býchod
    private static final float START_PITCH = 2.1f; // mierne nadol

    //registrácia ochranných pravidiel a pozicie po pripojeni
    public static void registerProtection() {
        // blokovanie ničenia Item Frame a obrazov hraca v Adventure mode
        AttackEntityCallback.EVENT.register((player, world, hand, entity, hitResult) -> {
            if (player instanceof ServerPlayerEntity serverPlayer &&
                    serverPlayer.interactionManager.getGameMode() == GameMode.ADVENTURE &&
                    (entity instanceof ItemFrameEntity || entity instanceof PaintingEntity)) {
                return ActionResult.FAIL; //blokovanie zničenia
            }
            return ActionResult.PASS;//povoliť v ostatných prípadoch
        });

        // teleport hráča po pripojení na spawn pozíciu
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            ServerWorld world = server.getOverworld();

            //stredová pozícia bloku spawn pozicie
            double x = START_POSITION.getX() + 0.5;
            double y = START_POSITION.getY();
            double z = START_POSITION.getZ() + 0.5;

            //teleportovanie aj s natočenim
            player.teleport(world, x, y, z, START_YAW, START_PITCH);
        });
    }
}
