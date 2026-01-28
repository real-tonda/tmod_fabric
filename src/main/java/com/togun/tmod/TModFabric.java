package com.togun.tmod;

import com.mojang.authlib.properties.Property;
import com.togun.tmod.mixin.GameProfileAccessor;
import com.togun.tmod.blacklist.ItemBlacklistManager;
import com.togun.tmod.commands.*;
import com.togun.tmod.config.ModConfigManager;
import com.togun.tmod.config.TutilsConfigManager;
import com.togun.tmod.config.XaeroEffectManager;
import com.togun.tmod.skin.SkinData;
import com.togun.tmod.skin.SkinManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.server.network.ServerPlayerEntity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TModFabric implements ModInitializer {
    public static final String MOD_ID = "tmod_fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    // Tick counter for periodic effect application
    private static int tickCounter = 0;
    private static final int EFFECT_REFRESH_INTERVAL = 20 * 60; // Every 60 seconds

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TMod Fabric Server-Side Mod!");

        // Initialize managers
        ItemBlacklistManager.initialize(FabricLoader.getInstance().getConfigDir().toFile());
        ModConfigManager.initialize(FabricLoader.getInstance().getConfigDir().toFile());
        TutilsConfigManager.initialize(FabricLoader.getInstance().getConfigDir().toFile());

        // Register commands
        FlyCommand.register();
        GamemodeCommands.register();
        SkinCommand.register();
        TpsCommand.register();
        PingCommand.register();
        PlayerInfoCommands.register();
        FreezeCommand.register();
        GodCommand.register();
        THelpCommand.register();
        BroadcastCommand.register();
        ItemBlacklistCommand.register();
        DimensionCommand.register();
        ModConfigCommand.register();
        TutilsCommand.register();

        // Register player join event to restore fly state and handle skins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            FlyCommand.restoreFlyState(player);

            // Apply Xaero effects based on server configuration
            XaeroEffectManager.applyConfiguredEffects(player);

            // Check if player has a cached skin
            SkinData cachedSkin = SkinManager.getCachedSkin(player.getUuid());
            if (cachedSkin != null) {
                // Apply cached skin to the player's GameProfile
                ((GameProfileAccessor) (Object) player.getGameProfile()).getProperties().removeAll("textures");
                ((GameProfileAccessor) (Object) player.getGameProfile()).getProperties().put(
                        "textures",
                        new Property("textures", cachedSkin.getValue(), cachedSkin.getSignature()));
                LOGGER.info(
                        "Cached skin applied to: " + player.getName().getString() + " (visible after next reconnect)");
            } else {
                // Auto-fetch and cache their real Mojang skin
                SkinManager.autoFetchSkin(player);
            }
        });

        // Register server tick event to periodically refresh Xaero effects
        ServerTickEvents.END_SERVER_TICK.register(server -> {
            tickCounter++;
            if (tickCounter >= EFFECT_REFRESH_INTERVAL) {
                tickCounter = 0;
                // Refresh effects for all online players
                for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
                    XaeroEffectManager.applyConfiguredEffects(player);
                }
            }
        });

        LOGGER.info("TMod initialized successfully!");
    }
}
