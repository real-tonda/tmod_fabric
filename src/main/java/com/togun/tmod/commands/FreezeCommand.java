package com.togun.tmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FreezeCommand {
    private static final Set<UUID> frozenPlayers = new HashSet<>();

    private static final SuggestionProvider<CommandSourceStack> PLAYER_SUGGESTIONS = (context, builder) -> {
        MinecraftServer server = context.getSource().getServer();
        String remaining = builder.getRemaining().toLowerCase();

        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            String playerName = player.getName().getString();
            if (playerName.toLowerCase().startsWith(remaining)) {
                builder.suggest(playerName);
            }
        }

        return builder.buildFuture();
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("freeze")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.freeze", 4))
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(FreezeCommand::execute)));
        });
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        UUID playerId = targetPlayer.getUUID();
        boolean isFrozen = frozenPlayers.contains(playerId);
        String targetName = targetPlayer.getName().getString();

        if (isFrozen) {
            // Unfreeze
            frozenPlayers.remove(playerId);
            targetPlayer.sendSystemMessage(Component.literal("§aYou have been unfrozen!"));
            source.sendSuccess(() -> Component.literal("§a" + targetName + " has been unfrozen."), true);
        } else {
            // Freeze
            frozenPlayers.add(playerId);
            targetPlayer.sendSystemMessage(Component.literal("§cYou have been frozen by a staff member!"));
            source.sendSuccess(() -> Component.literal("§c" + targetName + " has been frozen."), true);
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
