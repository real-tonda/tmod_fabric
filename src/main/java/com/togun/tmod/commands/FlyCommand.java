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
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class FlyCommand {
    private static final Set<UUID> flyingPlayers = new HashSet<>();
    private static final String FLY_DATA_KEY = "tmod_fly_enabled";

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
            dispatcher.register(Commands.literal("fly")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source, "tmod.command.fly",
                            4)) // Requires operator status
                    // (level 4)
                    .executes(FlyCommand::executeSelf)
                    .then(Commands.argument("player", StringArgumentType.string())
                            .suggests(PLAYER_SUGGESTIONS)
                            .executes(FlyCommand::executeOther)));
        });
    }

    private static int executeSelf(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        if (!(source.getEntity() instanceof ServerPlayer player)) {
            source.sendFailure(Component.literal("This command can only be executed by a player!"));
            return 0;
        }

        return toggleFly(player, source, player.getName().getString());
    }

    private static int executeOther(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String playerName = StringArgumentType.getString(context, "player");

        ServerPlayer targetPlayer = findPlayer(source.getServer(), playerName);
        if (targetPlayer == null) {
            source.sendFailure(Component.literal("Player '" + playerName + "' not found!"));
            return 0;
        }

        return toggleFly(targetPlayer, source, targetPlayer.getName().getString());
    }

    private static int toggleFly(ServerPlayer player, CommandSourceStack source, String playerName) {
        UUID playerId = player.getUUID();
        boolean isCurrentlyFlying = flyingPlayers.contains(playerId);

        if (isCurrentlyFlying) {
            // Disable fly
            flyingPlayers.remove(playerId);
            player.getAbilities().mayfly = false;
            player.getAbilities().flying = false;
            player.onUpdateAbilities();

            // Save to player data
            saveFlyState(player, false);

            source.sendSuccess(() -> Component.literal("Fly mode disabled for " + playerName + "."), false);
        } else {
            // Enable fly
            flyingPlayers.add(playerId);
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();

            // Save to player data
            saveFlyState(player, true);

            source.sendSuccess(() -> Component.literal("Fly mode enabled for " + playerName + "! They can now fly freely."),
                    false);
        }

        return 1;
    }

    private static ServerPlayer findPlayer(MinecraftServer server, String playerName) {
        return server.getPlayerList().getPlayerByName(playerName);
    }

    private static void saveFlyState(ServerPlayer player, boolean enabled) {
        // For now, we'll just use in-memory storage
        // The fly state will persist during the server session
        // but will reset on server restart (this is acceptable for most use cases)
    }

    public static void restoreFlyState(ServerPlayer player) {
        // For now, we'll just check if the player is already in our flying set
        // This provides session persistence but not server restart persistence
        UUID playerId = player.getUUID();
        if (flyingPlayers.contains(playerId)) {
            player.getAbilities().mayfly = true;
            player.getAbilities().flying = true;
            player.onUpdateAbilities();
        }
    }

    public static boolean isPlayerFlying(UUID playerId) {
        return flyingPlayers.contains(playerId);
    }
}