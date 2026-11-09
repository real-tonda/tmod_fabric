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

public class PingCommand {

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
            dispatcher.register(Commands.literal("ping")
                    .executes(PingCommand::executeSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(PingCommand::executeOther)));
        });
    }

    private static int executeSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        int ping = player.connection.latency();
        String pingColor = getPingColor(ping);

        source.sendSuccess(() -> Component.literal(
                "§7Your ping: " + pingColor + ping + "ms"), false);

        return 1;
    }

    private static int executeOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = source.getServer().getPlayerList().getPlayerByName(playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        int ping = targetPlayer.connection.latency();
        String pingColor = getPingColor(ping);
        String targetName = targetPlayer.getName().getString();

        source.sendSuccess(() -> Component.literal(
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
