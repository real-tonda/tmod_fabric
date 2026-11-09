package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameType;
import net.minecraft.server.MinecraftServer;

public class GamemodeCommands {
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
            // /gmc - Creative mode
            dispatcher.register(Commands.literal("gmc")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setCreativeSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setCreativeOther)));

            // /gms - Survival mode
            dispatcher.register(Commands.literal("gms")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setSurvivalSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setSurvivalOther)));

            // /gma - Adventure mode
            dispatcher.register(Commands.literal("gma")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setAdventureSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setAdventureOther)));

            // /gmsp - Spectator mode
            dispatcher.register(Commands.literal("gmsp")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setSpectatorSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setSpectatorOther)));
        });
    }

    // Self methods
    private static int setCreativeSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.setGameMode(GameType.CREATIVE);
        source.sendSuccess(() -> Component.literal("Game mode set to Creative."), false);

        return 1;
    }

    private static int setSurvivalSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.setGameMode(GameType.SURVIVAL);
        source.sendSuccess(() -> Component.literal("Game mode set to Survival."), false);

        return 1;
    }

    private static int setAdventureSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.setGameMode(GameType.ADVENTURE);
        source.sendSuccess(() -> Component.literal("Game mode set to Adventure."), false);

        return 1;
    }

    private static int setSpectatorSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.setGameMode(GameType.SPECTATOR);
        source.sendSuccess(() -> Component.literal("Game mode set to Spectator."), false);

        return 1;
    }

    // Other player methods
    private static int setCreativeOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.setGameMode(GameType.CREATIVE);
        source.sendSuccess(
                () -> Component.literal("Game mode set to Creative for " + targetPlayer.getName().getString() + "."), false);

        return 1;
    }

    private static int setSurvivalOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.setGameMode(GameType.SURVIVAL);
        source.sendSuccess(
                () -> Component.literal("Game mode set to Survival for " + targetPlayer.getName().getString() + "."), false);

        return 1;
    }

    private static int setAdventureOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.setGameMode(GameType.ADVENTURE);
        source.sendSuccess(
                () -> Component.literal("Game mode set to Adventure for " + targetPlayer.getName().getString() + "."),
                false);

        return 1;
    }

    private static int setSpectatorOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.setGameMode(GameType.SPECTATOR);
        source.sendSuccess(
                () -> Component.literal("Game mode set to Spectator for " + targetPlayer.getName().getString() + "."),
                false);

        return 1;
    }

    private static ServerPlayer findPlayer(MinecraftServer server, String playerName) {
        return server.getPlayerList().getPlayerByName(playerName);
    }
}
