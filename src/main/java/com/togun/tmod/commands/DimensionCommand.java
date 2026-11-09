package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.Commands;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.SharedSuggestionProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class DimensionCommand {

    private static final SuggestionProvider<CommandSourceStack> DIMENSION_SUGGESTIONS = (context, builder) -> {
        List<String> dimensionIds = new ArrayList<>();
        for (ResourceKey<Level> key : context.getSource().getServer().levelKeys()) {
            dimensionIds.add(key.location().toString());
        }
        return SharedSuggestionProvider.suggest(dimensionIds, builder);
    };

    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerDimensionCommands(dispatcher, registryAccess);
        });
    }

    private static void registerDimensionCommands(CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext registryAccess) {
        // /dims - List all dimensions
        dispatcher.register(Commands.literal("dims")
                .requires(
                        source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source, "tmod.command.dims", 4))
                .executes(context -> executeListDimensions(context)));

        // Alias: /dimensions
        dispatcher.register(Commands.literal("dimensions")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source,
                        "tmod.command.dimensions", 4))
                .executes(context -> executeListDimensions(context)));

        // /dim <dimension> - Teleport to dimension
        dispatcher.register(Commands.literal("dim")
                .requires(source -> me.lucko.fabric.api.permissions.v0.Permissions.check(source, "tmod.command.dim", 4))
                .then(Commands.argument("dimension", StringArgumentType.string())
                        .suggests(DIMENSION_SUGGESTIONS)
                        .executes(context -> executeTeleportToDimension(context))));
    }

    private static int executeListDimensions(CommandContext<CommandSourceStack> context) {
        CommandSourceStack source = context.getSource();

        source.sendSuccess(() -> Component.literal("§6§l=== Available Dimensions ==="), false);

        int totalPlayers = 0;
        for (ResourceKey<Level> worldKey : source.getServer().levelKeys()) {
            ServerLevel world = source.getServer().getLevel(worldKey);
            if (world != null) {
                int playerCount = world.players().size();
                totalPlayers += playerCount;

                String dimensionId = worldKey.location().toString();
                String dimensionName = getDimensionDisplayName(worldKey);
                String playerInfo = playerCount > 0
                        ? " §7(§a" + playerCount + " player" + (playerCount != 1 ? "s" : "") + "§7)"
                        : " §7(§8empty§7)";

                source.sendSuccess(
                        () -> Component.literal("§e" + dimensionName + " §7[§f" + dimensionId + "§7]" + playerInfo), false);
            }
        }

        int finalTotalPlayers = totalPlayers;
        source.sendSuccess(() -> Component.literal("§6Total players across dimensions: §e" + finalTotalPlayers), false);

        return 1;
    }

    private static int executeTeleportToDimension(CommandContext<CommandSourceStack> context)
            throws CommandSyntaxException {
        CommandSourceStack source = context.getSource();
        ServerPlayer player = source.getPlayerOrException();
        String dimensionInput = StringArgumentType.getString(context, "dimension");

        // Parse dimension identifier
        ResourceLocation dimensionId = ResourceLocation.tryParse(dimensionInput);
        if (dimensionId == null) {
            source.sendFailure(Component.literal("§cInvalid dimension identifier: " + dimensionInput));
            return 0;
        }

        // Get the dimension registry key
        ResourceKey<Level> worldKey = ResourceKey.create(Registries.DIMENSION, dimensionId);
        ServerLevel targetWorld = source.getServer().getLevel(worldKey);

        if (targetWorld == null) {
            source.sendFailure(Component.literal("§cDimension not found: " + dimensionInput));
            return 0;
        }

        // Get spawn position for the target dimension
        BlockPos spawnPos = targetWorld.getSharedSpawnPos();
        Vec3 targetPos = new Vec3(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);

        // Teleport the player
        player.teleportTo(targetWorld, targetPos.x, targetPos.y, targetPos.z, java.util.Set.of(), player.getYRot(),
                player.getXRot(), false);

        String dimensionName = getDimensionDisplayName(worldKey);
        source.sendSuccess(() -> Component.literal("§aTeleported to dimension: §e" + dimensionName), false);

        return 1;
    }

    /**
     * Gets a user-friendly display name for a dimension
     */
    private static String getDimensionDisplayName(ResourceKey<Level> worldKey) {
        String path = worldKey.location().getPath();

        // Handle vanilla dimensions
        return switch (path) {
            case "overworld" -> "Overworld";
            case "the_nether" -> "The Nether";
            case "the_end" -> "The End";
            default -> {
                // Capitalize first letter of each word for modded dimensions
                String[] words = path.replace('_', ' ').split(" ");
                StringBuilder result = new StringBuilder();
                for (String word : words) {
                    if (!word.isEmpty()) {
                        result.append(Character.toUpperCase(word.charAt(0)))
                                .append(word.substring(1).toLowerCase())
                                .append(" ");
                    }
                }
                yield result.toString().trim();
            }
        };
    }
}
