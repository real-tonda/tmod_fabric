package com.togun.tmod.commands;

import com.togun.tmod.util.Perms;
import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import me.lucko.fabric.api.permissions.v0.Permissions;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtIo;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.WorldSavePath;

import java.io.File;
import java.util.Optional;
import java.util.UUID;

public class PlayerInfoCommands {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            // /seen command - available to all players
            dispatcher.register(CommandManager.literal("seen")
                .requires(Perms.require("tmod.command.seen", 0))
                .then(CommandManager.argument("player", StringArgumentType.string())
                    .executes(PlayerInfoCommands::executeSeen)));

            // /whois command - requires operator
            dispatcher.register(CommandManager.literal("whois")
                .requires(Perms.require("tmod.command.whois", 4))
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
        File dataFile = playerDataFile(source.getServer(), profile.getId());

        StringBuilder msg = new StringBuilder("§7Player §f" + profile.getName() + " §7is §coffline§7.\n");
        if (dataFile.exists()) {
            msg.append("§7Last seen: §f").append(ago(System.currentTimeMillis() - dataFile.lastModified())).append(" ago");
            // Coordinates are staff-only
            if (Permissions.check(source, "tmod.command.seen.position", 4)) {
                msg.append("\n§7Last position: §f").append(lastLocation(dataFile));
            }
        } else {
            msg.append("§7No saved data for this player.");
        }

        source.sendFeedback(() -> Text.literal(msg.toString()), false);

        return 1;
    }

    private static int executeWhois(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity player = source.getServer().getPlayerManager().getPlayer(playerName);

        if (player == null) {
            return executeWhoisOffline(source, playerName);
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

        // Playtime (stat is in ticks) and experience
        int playTicks = player.getStatHandler().getStat(Stats.CUSTOM.getOrCreateStat(Stats.PLAY_TIME));
        String playtime = duration(playTicks * 50L);
        int xpLevel = player.experienceLevel;

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
            "§7XP Level: §f" + xpLevel + "\n" +
            "§7Playtime: §f" + playtime + "\n" +
            "§7Operator: " + (isOp ? "§aYes" : "§cNo") + "\n" +
            "§7§m                                        §r"
        ), false);

        return 1;
    }

    /**
     * Whois for a player that is offline, using their saved data file
     */
    private static int executeWhoisOffline(ServerCommandSource source, String playerName) {
        Optional<GameProfile> profileOpt = source.getServer().getUserCache().findByName(playerName);
        if (profileOpt.isEmpty()) {
            source.sendError(Text.literal("Player '" + playerName + "' has never joined the server."));
            return 0;
        }

        GameProfile profile = profileOpt.get();
        File dataFile = playerDataFile(source.getServer(), profile.getId());
        boolean isOp = source.getServer().getPlayerManager().isOperator(profile);

        String lastSeen = dataFile.exists()
            ? ago(System.currentTimeMillis() - dataFile.lastModified()) + " ago"
            : "unknown";
        String lastPos = dataFile.exists() ? lastLocation(dataFile) : "unknown";

        source.sendFeedback(() -> Text.literal(
            "§7§m                §r §bPlayer Info §7§m                §r\n" +
            "§7Name: §f" + profile.getName() + " §c(offline)\n" +
            "§7UUID: §f" + profile.getId() + "\n" +
            "§7Last seen: §f" + lastSeen + "\n" +
            "§7Last position: §f" + lastPos + "\n" +
            "§7Operator: " + (isOp ? "§aYes" : "§cNo") + "\n" +
            "§7§m                                        §r"
        ), false);

        return 1;
    }

    private static File playerDataFile(MinecraftServer server, UUID id) {
        return server.getSavePath(WorldSavePath.PLAYERDATA).resolve(id + ".dat").toFile();
    }

    /**
     * Reads "x, y, z in dimension" from a saved player data file
     */
    private static String lastLocation(File dataFile) {
        try {
            NbtCompound nbt = NbtIo.readCompressed(dataFile);
            if (!nbt.contains("Pos", NbtElement.LIST_TYPE)) {
                return "unknown";
            }
            var pos = nbt.getList("Pos", NbtElement.DOUBLE_TYPE);
            return (int) pos.getDouble(0) + ", " + (int) pos.getDouble(1) + ", " + (int) pos.getDouble(2)
                + " in " + nbt.getString("Dimension");
        } catch (Exception e) {
            return "unknown";
        }
    }

    private static String ago(long millis) {
        return duration(Math.max(millis, 0));
    }

    private static String duration(long millis) {
        long minutes = millis / 60_000;
        long days = minutes / 1440;
        long hours = (minutes / 60) % 24;
        minutes %= 60;
        if (days > 0) {
            return days + "d " + hours + "h";
        }
        if (hours > 0) {
            return hours + "h " + minutes + "m";
        }
        return minutes + "m";
    }
}
