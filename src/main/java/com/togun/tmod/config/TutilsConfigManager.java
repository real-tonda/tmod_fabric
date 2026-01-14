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
    private static final Map<String, Boolean> booleanConfigValues = new HashMap<>();
    private static final Map<String, Integer> integerConfigValues = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;

    // Valid boolean config keys
    public static final String VILLAGER_INFINITE_RESTOCKS = "villager.infiniteRestocks";
    public static final String VILLAGER_FASTER_BREEDING = "villager.fasterBreeding";
    public static final String ANVIL_NOT_EXPENSIVE = "anvil.notExpensive";

    private static final String[] VALID_BOOLEAN_KEYS = {
            VILLAGER_INFINITE_RESTOCKS,
            VILLAGER_FASTER_BREEDING,
            ANVIL_NOT_EXPENSIVE
    };

    // Valid integer config keys
    public static final String REDSTONE_HOPPER_TICKS = "redstone.hopperTicks";

    private static final String[] VALID_INTEGER_KEYS = {
            REDSTONE_HOPPER_TICKS
    };

    public static void initialize(File configDir) {
        configFile = new File(configDir, "tutils_config.json");
        loadConfig();

        // Set default states for all boolean config values (false by default)
        for (String key : VALID_BOOLEAN_KEYS) {
            booleanConfigValues.putIfAbsent(key, false);
        }

        // Set default values for integer configs
        integerConfigValues.putIfAbsent(REDSTONE_HOPPER_TICKS, 8); // Vanilla default
    }

    /**
     * Checks if a boolean config key is valid
     */
    public static boolean isValidBooleanKey(String key) {
        for (String validKey : VALID_BOOLEAN_KEYS) {
            if (validKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if an integer config key is valid
     */
    public static boolean isValidIntegerKey(String key) {
        for (String validKey : VALID_INTEGER_KEYS) {
            if (validKey.equals(key)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Gets a boolean config value
     */
    public static boolean getConfigValue(String key) {
        return booleanConfigValues.getOrDefault(key, false);
    }

    /**
     * Sets a boolean config value
     */
    public static void setConfigValue(String key, boolean value) {
        if (isValidBooleanKey(key)) {
            booleanConfigValues.put(key, value);
            saveConfig();
        }
    }

    /**
     * Gets an integer config value
     */
    public static int getIntConfigValue(String key, int defaultValue) {
        return integerConfigValues.getOrDefault(key, defaultValue);
    }

    /**
     * Sets an integer config value
     */
    public static void setIntConfigValue(String key, int value) {
        if (isValidIntegerKey(key)) {
            integerConfigValues.put(key, value);
            saveConfig();
        }
    }

    /**
     * Gets all boolean config values
     */
    public static Map<String, Boolean> getAllConfigValues() {
        return new HashMap<>(booleanConfigValues);
    }

    /**
     * Gets all integer config values
     */
    public static Map<String, Integer> getAllIntConfigValues() {
        return new HashMap<>(integerConfigValues);
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
            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            Map<String, Object> loaded = GSON.fromJson(reader, type);

            if (loaded != null) {
                // Load valid boolean and integer keys
                for (Map.Entry<String, Object> entry : loaded.entrySet()) {
                    String key = entry.getKey();
                    Object value = entry.getValue();

                    if (isValidBooleanKey(key) && value instanceof Boolean) {
                        booleanConfigValues.put(key, (Boolean) value);
                    } else if (isValidIntegerKey(key)) {
                        // Handle both Integer and Double (JSON number parsing)
                        if (value instanceof Number) {
                            integerConfigValues.put(key, ((Number) value).intValue());
                        }
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

            // Merge both boolean and integer configs into one map
            Map<String, Object> allConfigs = new HashMap<>();
            allConfigs.putAll(booleanConfigValues);
            allConfigs.putAll(integerConfigValues);

            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(allConfigs, writer);
            }
            TModFabric.LOGGER.info("Saved tutils configuration");
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save tutils config", e);
        }
    }
}
