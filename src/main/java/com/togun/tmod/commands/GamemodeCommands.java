package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.world.GameMode;
import net.minecraft.server.MinecraftServer;

public class GamemodeCommands {
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
            // /gmc - Creative mode
            dispatcher.register(CommandManager.literal("gmc")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setCreativeSelf)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setCreativeOther)));

            // /gms - Survival mode
            dispatcher.register(CommandManager.literal("gms")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setSurvivalSelf)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setSurvivalOther)));

            // /gma - Adventure mode
            dispatcher.register(CommandManager.literal("gma")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setAdventureSelf)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setAdventureOther)));

            // /gmsp - Spectator mode
            dispatcher.register(CommandManager.literal("gmsp")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                            "tmod.command.gamemode", 2))
                    .executes(GamemodeCommands::setSpectatorSelf)
                    .then(CommandManager.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(GamemodeCommands::setSpectatorOther)));
        });
    }

    // Self methods
    private static int setCreativeSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.changeGameMode(GameMode.CREATIVE);
        source.sendFeedback(() -> Text.literal("Game mode set to Creative."), false);

        return 1;
    }

    private static int setSurvivalSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.changeGameMode(GameMode.SURVIVAL);
        source.sendFeedback(() -> Text.literal("Game mode set to Survival."), false);

        return 1;
    }

    private static int setAdventureSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.changeGameMode(GameMode.ADVENTURE);
        source.sendFeedback(() -> Text.literal("Game mode set to Adventure."), false);

        return 1;
    }

    private static int setSpectatorSelf(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return 0;
        }

        player.changeGameMode(GameMode.SPECTATOR);
        source.sendFeedback(() -> Text.literal("Game mode set to Spectator."), false);

        return 1;
    }

    // Other player methods
    private static int setCreativeOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.changeGameMode(GameMode.CREATIVE);
        source.sendFeedback(
                () -> Text.literal("Game mode set to Creative for " + targetPlayer.getName().getString() + "."), false);

        return 1;
    }

    private static int setSurvivalOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.changeGameMode(GameMode.SURVIVAL);
        source.sendFeedback(
                () -> Text.literal("Game mode set to Survival for " + targetPlayer.getName().getString() + "."), false);

        return 1;
    }

    private static int setAdventureOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.changeGameMode(GameMode.ADVENTURE);
        source.sendFeedback(
                () -> Text.literal("Game mode set to Adventure for " + targetPlayer.getName().getString() + "."),
                false);

        return 1;
    }

    private static int setSpectatorOther(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayerEntity targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendError(Text.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        targetPlayer.changeGameMode(GameMode.SPECTATOR);
        source.sendFeedback(
                () -> Text.literal("Game mode set to Spectator for " + targetPlayer.getName().getString() + "."),
                false);

        return 1;
    }

    private static ServerPlayerEntity findPlayer(MinecraftServer server, String playerName) {
        return server.getPlayerManager().getPlayer(playerName);
    }
}
