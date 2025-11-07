package com.togun.tmod.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.Optional;

public class PlayerInfoCommands {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /seen command - available to all players
            dispatcher.register(CommandManager.literal("seen")
                .requires(source -> source.hasPermissionLevel(0))
                .then(CommandManager.argument("player", StringArgumentType.string())
                    .executes(PlayerInfoCommands::executeSeen)));
            
            // /whois command - requires operator
            dispatcher.register(CommandManager.literal("whois")
                .requires(source -> source.hasPermissionLevel(4))
                .then(CommandManager.argument("player", StringArgumentType.string())
                    .executes(PlayerInfoCommands::executeWhois)));
        });
    }
    
    private static int executeSeen(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        // Check if player is currently online
        ServerPlayerEntity onlinePlayer = source.getServer().getPlayerManager().getPlayer(playerName);
        
        if (onlinePlayer != null) {
            source.sendFeedback(() -> Text.literal("§a" + playerName + " is currently online!"), false);
            return 1;
        }
        
        // Try to get player from user cache
        Optional<GameProfile> profileOpt = source.getServer().getUserCache().findByName(playerName);
        
        if (profileOpt.isEmpty()) {
            source.sendError(Text.literal("Player '" + playerName + "' has never joined the server."));
            return 0;
        }
        
        GameProfile profile = profileOpt.get();
        
        source.sendFeedback(() -> Text.literal(
            "§7Player §f" + profile.getName() + " §7is §coffline§7.\n" +
            "§7UUID: §f" + profile.getId() + "\n" +
            "§7Last seen data is not tracked (requires additional implementation)."
        ), false);
        
        return 1;
    }
    
    private static int executeWhois(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");
        
        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);
        
        if (player == null) {
            source.sendError(Text.literal("Player '" + playerName + "' is not online!"));
            return 0;
        }
        
        // Gather player information
        String name = player.getName().getString();
        String uuid = player.getUuidAsString();
        String gamemode = player.interactionManager.getGameMode().name();
        String world = player.getWorld().getRegistryKey().getValue().toString();
        int ping = player.pingMilliseconds;
        String ip = player.getIp();
        boolean isOp = source.getServer().getPlayerManager().isOperator(player.getGameProfile());
        
        // Get position
        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();
        
        // Get health and hunger
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int hunger = player.getHungerManager().getFoodLevel();
        
        source.sendFeedback(() -> Text.literal(
            "§7§m                §r §bPlayer Info §7§m                §r\n" +
            "§7Name: §f" + name + "\n" +
            "§7UUID: §f" + uuid + "\n" +
            "§7IP Address: §f" + ip + "\n" +
            "§7Ping: §f" + ping + "ms\n" +
            "§7Gamemode: §f" + gamemode + "\n" +
            "§7World: §f" + world + "\n" +
            "§7Position: §f" + x + ", " + y + ", " + z + "\n" +
            "§7Health: §f" + String.format("%.1f", health) + "/" + String.format("%.1f", maxHealth) + "\n" +
            "§7Hunger: §f" + hunger + "/20\n" +
            "§7Operator: " + (isOp ? "§aYes" : "§cNo") + "\n" +
            "§7§m                                        §r"
        ), false);
        
        return 1;
    }
}

