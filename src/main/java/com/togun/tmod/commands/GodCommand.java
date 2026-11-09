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

public class GodCommand {
    private static final Set<UUID> godModePlayers = new HashSet<>();

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
            dispatcher.register(Commands.literal("god")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source, "tmod.command.god",
                            4))
                    .executes(GodCommand::executeSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GodCommand::executeOther)));
        });
    }

    private static int executeSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        return toggleGodMode(player, source, player.getName().getString());
    }

    private static int executeOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);

        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        return toggleGodMode(targetPlayer, source, targetPlayer.getName().getString());
    }

    private static int toggleGodMode(ServerPlayer player, CommandSourceStack source, String playerName) {
        UUID playerId = player.getUUID();
        boolean hasGodMode = godModePlayers.contains(playerId);

        if (hasGodMode) {
            // Disable god mode
            godModePlayers.remove(playerId);

            source.sendSuccess(() -> Component.literal("§cGod mode disabled for " + playerName + "."), false);
        } else {
            // Enable god mode (player can take damage but never goes below 0.5 hearts)
            godModePlayers.add(playerId);

            source.sendSuccess(() -> Component.literal("§aGod mode enabled for " + playerName + "! (Minimum 0.5 hearts)"),
                    false);
        }

        return 1;
    }

    public static boolean hasGodMode(UUID playerId) {
        return godModePlayers.contains(playerId);
    }
}
