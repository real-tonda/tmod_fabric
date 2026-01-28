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

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import me.lucko.fabric.api.permissions.v0.Permissions;

public class GodCommand {
    private static final Set<UUID> godModePlayers = new HashSet<>();

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
            dispatcher.register(CommandManager.literal("god")
                    .requires(source -> Permissions.check(source, "tmod.command.god", 4))
                    .executes(GodCommand::executeSelf)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GodCommand::executeOther)));
        });
    }

    private static int executeSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }

        return toggleGodMode(player, source, player.getName().getString());
    }

    private static int executeOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);

        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        return toggleGodMode(targetPlayer, source, targetPlayer.getName().getString());
    }

    private static int toggleGodMode(ServerPlayerEntity player, ServerCommandSource source, String playerName) {
        UUID playerId = player.getUuid();
        boolean hasGodMode = godModePlayers.contains(playerId);

        if (hasGodMode) {
            // Disable god mode
            godModePlayers.remove(playerId);

            source.sendFeedback(() -> Text.literal("§cGod mode disabled for " + playerName + "."), false);
        } else {
            // Enable god mode (player can take damage but never goes below 0.5 hearts)
            godModePlayers.add(playerId);

            source.sendFeedback(() -> Text.literal("§aGod mode enabled for " + playerName + "! (Minimum 0.5 hearts)"),
                    false);
        }

        return 1;
    }

    public static boolean hasGodMode(UUID playerId) {
        return godModePlayers.contains(playerId);
    }
}
