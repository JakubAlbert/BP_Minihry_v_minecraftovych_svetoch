package net.albert.bp;

import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.albert.bp.events.*;
import net.albert.bp.item.ModItems;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.player.UseBlockCallback;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.block.Blocks;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;




//Hlavná trieda módu – inicializuje všetky eventy, handleri, príkazy a logiku
public class BakalarskaPracaAlbert implements ModInitializer {
    //Identifikátor módu + logger pre výpisy do konzoly
    public static final String MOD_ID = "bpalbert";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    private static final BlockPos BUTTON_START_POS = new BlockPos(9, -59, -12); // Súradnice tlačidla "Štart"
    private static final BlockPos LEVER_LIGHT_POS = new BlockPos(18, -59, -22); // Súradnice páčky na svetlo
    private static final BlockPos LEVER_WISDOM_POS = new BlockPos(22, -58, -28); // Súradnice páčky pre Book of Wisdom

    @Override

        //metóda onInitialize, pri štarte spúšta ostatné triedy
        public void onInitialize() {

        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            player.getInventory().clear(); //Vyčistenie inventára
        });


        //Registrácia príkazu /resetroom
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            ResetRoomCommand.registerCommand(dispatcher);
        });

        //Registrácia  handlerov pre jednotlivé miestnosti a logiku
        ButtonPressHandler.registerTickHandler();
        LeverShadowsHandler.registerTickHandler();
        LeverLightHandler.registerTickHandler();
        ElementTempleButtonHandler.registerTickHandler();
        ChatAnswerHandler.registerTickHandler();
        BookPuzzleHandler.registerTickHandler();
        AdventureProtectionHandler.registerProtection();
        FinalHandler.registerTickHandler();
        RotateRoomHandler.register();
        BrushSolution.registerBrushLogic();
        ChatAnswerHandler.registerChatListener();
        ElementTempleButtonHandler.registerButtonEvent();

        //interakcia s dreveným tlačidlom
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos clickedPos = hitResult.getBlockPos();

                //  kontrolujeme, či bolo stlačené drevené tlačidlo na správnej pozícii
                if (clickedPos.equals(new BlockPos(-1, -62, 9)) && world.getBlockState(clickedPos).isOf(Blocks.OAK_BUTTON)) {
                    world.getServer().execute(() -> {
                        WoodButtonHandler.onButtonPress(serverPlayer, (ServerWorld) world, clickedPos);
                    });
                    return ActionResult.SUCCESS;
                }
            }
            return ActionResult.PASS;
        });

        // Automatická kontrola truhlice pre otváranie/zatváranie poklopu
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            WoodButtonHandler.checkChest(world);
        });


        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos clickedPos = hitResult.getBlockPos(); // Správne získanie pozície tlačidla

                //note block hádanka
                if (world.getBlockState(clickedPos).isOf(Blocks.POLISHED_BLACKSTONE_BUTTON)) {
                    world.getServer().execute(() -> {
                        boolean success = NoteBlockButtonHandler.onButtonPress(serverPlayer, (ServerWorld) world, clickedPos);

                        if (success) {
                            serverPlayer.sendMessage(Text.literal("HOTOVO!"), false);
                        }
                    });

                    return ActionResult.PASS;
                }
            }
            return ActionResult.PASS;
        });

        //oheň na oltári, spustenie dalšej fázy
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                FireAltarHandler.checkFireAltar(player, world);
            }
        });

        //páčka v truhlici, otvorenie dverí
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            LeverChestHandler.checkChest(world);
        });

        //strašidelný zvuk
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                ChestKeyHandler.checkPlayerPosition(player, world);

            }
        });

        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
        });

        //otvorenie dverí po vhodení klučov do hoppperu
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            HopperKeyHandler.checkHopper(world); // už nepotrebuje hráča
        });


        //registrácia vlastných predmetov
        ModItems.registerModItems();
        LOGGER.info("Escape Room mod úspešne inicializovaný!");

        //registrácia príkazu /getpos na získanie súradníc bloku
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("getpos")
                    .executes(context -> {
                        ServerPlayerEntity player = context.getSource().getPlayer();
                        if (player == null) {
                            context.getSource().sendFeedback(() -> Text.literal("Tento príkaz môže použiť iba hráč!"), false);
                            return 0;
                        }

                        // Získanie objektu, na ktorý sa hráč pozerá
                        HitResult hit = player.raycast(10.0, 0, false);

                        if (hit instanceof BlockHitResult blockHit) {
                            BlockPos pos = blockHit.getBlockPos();
                            context.getSource().sendFeedback(() -> Text.literal("Blok na súradniciach: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                        } else if (hit instanceof EntityHitResult entityHit) {
                            BlockPos pos = entityHit.getEntity().getBlockPos();
                            context.getSource().sendFeedback(() -> Text.literal("Entita na súradniciach: " + pos.getX() + ", " + pos.getY() + ", " + pos.getZ()), false);
                        } else {
                            context.getSource().sendFeedback(() -> Text.literal("Nezameral si žiadny blok alebo entitu!"), false);
                        }
                        return 1;
                    })
            );
        });

        // Automatická kontrola truhlice pre Book of Wisdom
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            ServerWorld world = server.getOverworld();
            for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                BlockPos chestPos = new BlockPos(4, -60, -25);
                BookPuzzleHandler.checkChest(player, world);
            }
        });

        // Event na interakciu s blokmi (páčky, tlačidlá)
        UseBlockCallback.EVENT.register((player, world, hand, hitResult) -> {
            if (!world.isClient && player instanceof ServerPlayerEntity serverPlayer) {
                BlockPos clickedPos = hitResult.getBlockPos();

                //  lačidlo "Štart" v úvodnej miestnosti
                if (clickedPos.equals(BUTTON_START_POS) && world.getBlockState(clickedPos).isOf(Blocks.STONE_BUTTON)) {
                    ButtonPressHandler.onButtonPressed(serverPlayer, (ServerWorld) world);
                    return ActionResult.SUCCESS;
                }

                // Rozlíšenie páčok podľa súradníc
                if (world.getBlockState(clickedPos).isOf(Blocks.LEVER)) {
                    world.getServer().execute(() -> {
                        try { Thread.sleep(100); } catch (InterruptedException ignored) {}

                        if (clickedPos.equals(LEVER_LIGHT_POS)) {
                            LeverLightHandler.onLeverPulled(serverPlayer, (ServerWorld) world, clickedPos);
                        } else if (clickedPos.equals(LEVER_WISDOM_POS)) {
                            LeverShadowsHandler.onLeverPulled(serverPlayer, (ServerWorld) world, clickedPos);
                        }
                    });
                    return ActionResult.PASS;
                }
            }
            return ActionResult.PASS;
        });

        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ServerWorld world = server.getOverworld();
            ResetRoomCommand.resetRoom(world);
        });



    }
}