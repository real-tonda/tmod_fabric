package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Identifier;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyCommand {
    private static final Set<UUID> flyingPlayers = new HashSet<>();
    private static final String FLY_DATA_KEY = "tmod_fly_enabled";
    
    private static final SuggestionProvider<ServerCommandSource> PLAYER_SUGGESTIONS = (context, builder) -> {
        MinecraftServer server = context.getSource().getServer();
        String remaining = builder.getRemaining().toLowerCase();
        
        for (ServerPlayerEntity player : server.getPlayerManager().getPlayerList()) {
            String playerName = player.getName().getString();
            if (playerName.toLowerCase().startsWith(remaining)) {
                builder.suggest(playerName);
            }
        }
        
        return builder.buildFuture();
    };
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("fly")
                .requires(source -> source.hasPermissionLevel(4)) // Requires operator status (level 4)
                .executes(FlyCommand::executeSelf)
                .then(CommandManager.argument("player", StringArgumentType.string())
                    .suggests(PLAYER_SUGGESTIONS)
                    .executes(FlyCommand::executeOther)));
        });
    }
    
    private static int executeSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }
        
        return toggleFly(player, source, player.getName().getString());
    }
    
    private static int executeOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }
        
        return toggleFly(targetPlayer, source, targetPlayer.getName().getString());
    }
    
    private static int toggleFly(ServerPlayerEntity player, ServerCommandSource source, String playerName) {
        UUID playerId = player.getUuid();
        boolean isCurrentlyFlying = flyingPlayers.contains(playerId);
        
        if (isCurrentlyFlying) {
            // Disable fly
            flyingPlayers.remove(playerId);
            player.getAbilities().allowFlying = false;
            player.getAbilities().flying = false;
            player.sendAbilitiesUpdate();
            
            // Save to player data
            saveFlyState(player, false);
            
            source.sendFeedback(() -> Text.literal("Fly mode disabled for " + playerName + "."), false);
        } else {
            // Enable fly
            flyingPlayers.add(playerId);
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
            player.sendAbilitiesUpdate();
            
            // Save to player data
            saveFlyState(player, true);
            
            source.sendFeedback(() -> Text.literal("Fly mode enabled for " + playerName + "! They can now fly freely."), false);
        }
        
        return 1;
    }
    
    private static ServerPlayerEntity findPlayer(MinecraftServer server, String playerName) {
        return server.getPlayerManager().getPlayer(playerName);
    }
    
    private static void saveFlyState(ServerPlayerEntity player, boolean enabled) {
        // For now, we'll just use in-memory storage
        // The fly state will persist during the server session
        // but will reset on server restart (this is acceptable for most use cases)
    }
    
    public static void restoreFlyState(ServerPlayerEntity player) {
        // For now, we'll just check if the player is already in our flying set
        // This provides session persistence but not server restart persistence
        UUID playerId = player.getUuid();
        if (flyingPlayers.contains(playerId)) {
            player.getAbilities().allowFlying = true;
            player.getAbilities().flying = true;
            player.sendAbilitiesUpdate();
        }
    }
    
    public static boolean isPlayerFlying(UUID playerId) {
        return flyingPlayers.contains(playerId);
    }
}