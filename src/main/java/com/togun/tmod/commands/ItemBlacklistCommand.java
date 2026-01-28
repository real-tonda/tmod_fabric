package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.command.argument.ItemStackArgumentType;
import net.minecraft.registry.Registries;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

import java.util.Set;

public class ItemBlacklistCommand {

    private static final SuggestionProvider<ServerCommandSource> BLACKLISTED_ITEMS_SUGGESTIONS = (context, builder) -> {
        Set<String> blacklisted = ItemBlacklistManager.getBlacklistedItems();
        return CommandSource.suggestMatching(blacklisted, builder);
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerItemBlacklist(dispatcher, registryAccess);
        });
    }

    private static void registerItemBlacklist(CommandDispatcher<ServerCommandSource> dispatcher,
            CommandRegistryAccess registryAccess) {
        dispatcher.register(CommandManager.literal("blacklist")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.blacklist", 4))

                // /blacklist add <item>
                .then(CommandManager.literal("add")
                        .then(CommandManager.argument("item", ItemStackArgumentType.itemStack(registryAccess))
                                .executes(context -> executeAdd(context, registryAccess))))

                // /blacklist remove <item>
                .then(CommandManager.literal("remove")
                        .then(CommandManager.argument("item", StringArgumentType.string())
                                .suggests(BLACKLISTED_ITEMS_SUGGESTIONS)
                                .executes(context -> executeRemove(context))))

                // /blacklist list
                .then(CommandManager.literal("list")
                        .executes(context -> executeList(context)))

                // /blacklist clear
                .then(CommandManager.literal("clear")
                        .executes(context -> executeClear(context))));
    }

    private static int executeAdd(CommandContext<ServerCommandSource> context, CommandRegistryAccess registryAccess) {
        ServerCommandSource source = context.getSource();

        try {
            var itemStackArg = ItemStackArgumentType.getItemStackArgument(context, "item");
            var item = itemStackArg.getItem();
            Identifier itemId = Registries.ITEM.getId(item);
            String itemIdString = itemId.toString();

            if (ItemBlacklistManager.addItem(itemIdString)) {
                source.sendFeedback(() -> Text.literal("§aAdded §e" + itemIdString + "§a to the blacklist."), true);
                return 1;
            } else {
                source.sendError(Text.literal("§cItem §e" + itemIdString + "§c is already blacklisted."));
                return 0;
            }
        } catch (Exception e) {
            source.sendError(Text.literal("§cInvalid item specified."));
            return 0;
        }
    }

    private static int executeRemove(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        String itemId = StringArgumentType.getString(context, "item");

        if (ItemBlacklistManager.removeItem(itemId)) {
            source.sendFeedback(() -> Text.literal("§aRemoved §e" + itemId + "§a from the blacklist."), true);
            return 1;
        } else {
            source.sendError(Text.literal("§cItem §e" + itemId + "§c is not in the blacklist."));
            return 0;
        }
    }

    private static int executeList(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        Set<String> blacklisted = ItemBlacklistManager.getBlacklistedItems();

        if (blacklisted.isEmpty()) {
            source.sendFeedback(() -> Text.literal("§eThe item blacklist is empty."), false);
            return 0;
        }

        source.sendFeedback(() -> Text.literal("§6§l=== Blacklisted Items ==="), false);
        for (String itemId : blacklisted) {
            source.sendFeedback(() -> Text.literal("§e- " + itemId), false);
        }
        source.sendFeedback(() -> Text.literal("§6Total: §e" + blacklisted.size() + " items"), false);

        return blacklisted.size();
    }

    private static int executeClear(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        int count = ItemBlacklistManager.getBlacklistedItems().size();

        ItemBlacklistManager.clearBlacklist();
        source.sendFeedback(() -> Text.literal("§aCleared §e" + count + "§a items from the blacklist."), true);

        return 1;
    }
}
