package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.togun.tmod.combat.CombatTagManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

public class ACLCommand {
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerACL(dispatcher, registryAccess);
        });
    }
    
    private static void registerACL(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        // /acl <on|off> - Toggle ACL system
        dispatcher.register(CommandManager.literal("acl")
            .requires(source -> source.hasPermissionLevel(4))
            .then(CommandManager.argument("state", StringArgumentType.word())
                .suggests((context, builder) -> {
                    builder.suggest("on");
                    builder.suggest("off");
                    return builder.buildFuture();
                })
                .executes(context -> executeToggle(context))
            )
            .executes(context -> executeStatus(context))
        );
        
        // /combat - Check your combat status
        dispatcher.register(CommandManager.literal("combat")
            .executes(context -> executeCombatCheck(context))
        );
    }
    
    private static int executeToggle(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String state = StringArgumentType.getString(context, "state").toLowerCase();
        
        if (state.equals("on")) {
            if (CombatTagManager.isEnabled()) {
                source.sendError(Text.literal("§cACL is already enabled!"));
                return 0;
            }
            
            CombatTagManager.enable();
            source.sendFeedback(() -> Text.literal("§a§l✓ §aAnti-Combat Log system is now §lENABLED"), true);
            source.sendFeedback(() -> Text.literal("§7Players who log out during combat will be killed"), false);
            return 1;
            
        } else if (state.equals("off")) {
            if (!CombatTagManager.isEnabled()) {
                source.sendError(Text.literal("§cACL is already disabled!"));
                return 0;
            }
            
            CombatTagManager.disable();
            source.sendFeedback(() -> Text.literal("§c§l✗ §cAnti-Combat Log system is now §lDISABLED"), true);
            source.sendFeedback(() -> Text.literal("§7All combat tags have been cleared"), false);
            return 1;
            
        } else {
            source.sendError(Text.literal("§cInvalid argument! Use: §e/acl <on|off>"));
            return 0;
        }
    }
    
    private static int executeStatus(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        boolean enabled = CombatTagManager.isEnabled();
        
        String status = enabled ? "§a§lENABLED" : "§c§lDISABLED";
        
        source.sendFeedback(() -> Text.literal("§6=== Anti-Combat Log Status ==="), false);
        source.sendFeedback(() -> Text.literal("§7Status: " + status), false);
        source.sendFeedback(() -> Text.literal("§7Combat Tag Duration: §e15 seconds"), false);
        
        if (enabled) {
            source.sendFeedback(() -> Text.literal("§7Logging out during combat will result in death"), false);
        }
        
        return 1;
    }
    
    private static int executeCombatCheck(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        try {
            ServerPlayerEntity player = source.getPlayerOrThrow();
            
            if (!CombatTagManager.isEnabled()) {
                player.sendMessage(Text.literal("§7Anti-Combat Log is currently §cdisabled"), false);
                return 0;
            }
            
            if (CombatTagManager.isInCombat(player)) {
                int remaining = CombatTagManager.getRemainingTime(player);
                player.sendMessage(Text.literal("§c§l⚔ §cYou are in combat! §7(§e" + remaining + "s §7remaining)"), false);
                player.sendMessage(Text.literal("§7§oDo not log out or you will die!"), false);
                return 1;
            } else {
                player.sendMessage(Text.literal("§a§l✓ §aYou are not in combat"), false);
                return 1;
            }
            
        } catch (Exception e) {
            source.sendError(Text.literal("§cThis command can only be used by players!"));
            return 0;
        }
    }
}

