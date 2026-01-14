package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.togun.tmod.config.TutilsConfigManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class TutilsCommand {

    private static final SuggestionProvider<ServerCommandSource> BOOLEAN_CONFIG_KEY_SUGGESTIONS = (context,
            builder) -> {
        return CommandSource.suggestMatching(
                new String[] {
                        TutilsConfigManager.VILLAGER_INFINITE_RESTOCKS,
                        TutilsConfigManager.VILLAGER_FASTER_BREEDING,
                        TutilsConfigManager.ANVIL_NOT_EXPENSIVE
                },
                builder);
    };

    private static final SuggestionProvider<ServerCommandSource> INTEGER_CONFIG_KEY_SUGGESTIONS = (context,
            builder) -> {
        return CommandSource.suggestMatching(
                new String[] {
                        TutilsConfigManager.REDSTONE_HOPPER_TICKS
                },
                builder);
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerTutils(dispatcher, registryAccess);
        });
    }

    private static void registerTutils(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("tutils")
                // Base command: /tutils
                .executes(TutilsCommand::execute)

                // /tutils cfg <key> [value] - handles both querying and setting configs
                .then(CommandManager.literal("cfg")
                        .then(CommandManager.argument("configKey", StringArgumentType.string())
                                .suggests((context, builder) -> {
                                    // Suggest both boolean and integer keys
                                    CommandSource.suggestMatching(
                                            new String[] {
                                                    TutilsConfigManager.VILLAGER_INFINITE_RESTOCKS,
                                                    TutilsConfigManager.VILLAGER_FASTER_BREEDING,
                                                    TutilsConfigManager.ANVIL_NOT_EXPENSIVE,
                                                    TutilsConfigManager.REDSTONE_HOPPER_TICKS
                                            },
                                            builder);
                                    return builder.buildFuture();
                                })
                                .executes(context -> executeCfgQuery(context))
                                .then(CommandManager.argument("value", StringArgumentType.string())
                                        .executes(context -> executeCfg(context)))))

                // /tutils repeatcmd <command> <howmanytimes>
                .then(CommandManager.literal("repeatcmd")
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                                .then(CommandManager.argument("times", IntegerArgumentType.integer(1, 1000))
                                        .executes(context -> executeRepeatCmd(context))))));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> Text.literal("SMACK A51 boss"), false);

        return 1;
    }

    private static int executeCfgQuery(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "configKey");

        // Handle Boolean Configs
        if (TutilsConfigManager.isValidBooleanKey(key)) {
            boolean value = TutilsConfigManager.getConfigValue(key);
            String status = value ? "§aenabled" : "§cdisabled";
            source.sendFeedback(() -> Text.literal(key + " §7is currently " + status), false);
            return 1;
        }

        // Handle Integer Configs
        if (TutilsConfigManager.isValidIntegerKey(key)) {
            int value = TutilsConfigManager.getIntConfigValue(key, 0); // Default doesn't matter here as we know key is
                                                                       // valid
            source.sendFeedback(() -> Text.literal(key + " §7is currently set to §a" + value), false);
            return 1;
        }

        // Invalid Key
        source.sendError(Text.literal("§cInvalid config key: " + key));
        return 0;
    }

    private static int executeCfg(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "configKey");
        String valueStr = StringArgumentType.getString(context, "value");

        // Handle Boolean Configs
        if (TutilsConfigManager.isValidBooleanKey(key)) {
            boolean value;
            if (valueStr.equalsIgnoreCase("true")) {
                value = true;
            } else if (valueStr.equalsIgnoreCase("false")) {
                value = false;
            } else {
                source.sendError(Text.literal("§cInvalid boolean value: " + valueStr));
                source.sendFeedback(() -> Text.literal("§7Expected 'true' or 'false'"), false);
                return 0;
            }

            TutilsConfigManager.setConfigValue(key, value);
            String status = value ? "§aenabled" : "§cdisabled";
            source.sendFeedback(() -> Text.literal(key + " §7is now " + status), false);
            return 1;
        }

        // Handle Integer Configs
        if (TutilsConfigManager.isValidIntegerKey(key)) {
            try {
                int value = Integer.parseInt(valueStr);

                // Validate range for hopper ticks specifically (or general int validation)
                if (key.equals(TutilsConfigManager.REDSTONE_HOPPER_TICKS)) {
                    if (value < 1 || value > 20) {
                        source.sendError(Text.literal("§cInvalid range. Expected 1-20."));
                        return 0;
                    }
                }

                TutilsConfigManager.setIntConfigValue(key, value);
                source.sendFeedback(() -> Text.literal(key + "§7 is now set to §a" + value), false);
                return 1;
            } catch (NumberFormatException e) {
                source.sendError(Text.literal("§cInvalid integer value: " + valueStr));
                return 0;
            }
        }

        // Invalid Key
        source.sendError(Text.literal("§cInvalid config key: " + key));
        source.sendFeedback(
                () -> Text.literal(
                        "§7Valid keys: villager.infiniteRestocks, villager.fasterBreeding, anvil.notExpensive, redstone.hopperTicks"),
                false);
        return 0;
    }

    private static int executeRepeatCmd(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String commandInput = StringArgumentType.getString(context, "command");
        int times = IntegerArgumentType.getInteger(context, "times");

        // Remove leading slash if present
        final String command = commandInput.startsWith("/") ? commandInput.substring(1) : commandInput;

        final int[] successCount = { 0 };
        final int[] failCount = { 0 };

        CommandDispatcher<ServerCommandSource> dispatcher = source.getServer().getCommandManager().getDispatcher();

        for (int i = 0; i < times; i++) {
            try {
                int result = dispatcher.execute(command, source);
                if (result > 0) {
                    successCount[0]++;
                } else {
                    failCount[0]++;
                }
            } catch (CommandSyntaxException e) {
                failCount[0]++;
            } catch (Exception e) {
                failCount[0]++;
            }
        }

        final int finalSuccess = successCount[0];
        final int finalFail = failCount[0];

        source.sendFeedback(() -> Text.literal(
                "§7Executed command §f" + command + " §7" + times + " times " +
                        "§7(§a" + finalSuccess + " §7successful, §c" + finalFail + " §7failed)"),
                false);

        return 1;
    }
}
