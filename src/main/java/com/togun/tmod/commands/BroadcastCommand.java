package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

public class BroadcastCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerBroadcast(dispatcher, registryAccess);
        });
    }

    private static void registerBroadcast(CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess) {
        // Main command: /broadcast
        dispatcher.register(Commands.literal("broadcast")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.broadcast", 4))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeBroadcast(context))));

        // Alias: /bc
        dispatcher.register(Commands.literal("bc")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.broadcast", 4))
                .then(Commands.argument("message", StringArgumentType.greedyString())
                        .executes(context -> executeBroadcast(context))));
    }

    private static int executeBroadcast(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String message = StringArgumentType.getString(context, "message");

        // Get broadcaster name
        String broadcasterName;
        try {
            ServerPlayer player = source.getPlayerOrException();
            broadcasterName = player.getName().getString();
        } catch (Exception e) {
            broadcasterName = "Server";
        }

        // Format: [Broadcast] <Broadcaster> message
        String formattedMessage = "§6§l[BROADCAST] §r§e<" + broadcasterName + "> §f" + message;

        // Send to all players
        for (ServerPlayer player : source.getServer().getPlayerList().getPlayers()) {
            player.sendSystemMessage(Component.literal(formattedMessage));
        }

        // Also log to console
        source.getServer().sendSystemMessage(Component.literal(formattedMessage));

        return 1;
    }
}
