package com.togun.tmod.config;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.togun.tmod.TModFabric;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages configuration for tutils command settings
 */
public class TutilsConfigManager {
    private static final Map<String, Boolean> configValues = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    
    // Valid config keys
    public static final String VILLAGER_INFINITE_RESTOCKS = "villager.infiniteRestocks";
    public static final String VILLAGER_FASTER_BREEDING = "villager.fasterBreeding";
    public static final String ANVIL_NOT_EXPENSIVE = "anvil.notExpensive";
    
    private static final String[] VALID_KEYS = {
        VILLAGER_INFINITE_RESTOCKS,
        VILLAGER_FASTER_BREEDING,
        ANVIL_NOT_EXPENSIVE
    };
    
    public static void initialize(File configDir) {
        configFile = new File(configDir, "tutils_config.json");
        loadConfig();
        
        // Set default states for all config values (false by default)
        for (String key : VALID_KEYS) {
            configValues.putIfAbsent(key, false);
        }
    }
    
    /**
     * Checks if a config key is valid
     */
    public static boolean isValidKey(String key) {
        for (String validKey : VALID_KEYS) {
            if (validKey.equals(key)) {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Gets a config value
     */
    public static boolean getConfigValue(String key) {
        return configValues.getOrDefault(key, false);
    }
    
    /**
     * Sets a config value
     */
    public static void setConfigValue(String key, boolean value) {
        if (isValidKey(key)) {
            configValues.put(key, value);
            saveConfig();
        }
    }
    
    /**
     * Gets all config values
     */
    public static Map<String, Boolean> getAllConfigValues() {
        return new HashMap<>(configValues);
    }
    
    /**
     * Loads configuration from file
     */
    private static void loadConfig() {
        if (!configFile.exists()) {
            TModFabric.LOGGER.info("No tutils config found, creating default.");
            saveConfig();
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, Boolean>>(){}.getType();
            Map<String, Boolean> loaded = GSON.fromJson(reader, type);
            
            if (loaded != null) {
                // Only load valid keys
                for (Map.Entry<String, Boolean> entry : loaded.entrySet()) {
                    if (isValidKey(entry.getKey())) {
                        configValues.put(entry.getKey(), entry.getValue());
                    }
                }
                TModFabric.LOGGER.info("Loaded tutils configuration");
            }
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to load tutils config", e);
        }
    }
    
    /**
     * Saves configuration to file
     */
    private static void saveConfig() {
        try {
            configFile.getParentFile().mkdirs();
            
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(configValues, writer);
            }
            TModFabric.LOGGER.info("Saved tutils configuration");
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save tutils config", e);
        }
    }
}

