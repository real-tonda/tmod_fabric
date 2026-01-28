package com.togun.tmod.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.togun.tmod.mixin.EntityAccessor;

import com.togun.tmod.mixin.ServerCommonNetworkHandlerAccessor;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import me.lucko.fabric.api.permissions.v0.Permissions;
import com.togun.tmod.mixin.MinecraftServerAccessor;
import com.togun.tmod.mixin.GameProfileAccessor;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.util.UserCache;

import java.util.Optional;

public class PlayerInfoCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /seen command - available to all players
            dispatcher.register(CommandManager.literal("seen")
                    .requires(source -> Permissions.check(source, "tmod.command.seen", 0))
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .executes(PlayerInfoCommands::executeSeen)));

            // /whois command - requires operator
            dispatcher.register(CommandManager.literal("whois")
                    .requires(source -> Permissions.check(source, "tmod.command.whois", 4))
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
        UserCache userCache = ((MinecraftServerAccessor) source.getServer()).getUserCache();
        Optional<?> entryOpt = userCache.findByName(playerName);

        if (entryOpt.isEmpty()) {
            source.sendError(Text.literal("Player '" + playerName + "' has never joined the server."));
            return 0;
        }

        GameProfile profile;
        Object entry = entryOpt.get();
        if (entry instanceof GameProfile) {
            profile = (GameProfile) entry;
        } else {
            // Assume it's an entry wrapper (PlayerConfigEntry) with a getProfile or profile
            // method
            try {
                profile = (GameProfile) entry.getClass().getMethod("profile").invoke(entry);
            } catch (Exception e1) {
                try {
                    profile = (GameProfile) entry.getClass().getMethod("getProfile").invoke(entry);
                } catch (Exception e2) {
                    source.sendError(Text.literal("Error: Could not retrieve GameProfile for " + playerName));
                    return 0;
                }
            }
        }

        final GameProfile finalProfile = profile;

        source.sendFeedback(() -> Text.literal(
                "§7Player §f" + ((GameProfileAccessor) (Object) finalProfile).getName() + " §7is §coffline§7.\n" +
                        "§7UUID: §f" + ((GameProfileAccessor) (Object) finalProfile).getId() + "\n" +
                        "§7Last seen data is not tracked (requires additional implementation)."),
                false);

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
        String world = ((EntityAccessor) player).getLevelField().getRegistryKey().getValue().toString();
        int ping = ((ServerCommonNetworkHandlerAccessor) player.networkHandler).getLatency();
        String ip = player.getIp();
        boolean isOp = Permissions.check(player, "tmod.admin", 4);

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
                        "§7§m                                        §r"),
                false);

        return 1;
    }
}
