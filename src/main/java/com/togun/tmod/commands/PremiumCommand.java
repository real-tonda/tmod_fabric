package com.togun.tmod.commands;

import com.mojang.brigadier.context.CommandContext;
import com.togun.tmod.skin.SkinManager;
import com.togun.tmod.util.PremiumManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

/**
 * /premium - lock your name so it can only be joined with a real Mojang login
 * (offline mode only). /premium off - remove the lock.
 */
public class PremiumCommand {
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("premium")
                    .executes(PremiumCommand::lock)
                    .then(CommandManager.literal("off").executes(PremiumCommand::unlock)));
        });
    }

    private static ServerPlayerEntity checkedPlayer(ServerCommandSource source) {
        if (source.getServer().isOnlineMode()) {
            source.sendError(Text.literal("/premium is only available when the server is in offline mode."));
            return null;
        }
        if (!(source.getEntity() instanceof ServerPlayerEntity player)) {
            source.sendError(Text.literal("This command can only be executed by a player!"));
            return null;
        }
        return player;
    }

    private static int lock(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = checkedPlayer(source);
        if (player == null) {
            return 0;
        }
        String name = player.getName().getString();
        if (PremiumManager.isLocked(name)) {
            source.sendError(Text.literal("Your account is already locked. Use /premium off to remove the lock."));
            return 0;
        }

        // Locking a name that isn't a real Mojang account would lock everyone out of it
        SkinManager.fetchUUID(name).thenAccept(uuid -> source.getServer().execute(() -> {
            if (uuid == null) {
                source.sendError(Text.literal("§c'" + name + "' is not a premium Minecraft account"
                        + " (or Mojang could not be reached). Nothing was locked."));
                return;
            }
            PremiumManager.setLocked(name, true);
            source.sendFeedback(() -> Text.literal("§aAccount locked! '" + name
                    + "' can now only be joined with a real Minecraft account."), false);
        }));
        return 1;
    }

    private static int unlock(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = checkedPlayer(source);
        if (player == null) {
            return 0;
        }
        String name = player.getName().getString();
        if (!PremiumManager.setLocked(name, false)) {
            source.sendError(Text.literal("Your account is not locked."));
            return 0;
        }
        source.sendFeedback(() -> Text.literal("§eAccount unlocked. Anyone can join as '" + name + "' again."), false);
        return 1;
    }
}
