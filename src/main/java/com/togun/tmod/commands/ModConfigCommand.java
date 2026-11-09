package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.togun.tmod.config.ModConfigManager;
import com.togun.tmod.config.ModConfigManager.ModFeature;
import com.togun.tmod.config.XaeroEffectManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.network.chat.Component;
import me.lucko.fabric.api.permissions.v0.Permissions;

import java.util.Arrays;
import java.util.Map;

public class ModConfigCommand {

    private static final SuggestionProvider<CommandSourceStack> FEATURE_SUGGESTIONS = (context, builder) -> {
        return SharedSuggestionProvider.suggest(
                Arrays.stream(ModFeature.values())
                        .map(f -> f.getConfigKey()),
                builder);
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerModConfig(dispatcher, registryAccess);
        });
    }

    private static void registerModConfig(CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("modcfg")
                .requires(source -> Permissions.check(source, "tmod.command.modcfg", 4))

                // /modcfg list - Show all features and their states
                .then(Commands.literal("list")
                        .executes(context -> executeList(context)))

                // /modcfg enable <feature> - Enable a feature
                .then(Commands.literal("enable")
                        .then(Commands.argument("feature", StringArgumentType.string())
                                .suggests(FEATURE_SUGGESTIONS)
                                .executes(context -> executeEnable(context))))

                // /modcfg disable <feature> - Disable a feature
                .then(Commands.literal("disable")
                        .then(Commands.argument("feature", StringArgumentType.string())
                                .suggests(FEATURE_SUGGESTIONS)
                                .executes(context -> executeDisable(context))))

                // /modcfg toggle <feature> - Toggle a feature
                .then(Commands.literal("toggle")
                        .then(Commands.argument("feature", StringArgumentType.string())
                                .suggests(FEATURE_SUGGESTIONS)
                                .executes(context -> executeToggle(context))))

                // /modcfg info <feature> - Show info about a feature
                .then(Commands.literal("info")
                        .then(Commands.argument("feature", StringArgumentType.string())
                                .suggests(FEATURE_SUGGESTIONS)
                                .executes(context -> executeInfo(context)))));
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Map<ModFeature, Boolean> features = ModConfigManager.getAllFeatures();

        source.sendSuccess(() -> Component.literal("§6§l=== Mod Feature Configuration ==="), false);
        source.sendSuccess(() -> Component.literal("§7Features that are §cdisabled §7will not work for players."), false);
        source.sendSuccess(() -> Component.literal(""), false);

        for (Map.Entry<ModFeature, Boolean> entry : features.entrySet()) {
            ModFeature feature = entry.getKey();
            boolean enabled = entry.getValue();

            String status = enabled ? "§a✓ Enabled" : "§c✗ Disabled";
            String featureName = "§e" + feature.getDisplayName();
            String configKey = "§7[" + feature.getConfigKey() + "]";

            source.sendSuccess(() -> Component.literal(status + " §8| " + featureName + " " + configKey), false);
        }

        source.sendSuccess(() -> Component.literal(""), false);
        source.sendSuccess(() -> Component.literal("§7Use §f/modcfg enable/disable <feature>§7 to change settings"), false);

        return 1;
    }

    private static int executeEnable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String featureKey = StringArgumentType.getString(context, "feature");

        ModFeature feature = ModFeature.fromConfigKey(featureKey);
        if (feature == null) {
            source.sendFailure(Component.literal("§cUnknown feature: " + featureKey));
            source.sendSuccess(() -> Component.literal("§7Use §f/modcfg list§7 to see available features"), false);
            return 0;
        }

        if (ModConfigManager.isFeatureEnabled(feature)) {
            source.sendSuccess(() -> Component.literal("§e" + feature.getDisplayName() + " §7is already enabled"), false);
            return 0;
        }

        ModConfigManager.enableFeature(feature);

        // Immediately apply changes to all online players
        XaeroEffectManager.refreshFeatureForAllPlayers(feature, source.getServer());

        source.sendSuccess(() -> Component.literal("§a✓ Enabled: §e" + feature.getDisplayName()), true);
        source.sendSuccess(() -> Component.literal("§7Changes applied immediately to all online players"), false);

        return 1;
    }

    private static int executeDisable(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String featureKey = StringArgumentType.getString(context, "feature");

        ModFeature feature = ModFeature.fromConfigKey(featureKey);
        if (feature == null) {
            source.sendFailure(Component.literal("§cUnknown feature: " + featureKey));
            source.sendSuccess(() -> Component.literal("§7Use §f/modcfg list§7 to see available features"), false);
            return 0;
        }

        if (!ModConfigManager.isFeatureEnabled(feature)) {
            source.sendSuccess(() -> Component.literal("§e" + feature.getDisplayName() + " §7is already disabled"), false);
            return 0;
        }

        ModConfigManager.disableFeature(feature);

        // Immediately apply changes to all online players
        XaeroEffectManager.refreshFeatureForAllPlayers(feature, source.getServer());

        source.sendSuccess(() -> Component.literal("§c✗ Disabled: §e" + feature.getDisplayName()), true);
        source.sendSuccess(() -> Component.literal("§7Changes applied immediately to all online players"), false);

        return 1;
    }

    private static int executeToggle(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String featureKey = StringArgumentType.getString(context, "feature");

        ModFeature feature = ModFeature.fromConfigKey(featureKey);
        if (feature == null) {
            source.sendFailure(Component.literal("§cUnknown feature: " + featureKey));
            source.sendSuccess(() -> Component.literal("§7Use §f/modcfg list§7 to see available features"), false);
            return 0;
        }

        boolean newState = ModConfigManager.toggleFeature(feature);

        // Immediately apply changes to all online players
        XaeroEffectManager.refreshFeatureForAllPlayers(feature, source.getServer());

        String status = newState ? "§a✓ Enabled" : "§c✗ Disabled";

        source.sendSuccess(() -> Component.literal(status + ": §e" + feature.getDisplayName()), true);
        source.sendSuccess(() -> Component.literal("§7Changes applied immediately to all online players"), false);

        return 1;
    }

    private static int executeInfo(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String featureKey = StringArgumentType.getString(context, "feature");

        ModFeature feature = ModFeature.fromConfigKey(featureKey);
        if (feature == null) {
            source.sendFailure(Component.literal("§cUnknown feature: " + featureKey));
            return 0;
        }

        boolean enabled = ModConfigManager.isFeatureEnabled(feature);
        String status = enabled ? "§a✓ Enabled" : "§c✗ Disabled";

        source.sendSuccess(() -> Component.literal("§6=== Feature Information ==="), false);
        source.sendSuccess(() -> Component.literal("§7Name: §e" + feature.getDisplayName()), false);
        source.sendSuccess(() -> Component.literal("§7Config Key: §f" + feature.getConfigKey()), false);
        source.sendSuccess(() -> Component.literal("§7Status: " + status), false);

        // Add feature-specific descriptions
        String description = getFeatureDescription(feature);
        if (description != null) {
            source.sendSuccess(() -> Component.literal("§7Description: §f" + description), false);
        }

        return 1;
    }

    private static String getFeatureDescription(ModFeature feature) {
        return switch (feature) {
            case XAERO_ENTITY_RADAR -> "Disables entity radar functionality on Xaero's Minimap for all players";
            case XAERO_CAVE_MODE -> "Disables cave map mode on Xaero's Minimap";
            case XAERO_WAYPOINTS -> "Disables waypoint functionality on Xaero's Minimap";
        };
    }
}
