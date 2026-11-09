package com.togun.tmod.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.togun.tmod.skin.SkinManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;

public class SkinCommand {

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
            // /skin set <playerName> - Set your skin to match another player
            dispatcher.register(Commands.literal("skin")
                    .then(Commands.literal("set")
                            .then(Commands.argument("playerName", StringArgumentType.string())
                                    .executes(SkinCommand::setSkinSelf)
                                    .then(Commands.argument("target", StringArgumentType.string())
                                            .suggests(PLAYER_SUGGESTIONS)
                                            .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions
                                                    .check(source, "tmod.command.skin.other", 4))
                                            .executes(SkinCommand::setSkinOther))))

                    // /skin clear - Clear your custom skin
                    .then(Commands.literal("clear")
                            .executes(SkinCommand::clearSkinSelf)
                            .then(Commands.argument("target", StringArgumentType.string())
                                    .suggests(PLAYER_SUGGESTIONS)
                                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                                            "tmod.command.skin.other", 4))
                                    .executes(SkinCommand::clearSkinOther)))

                    // /skin update - Refresh your skin
                    .then(Commands.literal("update")
                            .executes(SkinCommand::updateSkinSelf)
                            .then(Commands.argument("target", StringArgumentType.string())
                                    .suggests(PLAYER_SUGGESTIONS)
                                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                                            "tmod.command.skin.other", 4))
                                    .executes(SkinCommand::updateSkinOther))));
        });
    }

    /**
     * Set skin for self
     */
    private static int setSkinSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        String targetPlayerName = StringArgumentType.getString(context, "playerName");

        source.sendSuccess(() -> Component.literal("§eFetching skin for " + targetPlayerName + "..."), false);

        SkinManager.setSkin(player, targetPlayerName).thenAccept(success -> {
            if (success) {
                source.sendSuccess(
                        () -> Component.literal(
                                "§aSkin updated to " + targetPlayerName + "'s skin! §eReconnect to see changes."),
                        false);
            } else {
                source.sendFailure(
                        Component.literal("§cFailed to fetch skin for " + targetPlayerName + ". Player may not exist."));
            }
        });

        return 1;
    }

    /**
     * Set skin for another player (requires op)
     */
    private static int setSkinOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String targetPlayerName = StringArgumentType.getString(context, "playerName");
        String targetName = StringArgumentType.getString(context, "target");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), targetName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("§cPlayer '" + targetName + "' not found!"));
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§eFetching skin for " + targetPlayerName + "..."), false);

        SkinManager.setSkin(targetPlayer, targetPlayerName).thenAccept(success -> {
            if (success) {
                source.sendSuccess(() -> Component.literal("§aUpdated " + targetPlayer.getName().getString() + "'s skin to "
                        + targetPlayerName + "'s skin!"), false);
                targetPlayer.sendSystemMessage(Component.literal("§aYour skin has been updated to " + targetPlayerName
                        + "'s skin! §eReconnect to see changes."));
            } else {
                source.sendFailure(
                        Component.literal("§cFailed to fetch skin for " + targetPlayerName + ". Player may not exist."));
            }
        });

        return 1;
    }

    /**
     * Clear skin for self
     */
    private static int clearSkinSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        SkinManager.clearSkin(player);
        source.sendSuccess(() -> Component.literal("§aYour skin has been restored to default! §eReconnect to see changes."),
                false);

        return 1;
    }

    /**
     * Clear skin for another player (requires op)
     */
    private static int clearSkinOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String targetName = StringArgumentType.getString(context, "target");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), targetName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("§cPlayer '" + targetName + "' not found!"));
            return 0;
        }

        SkinManager.clearSkin(targetPlayer);
        source.sendSuccess(() -> Component.literal("§aCleared " + targetPlayer.getName().getString() + "'s custom skin!"),
                false);
        targetPlayer.sendSystemMessage(Component.literal("§aYour skin has been restored to default! §eReconnect to see changes."));

        return 1;
    }

    /**
     * Update/refresh skin for self
     */
    private static int updateSkinSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        SkinManager.restoreSkin(player);
        source.sendSuccess(() -> Component.literal("§aSkin refreshed!"), false);

        return 1;
    }

    /**
     * Update/refresh skin for another player (requires op)
     */
    private static int updateSkinOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String targetName = StringArgumentType.getString(context, "target");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), targetName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("§cPlayer '" + targetName + "' not found!"));
            return 0;
        }

        SkinManager.restoreSkin(targetPlayer);
        source.sendSuccess(() -> Component.literal("§aRefreshed " + targetPlayer.getName().getString() + "'s skin!"),
                false);

        return 1;
    }

    private static ServerPlayer findPlayer(MinecraftServer server, String playerName) {
        return server.getPlayerList().getPlayerByName(playerName);
    }
}
