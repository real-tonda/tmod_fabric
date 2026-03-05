package com.togun.tmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.togun.tmod.TModFabric;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class TPAllowListManager {
    private static final Set<String> allowedPlayers = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File allowListFile;

    public static void initialize(File configDir) {
        allowListFile = new File(configDir, "tp_allowlist.json");
        reload();
    }

    /**
     * Reloads the allow list from file
     */
    public static void reload() {
        allowedPlayers.clear();
        loadAllowList();
    }

    /**
     * Adds a player name to the allow list
     */
    public static boolean addPlayer(String playerName) {
        boolean added = allowedPlayers.add(playerName.toLowerCase());
        if (added) {
            saveAllowList();
        }
        return added;
    }

    /**
     * Removes a player name from the allow list
     */
    public static boolean removePlayer(String playerName) {
        boolean removed = allowedPlayers.remove(playerName.toLowerCase());
        if (removed) {
            saveAllowList();
        }
        return removed;
    }

    /**
     * Checks if a player is on the allow list
     */
    public static boolean isAllowed(String playerName) {
        return allowedPlayers.contains(playerName.toLowerCase());
    }

    /**
     * Checks if a command source is allowed to use TP
     */
    public static boolean canUseTP(ServerCommandSource source) {
        // OPs can always use /tp (permission level 2+)
        if (source.hasPermissionLevel(2)) {
            return true;
        }

        // Check if the source is a player and on the allow list
        if (source.getEntity() instanceof ServerPlayerEntity player) {
            return isAllowed(player.getName().getString());
        }

        return false;
    }

    /**
     * Gets all allowed players
     */
    public static Set<String> getAllowedPlayers() {
        return new HashSet<>(allowedPlayers);
    }

    /**
     * Loads the allow list from file
     */
    private static void loadAllowList() {
        if (!allowListFile.exists()) {
            TModFabric.LOGGER.info("No TP allow list file found, creating new one.");
            saveAllowList();
            return;
        }

        try (FileReader reader = new FileReader(allowListFile)) {
            Type type = new TypeToken<Set<String>>() {
            }.getType();
            Set<String> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                for (String name : loaded) {
                    allowedPlayers.add(name.toLowerCase());
                }
                TModFabric.LOGGER.info("Loaded {} players into TP allow list", allowedPlayers.size());
            }
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to load TP allow list", e);
        }
    }

    /**
     * Saves the allow list to file
     */
    private static void saveAllowList() {
        try {
            allowListFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(allowListFile)) {
                GSON.toJson(allowedPlayers, writer);
            }
            TModFabric.LOGGER.info("Saved TP allow list with {} players", allowedPlayers.size());
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save TP allow list", e);
        }
    }
}
