package com.togun.tmod.commands;

import com.mojang.brigadier.context.CommandContext;
import com.togun.tmod.util.TickMetrics;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TpsCommand {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("tps")
                .requires(source -> source.hasPermissionLevel(0)) // Anyone can use
                .executes(TpsCommand::execute));
        });
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        double averageTickTimeMs = TickMetrics.getAverageTickTimeMs();
        double tps = averageTickTimeMs > 0.0 ? Math.min(20.0, 1000.0 / averageTickTimeMs) : 20.0;

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
        
        source.sendFeedback(() -> Text.literal(
            "§7[§bTPS§7] §fCurrent TPS: " + tpsColor + String.format("%.2f", tps) + 
            " §7(Avg Tick: §f" + String.format("%.2f", averageTickTimeMs) + "ms§7)"
        ), false);
        
        return 1;
    }
}

