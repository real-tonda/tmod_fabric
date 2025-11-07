package com.togun.tmod.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.command.CommandSource;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class DimensionCommand {
    
    private static final SuggestionProvider<ServerCommandSource> DIMENSION_SUGGESTIONS = 
        (context, builder) -> {
            List<String> dimensionIds = new ArrayList<>();
            for (RegistryKey<World> key : context.getSource().getServer().getWorldRegistryKeys()) {
                dimensionIds.add(key.getValue().toString());
            }
            return CommandSource.suggestMatching(dimensionIds, builder);
        };
    
    public static void register() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> {
            registerDimensionCommands(dispatcher, registryAccess);
        });
    }
    
    private static void registerDimensionCommands(CommandDispatcher<ServerCommandSource> dispatcher, 
                                                   CommandRegistryAccess registryAccess) {
        // /dims - List all dimensions
        dispatcher.register(CommandManager.literal("dims")
            .requires(source -> source.hasPermissionLevel(4))
            .executes(context -> executeListDimensions(context))
        );
        
        // Alias: /dimensions
        dispatcher.register(CommandManager.literal("dimensions")
            .requires(source -> source.hasPermissionLevel(4))
            .executes(context -> executeListDimensions(context))
        );
        
        // /dim <dimension> - Teleport to dimension
        dispatcher.register(CommandManager.literal("dim")
            .requires(source -> source.hasPermissionLevel(4))
            .then(CommandManager.argument("dimension", StringArgumentType.string())
                .suggests(DIMENSION_SUGGESTIONS)
                .executes(context -> executeTeleportToDimension(context))
            )
        );
    }
    
    private static int executeListDimensions(CommandContext<ServerCommandSource> context) {
        ServerCommandSource source = context.getSource();
        
        source.sendFeedback(() -> Text.literal("§6§l=== Available Dimensions ==="), false);
        
        int totalPlayers = 0;
        for (RegistryKey<World> worldKey : source.getServer().getWorldRegistryKeys()) {
            ServerWorld world = source.getServer().getWorld(worldKey);
            if (world != null) {
                int playerCount = world.getPlayers().size();
                totalPlayers += playerCount;
                
                String dimensionId = worldKey.getValue().toString();
                String dimensionName = getDimensionDisplayName(worldKey);
                String playerInfo = playerCount > 0 ? " §7(§a" + playerCount + " player" + (playerCount != 1 ? "s" : "") + "§7)" : " §7(§8empty§7)";
                
                source.sendFeedback(() -> Text.literal("§e" + dimensionName + " §7[§f" + dimensionId + "§7]" + playerInfo), false);
            }
        }
        
        int finalTotalPlayers = totalPlayers;
        source.sendFeedback(() -> Text.literal("§6Total players across dimensions: §e" + finalTotalPlayers), false);
        
        return 1;
    }
    
    private static int executeTeleportToDimension(CommandContext<ServerCommandSource> context) throws CommandSyntaxException {
        ServerCommandSource source = context.getSource();
        ServerPlayerEntity player = source.getPlayerOrThrow();
        String dimensionInput = StringArgumentType.getString(context, "dimension");
        
        // Parse dimension identifier
        Identifier dimensionId = Identifier.tryParse(dimensionInput);
        if (dimensionId == null) {
            source.sendError(Text.literal("§cInvalid dimension identifier: " + dimensionInput));
            return 0;
        }
        
        // Get the dimension registry key
        RegistryKey<World> worldKey = RegistryKey.of(RegistryKeys.WORLD, dimensionId);
        ServerWorld targetWorld = source.getServer().getWorld(worldKey);
        
        if (targetWorld == null) {
            source.sendError(Text.literal("§cDimension not found: " + dimensionInput));
            return 0;
        }
        
        // Get spawn position for the target dimension
        BlockPos spawnPos = targetWorld.getSpawnPos();
        Vec3d targetPos = new Vec3d(spawnPos.getX() + 0.5, spawnPos.getY(), spawnPos.getZ() + 0.5);
        
        // Teleport the player
        player.teleport(targetWorld, targetPos.x, targetPos.y, targetPos.z, player.getYaw(), player.getPitch());
        
        String dimensionName = getDimensionDisplayName(worldKey);
        source.sendFeedback(() -> Text.literal("§aTeleported to dimension: §e" + dimensionName), false);
        
        return 1;
    }
    
    /**
     * Gets a user-friendly display name for a dimension
     */
    private static String getDimensionDisplayName(RegistryKey<World> worldKey) {
        String path = worldKey.getValue().getPath();
        
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

