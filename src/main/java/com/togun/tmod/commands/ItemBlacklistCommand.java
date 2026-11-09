package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.commands.arguments.item.ItemArgument;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import java.util.Set;

public class ItemBlacklistCommand {

    private static final SuggestionProvider<CommandSourceStack> BLACKLISTED_ITEMS_SUGGESTIONS = (context, builder) -> {
        Set<String> blacklisted = ItemBlacklistManager.getBlacklistedItems();
        return SharedSuggestionProvider.suggest(blacklisted, builder);
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerItemBlacklist(dispatcher, registryAccess);
        });
    }

    private static void registerItemBlacklist(CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess) {
        dispatcher.register(Commands.literal("blacklist")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.blacklist", 4))

                // /blacklist add <item>
                .then(Commands.literal("add")
                        .then(Commands.argument("item", ItemArgument.item(registryAccess))
                                .executes(context -> executeAdd(context, registryAccess))))

                // /blacklist remove <item>
                .then(Commands.literal("remove")
                        .then(Commands.argument("item", StringArgumentType.string())
                                .suggests(BLACKLISTED_ITEMS_SUGGESTIONS)
                                .executes(context -> executeRemove(context))))

                // /blacklist list
                .then(Commands.literal("list")
                        .executes(context -> executeList(context)))

                // /blacklist clear
                .then(Commands.literal("clear")
                        .executes(context -> executeClear(context))));
    }

    private static int executeAdd(CommandContext<CommandSourceStack> context, CommandBuildContext registryAccess) {
        CommandSourceStack source = context.getSource();

        try {
            var itemStackArg = ItemArgument.getItem(context, "item");
            var item = itemStackArg.getItem();
            ResourceLocation itemId = BuiltInRegistries.ITEM.getKey(item);
            String itemIdString = itemId.toString();

            if (ItemBlacklistManager.addItem(itemIdString)) {
                source.sendSuccess(() -> Component.literal("§aAdded §e" + itemIdString + "§a to the blacklist."), true);
                return 1;
            } else {
                source.sendFailure(Component.literal("§cItem §e" + itemIdString + "§c is already blacklisted."));
                return 0;
            }
        } catch (Exception e) {
            source.sendFailure(Component.literal("§cInvalid item specified."));
            return 0;
        }
    }

    private static int executeRemove(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        String itemId = StringArgumentType.getString(context, "item");

        if (ItemBlacklistManager.removeItem(itemId)) {
            source.sendSuccess(() -> Component.literal("§aRemoved §e" + itemId + "§a from the blacklist."), true);
            return 1;
        } else {
            source.sendFailure(Component.literal("§cItem §e" + itemId + "§c is not in the blacklist."));
            return 0;
        }
    }

    private static int executeList(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        Set<String> blacklisted = ItemBlacklistManager.getBlacklistedItems();

        if (blacklisted.isEmpty()) {
            source.sendSuccess(() -> Component.literal("§eThe item blacklist is empty."), false);
            return 0;
        }

        source.sendSuccess(() -> Component.literal("§6§l=== Blacklisted Items ==="), false);
        for (String itemId : blacklisted) {
            source.sendSuccess(() -> Component.literal("§e- " + itemId), false);
        }
        source.sendSuccess(() -> Component.literal("§6Total: §e" + blacklisted.size() + " items"), false);

        return blacklisted.size();
    }

    private static int executeClear(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();
        int count = ItemBlacklistManager.getBlacklistedItems().size();

        ItemBlacklistManager.clearBlacklist();
        source.sendSuccess(() -> Component.literal("§aCleared §e" + count + "§a items from the blacklist."), true);

        return 1;
    }
}
