package com.togun.tmod.commands;

import com.mojang.brigadier.context.CommandContext;
import com.togun.tmod.config.TPAllowListManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TModCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("tmod")
                    .requires(source -> source.hasPermissionLevel(4))
                    .then(CommandManager.literal("reload")
                            .executes(TModCommand::executeReload)));
        });
    }

    private static int executeReload(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        try {
            TPAllowListManager.reload();
            source.sendFeedback(() -> Text.literal("§aTMod configuration reloaded successfully!"), true);
            return 1;
        } catch (Exception e) {
            source.sendError(Text.literal("§cFailed to reload configuration: " + e.getMessage()));
            return 0;
        }
    }
}
