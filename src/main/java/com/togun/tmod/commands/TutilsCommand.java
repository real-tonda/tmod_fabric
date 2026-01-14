package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
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

                // /tutils cfg <key> <true|false> - for boolean configs
                .then(CommandManager.literal("cfg")
                        .then(CommandManager.argument("key", StringArgumentType.string())
                                .suggests(BOOLEAN_CONFIG_KEY_SUGGESTIONS)
                                .then(CommandManager.argument("value", BoolArgumentType.bool())
                                        .executes(context -> executeCfgBoolean(context))))
                        .then(CommandManager.argument("intKey", StringArgumentType.string())
                                .suggests(INTEGER_CONFIG_KEY_SUGGESTIONS)
                                .then(CommandManager.argument("intValue", IntegerArgumentType.integer(1, 20))
                                        .executes(context -> executeCfgInteger(context)))))

                // /tutils repeatcmd <command> <howmanytimes>
                .then(CommandManager.literal("repeatcmd")
                        .then(CommandManager.argument("command", StringArgumentType.greedyString())
                                .then(CommandManager.argument("times", IntegerArgumentType.integer(1, 1000))
                                        .executes(context -> executeRepeatCmd(context))))));
    }

    private static int execute(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();

        source.sendFeedback(() -> Text.literal("SMACKA51"), false);

        return 1;
    }

    private static int executeCfgBoolean(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "key");
        boolean value = BoolArgumentType.getBool(context, "value");

        if (!TutilsConfigManager.isValidBooleanKey(key)) {
            source.sendError(Text.literal("§cInvalid boolean config key: " + key));
            source.sendFeedback(
                    () -> Text.literal(
                            "§7Valid keys: villager.infiniteRestocks, villager.fasterBreeding, anvil.notExpensive"),
                    false);
            return 0;
        }

        TutilsConfigManager.setConfigValue(key, value);

        String status = value ? "§aenabled" : "§cdisabled";
        source.sendFeedback(() -> Text.literal("§7Config §f" + key + " §7has been " + status), false);

        return 1;
    }

    private static int executeCfgInteger(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String key = StringArgumentType.getString(context, "intKey");
        int value = IntegerArgumentType.getInteger(context, "intValue");

        if (!TutilsConfigManager.isValidIntegerKey(key)) {
            source.sendError(Text.literal("§cInvalid integer config key: " + key));
            source.sendFeedback(
                    () -> Text.literal("§7Valid keys: redstone.hopperTicks"),
                    false);
            return 0;
        }

        TutilsConfigManager.setIntConfigValue(key, value);

        source.sendFeedback(() -> Text.literal("§7Config §f" + key + "§7 has been set to §a" + value), false);

        return 1;
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
