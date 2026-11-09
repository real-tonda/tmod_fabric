package com.togun.tmod.commands;

import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;

public class TpsCommand {

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(Commands.literal("tps")
                    .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source, "tmod.command.tps",
                            0)) // Anyone can use
                    .executes(TpsCommand::execute));
        });
    }

    private static int execute(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        // getAverageTickTimeNanos returns nanoseconds in modern Minecraft
        double avgTickTimeMs = source.getServer().getAverageTickTimeNanos() / 1_000_000.0;
        double tps = Math.min(20.0, 1000.0 / avgTickTimeMs);

        // Format TPS with color coding
        String tpsColor;
        if (tps >= 19.0) {
            tpsColor = "§a"; // Green - Excellent
        } else if (tps >= 17.0) {
            tpsColor = "§e"; // Yellow - Good
        } else if (tps >= 15.0) {
            tpsColor = "§6"; // Gold - Fair
        } else {
            tpsColor = "§c"; // Red - Poor
        }

        source.sendSuccess(() -> Component.literal(
                "§7[§bTPS§7] §fCurrent TPS: " + tpsColor + String.format("%.2f", tps) +
                        " §7(Avg Tick: §f" + String.format("%.2f", avgTickTimeMs) + "ms§7)"),
                false);

        return 1;
    }
}
