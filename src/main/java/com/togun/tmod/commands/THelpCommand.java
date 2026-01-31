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
        // === Player Commands (Public) ===
        TMOD_COMMANDS.put("thelp", new CommandInfo("/thelp", "Show this help message", 0));
        TMOD_COMMANDS.put("tps", new CommandInfo("/tps", "Show server TPS", 0));
        TMOD_COMMANDS.put("ping", new CommandInfo("/ping [player]", "Show player latency", 0));
        TMOD_COMMANDS.put("seen", new CommandInfo("/seen <player>", "Check when player was last online", 0));
        TMOD_COMMANDS.put("skin", new CommandInfo("/skin <set|clear|update> [args]", "Manage player skins", 0));

        // === Operator Commands (Level 4) ===
        TMOD_COMMANDS.put("fly", new CommandInfo("/fly [player]", "Toggle flight mode", 4));
        TMOD_COMMANDS.put("gmc", new CommandInfo("/gmc [player]", "Set gamemode to Creative", 4));
        TMOD_COMMANDS.put("gms", new CommandInfo("/gms [player]", "Set gamemode to Survival", 4));
        TMOD_COMMANDS.put("gma", new CommandInfo("/gma [player]", "Set gamemode to Adventure", 4));
        TMOD_COMMANDS.put("gmsp", new CommandInfo("/gmsp [player]", "Set gamemode to Spectator", 4));
        TMOD_COMMANDS.put("whois", new CommandInfo("/whois <player>", "Show detailed player information", 4));
        TMOD_COMMANDS.put("freeze", new CommandInfo("/freeze <player>", "Freeze/unfreeze a player", 4));
        TMOD_COMMANDS.put("god", new CommandInfo("/god [player]", "Toggle god mode (min 0.5 hearts)", 4));
        TMOD_COMMANDS.put("broadcast", new CommandInfo("/broadcast <message>", "Send server-wide announcement", 4));
        TMOD_COMMANDS.put("bc", new CommandInfo("/bc <message>", "Alias for /broadcast", 4));
        TMOD_COMMANDS.put("dims", new CommandInfo("/dims", "List all dimensions with player counts", 4));
        TMOD_COMMANDS.put("dim", new CommandInfo("/dim <dimension>", "Teleport to a dimension", 4));
        TMOD_COMMANDS.put("blacklist",
                new CommandInfo("/blacklist <add|remove|list|clear>", "Manage item blacklist", 4));
        TMOD_COMMANDS.put("modcfg",
                new CommandInfo("/modcfg <list|enable|disable|toggle|info>", "Configure mod features", 4));
        TMOD_COMMANDS.put("tutils",
                new CommandInfo("/tutils <cfg|repeatcmd>", "Configure QOL game behavior & utilities", 4));
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
        int permLevel = getPermissionLevel(source);

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
                String color = getCommandColor(info.requiredLevel);
                String badge = getPermissionBadge(info.requiredLevel);

                helpMessage.append(color).append(info.syntax).append("§7 - §f").append(info.description);

                if (!badge.isEmpty()) {
                    helpMessage.append(" ").append(badge);
                }

                helpMessage.append("\n");
                commandCount++;
            }
        }

        if (commandCount == 0) {
            helpMessage.append("§cNo commands available.\n");
        }

        helpMessage.append("§7§m                                                    §r\n");
        helpMessage.append("§7Total commands available: §e").append(commandCount);

        source.sendFeedback(() -> Text.literal(helpMessage.toString()), false);
        return 1;
    }

    /**
     * Gets the permission level of the command source
     */
    private static int getPermissionLevel(ServerCommandSource source) {
        if (me.lucko.fabric.api.permissions.v0.Permissions.check(source, "tmod.admin", 4))
            return 4;
        return 0;
    }

    /**
     * Gets the color code for a command based on permission level
     */
    private static String getCommandColor(int requiredLevel) {
        return requiredLevel >= 4 ? "§c" : "§a"; // Red for operator, green for public
    }

    /**
     * Gets the permission badge for a command
     */
    private static String getPermissionBadge(int requiredLevel) {
        return requiredLevel >= 4 ? "§7[§cOP§7]" : "";
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
