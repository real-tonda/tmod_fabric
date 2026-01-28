package com.togun.tmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import me.lucko.fabric.api.permissions.v0.Permissions;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeCommand {
    private static final Set<UUID> frozenPlayers = new HashSet<>();

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
            dispatcher.register(CommandManager.literal("freeze")
                    .requires(source -> Permissions.check(source, "tmod.command.freeze", 4))
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(FreezeCommand::execute)));
        });
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);

        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        UUID playerId = targetPlayer.getUuid();
        boolean isFrozen = frozenPlayers.contains(playerId);
        String targetName = targetPlayer.getName().getString();

        if (isFrozen) {
            // Unfreeze
            frozenPlayers.remove(playerId);
            targetPlayer.sendMessage(Text.literal("§aYou have been unfrozen!"), false);
            source.sendFeedback(() -> Text.literal("§a" + targetName + " has been unfrozen."), true);
        } else {
            // Freeze
            frozenPlayers.add(playerId);
            targetPlayer.sendMessage(Text.literal("§cYou have been frozen by a staff member!"), false);
            source.sendFeedback(() -> Text.literal("§c" + targetName + " has been frozen."), true);
        }

        return 1;
    }

    public static boolean isFrozen(UUID playerId) {
        return frozenPlayers.contains(playerId);
    }

    public static void unfreeze(UUID playerId) {
        frozenPlayers.remove(playerId);
    }
}
