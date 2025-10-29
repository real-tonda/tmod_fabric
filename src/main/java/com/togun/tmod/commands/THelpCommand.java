package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.CommandNode;
import com.mojang.brigadier.tree.LiteralCommandNode;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

import java.util.*;

public class THelpCommand {
    
    // List of all TMod commands with their syntax and descriptions
    private static final Map<String, CommandInfo> TMOD_COMMANDS = new LinkedHashMap<>();
    
    static {
        // Register command information
        TMOD_COMMANDS.put("fly", new CommandInfo("/fly [player]", "Toggle flight mode", 4));
        TMOD_COMMANDS.put("gmc", new CommandInfo("/gmc [player]", "Set gamemode to Creative", 4));
        TMOD_COMMANDS.put("gms", new CommandInfo("/gms [player]", "Set gamemode to Survival", 4));
        TMOD_COMMANDS.put("gma", new CommandInfo("/gma [player]", "Set gamemode to Adventure", 4));
        TMOD_COMMANDS.put("gmsp", new CommandInfo("/gmsp [player]", "Set gamemode to Spectator", 4));
        TMOD_COMMANDS.put("skin", new CommandInfo("/skin <set|clear|update> [args]", "Manage player skins", 0));
        TMOD_COMMANDS.put("tps", new CommandInfo("/tps", "Show server TPS", 0));
        TMOD_COMMANDS.put("ping", new CommandInfo("/ping [player]", "Show player latency", 0));
        TMOD_COMMANDS.put("seen", new CommandInfo("/seen <player>", "Check when player was last online", 0));
        TMOD_COMMANDS.put("whois", new CommandInfo("/whois <player>", "Show detailed player information", 4));
        TMOD_COMMANDS.put("freeze", new CommandInfo("/freeze <player>", "Freeze/unfreeze a player", 4));
        TMOD_COMMANDS.put("god", new CommandInfo("/god [player]", "Toggle god mode (min 0.5 hearts)", 4));
        TMOD_COMMANDS.put("thelp", new CommandInfo("/thelp", "Show this help message", 0));
    }
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            dispatcher.register(CommandManager.literal("thelp")
                .executes(THelpCommand::execute));
        });
    }
    
    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        // Get the player's permission level
        int permLevel = source.hasPermissionLevel(4) ? 4 : 0;
        
        StringBuilder helpMessage = new StringBuilder();
        helpMessage.append("§7§m                    §r §6§lTMod Commands §7§m                    §r\n");
        
        // Get actually registered commands from dispatcher
        CommandDispatcher<ServerCommandSource> dispatcher = source.getServer().getCommandManager().getDispatcher();
        Set<String> registeredCommands = new HashSet<>();
        
        for (CommandNode<ServerCommandSource> node : dispatcher.getRoot().getChildren()) {
            if (node instanceof LiteralCommandNode) {
                registeredCommands.add(node.getName());
            }
        }
        
        // Display commands that are both in our list and actually registered
        int commandCount = 0;
        for (Map.Entry<String, CommandInfo> entry : TMOD_COMMANDS.entrySet()) {
            String cmdName = entry.getKey();
            CommandInfo info = entry.getValue();
            
            // Only show if command is actually registered and player has permission
            if (registeredCommands.contains(cmdName) && permLevel >= info.requiredLevel) {
                String color = info.requiredLevel >= 4 ? "§c" : "§a"; // Red for op-only, green for public
                helpMessage.append(color).append(info.syntax).append("§7 - §f").append(info.description);
                
                if (info.requiredLevel >= 4) {
                    helpMessage.append(" §7[§cOP§7]");
                }
                
                helpMessage.append("\n");
                commandCount++;
            }
        }
        
        if (commandCount == 0) {
            helpMessage.append("§cNo commands available.\n");
        }
        
        helpMessage.append("§7§m                                                    §r");
        
        source.sendFeedback(() -> Text.literal(helpMessage.toString()), false);
        return 1;
    }
    
    private static class CommandInfo {
        final String syntax;
        final String description;
        final int requiredLevel;
        
        CommandInfo(String syntax, String description, int requiredLevel) {
            this.syntax = syntax;
            this.description = description;
            this.requiredLevel = requiredLevel;
        }
    }
}

