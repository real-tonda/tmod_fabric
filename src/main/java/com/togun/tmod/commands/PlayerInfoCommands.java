package com.togun.tmod.commands;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.togun.tmod.TModFabric;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.GameProfileCache;

import java.lang.reflect.Field;
import java.util.Optional;

public class PlayerInfoCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /seen command - available to all players
            dispatcher.register(Commands.literal("seen")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.seen", 0))
                    .then(Commands.argument("player", StringArgumentType.string())
                            .executes(PlayerInfoCommands::executeSeen)));

            // /whois command - requires operator
            dispatcher.register(Commands.literal("whois")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.whois", 4))
                    .then(Commands.argument("player", StringArgumentType.string())
                            .executes(PlayerInfoCommands::executeWhois)));
        });
    }

    private static int executeSeen(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        // Check if player is currently online
        ServerPlayer onlinePlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (onlinePlayer != null) {
            source.sendSuccess(() -> Component.literal("§a" + playerName + " is currently online!"), false);
            return 1;
        }

        // Try to get player from user cache
        GameProfileCache userCache = getUserCache(source.getServer());
        if (userCache == null) {
            source.sendFailure(Component.literal("Error: Could not access Server UserCache."));
            return 0;
        }
        Optional<?> entryOpt = userCache.get(playerName);

        if (entryOpt.isEmpty()) {
            source.sendFailure(Component.literal("Player '" + playerName + "' has never joined the server."));
            return 0;
        }

        GameProfile profile = null;
        Object entry = entryOpt.get();
        if (entry instanceof GameProfile) {
            profile = (GameProfile) entry;
        } else if (entry != null) {
            // Extract profile from PlayerConfigEntry using reflection
            try {
                profile = (GameProfile) entry.getClass().getMethod("profile").invoke(entry);
            } catch (Exception e1) {
                try {
                    profile = (GameProfile) entry.getClass().getMethod("getProfile").invoke(entry);
                } catch (Exception e2) {
                    source.sendFailure(Component.literal("Error: Could not retrieve GameProfile for " + playerName));
                    return 0;
                }
            }
        }

        if (profile == null) {
            source.sendFailure(Component.literal("Error: GameProfile entry is empty for " + playerName));
            return 0;
        }

        final GameProfile finalProfile = profile;

        source.sendSuccess(() -> Component.literal(
                "§7Player §f" + finalProfile.name() + " §7is §coffline§7.\n" +
                        "§7UUID: §f" + finalProfile.id() + "\n" +
                        "§7Last seen data is not tracked (requires additional implementation)."),
                false);

        return 1;
    }

    private static int executeWhois(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer player = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (player == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' is not online!"));
            return 0;
        }

        // Gather player information
        String name = player.getName().getString();
        String uuid = player.getStringUUID();
        String gamemode = player.gameMode.getGameModeForPlayer().name();
        String world = player.level().dimension().location().toString();
        int ping = player.connection.latency();
        String ip = player.getIpAddress();
        boolean isOp = me.lucko.fabric.api.permissions.v0.Permissions.check(player, "tmod.admin", 4);

        // Get position
        int x = (int) player.getX();
        int y = (int) player.getY();
        int z = (int) player.getZ();

        // Get health and hunger
        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();
        int hunger = player.getFoodData().getFoodLevel();

        source.sendSuccess(() -> Component.literal(
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

    /**
     * Helper to get UserCache via reflection to avoid remapping issues across
     * versions.
     */
    private static GameProfileCache getUserCache(MinecraftServer server) {
        try {
            for (Field field : MinecraftServer.class.getDeclaredFields()) {
                if (field.getType() == GameProfileCache.class) {
                    field.setAccessible(true);
                    return (GameProfileCache) field.get(server);
                }
            }
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to retrieve UserCache via reflection", e);
        }
        return null;
    }
}
