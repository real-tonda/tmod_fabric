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

public class PingCommand {

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
            dispatcher.register(CommandManager.literal("ping")
                    .executes(PingCommand::executeSelf)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(PingCommand::executeOther)));
        });
    }

    private static int executeSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }

        int ping = player.networkHandler.getLatency();
        String pingColor = getPingColor(ping);

        source.sendFeedback(() -> Text.literal(
                "§7Your ping: " + pingColor + ping + "ms"), false);

        return 1;
    }

    private static int executeOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = source.getServer().getPlayerManager().getPlayer(playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        int ping = targetPlayer.networkHandler.getLatency();
        String pingColor = getPingColor(ping);
        String targetName = targetPlayer.getName().getString();

        source.sendFeedback(() -> Text.literal(
                "§7" + targetName + "'s ping: " + pingColor + ping + "ms"), false);

        return 1;
    }

    private static String getPingColor(int ping) {
        if (ping <= 50)
            return "§a"; // Green - Excellent
        if (ping <= 100)
            return "§e"; // Yellow - Good
        if (ping <= 150)
            return "§6"; // Gold - Fair
        if (ping <= 250)
            return "§c"; // Red - Poor
        return "§4"; // Dark Red - Very Poor
    }
}
