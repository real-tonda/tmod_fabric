package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class BroadcastCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerBroadcast(dispatcher, registryAccess);
        });
    }

    private static void registerBroadcast(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
        // Main command: /broadcast
        dispatcher.register(CommandManager.literal("broadcast")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.broadcast", 4))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeBroadcast(context))));

        // Alias: /bc
        dispatcher.register(CommandManager.literal("bc")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.broadcast", 4))
                .then(CommandManager.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeBroadcast(context))));
    }

    private static int executeBroadcast(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String message = StringArgumentType.getString(context, "message");

        // Get broadcaster name
        String broadcasterName;
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            broadcasterName = player.getName().getString();
        } catch (Exception e) {
            broadcasterName = "Server";
        }

        // Format: [Broadcast] <Broadcaster> message
        String formattedMessage = "§6§l[BROADCAST] §r§e<" + broadcasterName + "> §f" + message;

        // Send to all players
        for (ServerPlayerEntity player : source.getServer().getPlayerManager().getPlayerList()) {
            player.sendMessage(Text.literal(formattedMessage), false);
        }

        // Also log to console
        source.getServer().sendMessage(Text.literal(formattedMessage));

        return 1;
    }
}
