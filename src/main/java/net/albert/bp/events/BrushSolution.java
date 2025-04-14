package net.albert.bp.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.minecraft.entity.effect.StatusEffectInstance;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.BrushItem;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.GameMode;

public class BrushSolution {
    // Registrácia logiky, ktorá každým tickom kontroluje stav hráča
    public static void registerBrushLogic() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

            for (ServerPlayerEntity player : world.getPlayers()) {
                ItemStack mainHand = player.getMainHandStack();

                //overovanie, či hráč drží predmet v ruke a či má daný efekt
                boolean isHoldingBrush = mainHand.getItem() instanceof BrushItem;
                boolean isHoldingHoe = mainHand.getItem() == Items.STONE_HOE;
                boolean isHoldingFlint = mainHand.getItem() == Items.FLINT_AND_STEEL;
                boolean hasFatigue = player.hasStatusEffect(StatusEffects.MINING_FATIGUE);

                //ak hráč drži predmet
                if (isHoldingBrush || isHoldingHoe || isHoldingFlint) {
                    //prepneme ho do survival modu
                    if (player.interactionManager.getGameMode() != GameMode.SURVIVAL) {
                        player.changeGameMode(GameMode.SURVIVAL);
                    }
                    //pridáme efekt mining fatigue
                    if (!hasFatigue) {
                        player.addStatusEffect(new StatusEffectInstance(
                                StatusEffects.MINING_FATIGUE, 600, 254, false, false, true));
                    }
                } else {
                    //v oopačnom prípade hráča prepne naspäť do adventure modu
                    if (player.interactionManager.getGameMode() != GameMode.ADVENTURE) {
                        player.changeGameMode(GameMode.ADVENTURE);
                    }
                    //odstránenie efektu
                    if (hasFatigue) {
                        player.removeStatusEffect(StatusEffects.MINING_FATIGUE);
                    }
                }
            }
        });
    }
}
