package com.togun.tmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.togun.tmod.skin.SkinManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class SkinCommand {
    
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
            // /skin set <playerName> - Set your skin to match another player
            dispatcher.register(CommandManager.literal("skin")
                .then(CommandManager.literal("set")
                    .then(CommandManager.argument("playerName", StringArgumentType.string())
                        .executes(SkinCommand::setSkinSelf)
                        .then(CommandManager.argument("target", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .requires(source -> source.hasPermissionLevel(4))
                            .executes(SkinCommand::setSkinOther))))
                
                // /skin clear - Clear your custom skin
                .then(CommandManager.literal("clear")
                    .executes(SkinCommand::clearSkinSelf)
                    .then(CommandManager.argument("target", StringArgumentType.string())
                        .suggests(PLAYER_SUGGESTIONS)
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes(SkinCommand::clearSkinOther)))
                
                // /skin update - Refresh your skin
                .then(CommandManager.literal("update")
                    .executes(SkinCommand::updateSkinSelf)
                    .then(CommandManager.argument("target", StringArgumentType.string())
                        .suggests(PLAYER_SUGGESTIONS)
                        .requires(source -> source.hasPermissionLevel(4))
                        .executes(SkinCommand::updateSkinOther))));
        });
    }
    
    /**
     * Set skin for self
     */
    private static int setSkinSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }
        
        String targetPlayerName = StringArgumentType.getString(context, "playerName");
        
        source.sendFeedback(() -> Text.literal("§eFetching skin for " + targetPlayerName + "..."), false);
        
        SkinManager.setSkin(player, targetPlayerName).thenAccept(success -> {
            if (success) {
                source.sendFeedback(() -> Text.literal("§aSkin updated to " + targetPlayerName + "'s skin! §eReconnect to see changes."), false);
            } else {
                source.sendError(Text.literal("§cFailed to fetch skin for " + targetPlayerName + ". Player may not exist."));
            }
        });
        
        return 1;
    }
    
    /**
     * Set skin for another player (requires op)
     */
    private static int setSkinOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String targetPlayerName = StringArgumentType.getString(context, "playerName");
        String targetName = StringArgumentType.getString(context, "target");
        
        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), targetName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("§cPlayer '" + targetName + "' not found!"));
            return 0;
        }
        
        source.sendFeedback(() -> Text.literal("§eFetching skin for " + targetPlayerName + "..."), false);
        
        SkinManager.setSkin(targetPlayer, targetPlayerName).thenAccept(success -> {
            if (success) {
                source.sendFeedback(() -> Text.literal("§aUpdated " + targetPlayer.getName().getString() + "'s skin to " + targetPlayerName + "'s skin!"), false);
                targetPlayer.sendMessage(Text.literal("§aYour skin has been updated to " + targetPlayerName + "'s skin! §eReconnect to see changes."));
            } else {
                source.sendError(Text.literal("§cFailed to fetch skin for " + targetPlayerName + ". Player may not exist."));
            }
        });
        
        return 1;
    }
    
    /**
     * Clear skin for self
     */
    private static int clearSkinSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }
        
        SkinManager.clearSkin(player);
        source.sendFeedback(() -> Text.literal("§aYour skin has been restored to default! §eReconnect to see changes."), false);
        
        return 1;
    }
    
    /**
     * Clear skin for another player (requires op)
     */
    private static int clearSkinOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String targetName = StringArgumentType.getString(context, "target");
        
        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), targetName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("§cPlayer '" + targetName + "' not found!"));
            return 0;
        }
        
        SkinManager.clearSkin(targetPlayer);
        source.sendFeedback(() -> Text.literal("§aCleared " + targetPlayer.getName().getString() + "'s custom skin!"), false);
        targetPlayer.sendMessage(Text.literal("§aYour skin has been restored to default! §eReconnect to see changes."));
        
        return 1;
    }
    
    /**
     * Update/refresh skin for self
     */
    private static int updateSkinSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }
        
        SkinManager.restoreSkin(player);
        source.sendFeedback(() -> Text.literal("§aSkin refreshed!"), false);
        
        return 1;
    }
    
    /**
     * Update/refresh skin for another player (requires op)
     */
    private static int updateSkinOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String targetName = StringArgumentType.getString(context, "target");
        
        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), targetName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("§cPlayer '" + targetName + "' not found!"));
            return 0;
        }
        
        SkinManager.restoreSkin(targetPlayer);
        source.sendFeedback(() -> Text.literal("§aRefreshed " + targetPlayer.getName().getString() + "'s skin!"), false);
        
        return 1;
    }
    
    private static ServerPlayerEntity findPlayer(MinecraftServer server, String playerName) {
        return server.getPlayerManager().getPlayer(playerName);
    }
}

