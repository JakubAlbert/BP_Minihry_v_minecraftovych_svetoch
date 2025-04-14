package net.albert.bp.events;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.albert.bp.item.ModItems;
import net.minecraft.block.Blocks;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;
import net.minecraft.item.ItemStack;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

public class LeverLightHandler {
    private static final BlockPos LEVER_POS = new BlockPos(18, -59, -22); // Súradnice páčky
    private static final BlockPos REDSTONE_POS = new BlockPos(20, -59, -22); // Súradnice redstone prachu

    private static final Logger LOGGER = LoggerFactory.getLogger("LeverLightHandler");
    private static final HashSet<UUID> playersWhoReceived = new HashSet<>();
    private static final HashMap<UUID, Integer> dialogueTimers = new HashMap<>(); // Mapovanie hráčov na dialóg

    //metoda na reakciu zatiahnutia páčky , Hráč získa Book of Light, spustí sa dialóg a objaví sa redstone.
    public static void onLeverPulled(ServerPlayerEntity player, ServerWorld world, BlockPos pos) {
        if (pos.equals(LEVER_POS) && world.getBlockState(pos).isOf(Blocks.LEVER)) {
            // Skontrolujeme, či hráč už má knihu
            if (!player.getInventory().contains(new ItemStack(ModItems.BOOK_OF_LIGHT))) {
                // Pridáme hráčovi Book of Light
                player.getInventory().insertStack(new ItemStack(ModItems.BOOK_OF_LIGHT));
                player.sendMessage(Text.literal("Získal si Book of Light!"), false);
            } else {
                player.sendMessage(Text.literal("Už si získal Book of Light."), false);
            }

            // Po stlačení páčky položíme redstone na dané súradnice, kvôli resetu miestnosti
            if (world.getBlockState(REDSTONE_POS).isAir()) {
                world.setBlockState(REDSTONE_POS, Blocks.REDSTONE_WIRE.getDefaultState());
            }

            // Spustíme pokračovanie dialógu
            dialogueTimers.put(player.getUuid(), 0);
        }
    }

    public static void registerTickHandler() {
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();

            // Iterator na bezpečné odstraňovanie prvkov - obmedzenie crashovania hry z predosleho kodu
            Iterator<Map.Entry<UUID, Integer>> iterator = dialogueTimers.entrySet().iterator();
            while (iterator.hasNext()) {
                Map.Entry<UUID, Integer> entry = iterator.next();
                UUID uuid = entry.getKey();
                int ticks = entry.getValue();

                ServerPlayerEntity player = world.getServer().getPlayerManager().getPlayer(uuid);
                if (player == null) continue;

                //postupný dialóg
                switch (ticks) {
                    case 40 -> player.sendMessage(Text.literal("Player: Nejaká stará kniha? Čo mám podľa teba robiť so starou knihou?! Ja chcem ísť von, nie ju čítať!"), false);
                    case 80 -> player.sendMessage(Text.literal("Duch: Tá kniha je jedným z kľúčov, ktoré ti pomôžu dostať sa odtiaľto von."), false);
                    case 120 -> player.sendMessage(Text.literal("Player: Dobre, už rozumiem. Len stále neviem, čo s tou knihou robiť. Nechceš mi poradiť trošku viac, prosím ťa?"), false);
                    case 160 -> player.sendMessage(Text.literal("Duch: Najprv si ťa musím otestovať. Predsa len sme v Minecrafte, tak by si mal niečo vedieť o základoch, nie?"), false);
                    case 200 -> {
                        player.sendMessage(Text.literal("Duch: Kocka tvorí Minecraft, tak ma zaujíma – koľko stien má kocka? [Odpoveď uveď v tvare číslice]"), false);
                        iterator.remove();
                    }
                }

                entry.setValue(ticks + 1);
            }
        });
    }
}
