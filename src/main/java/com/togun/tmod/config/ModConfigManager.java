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
 * Manages server-side configuration for disabling specific mod features
 */
public class ModConfigManager {
    private static final Map<ModFeature, Boolean> featureStates = new HashMap<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File configFile;
    
    /**
     * All configurable mod features
     */
    public enum ModFeature {
        XAERO_ENTITY_RADAR("Xaero's Minimap - Entity Radar", "xaero.entity_radar"),
        XAERO_CAVE_MODE("Xaero's Minimap - Cave Maps", "xaero.cave_mode"),
        XAERO_WAYPOINTS("Xaero's Minimap - Waypoints", "xaero.waypoints");
        
        private final String displayName;
        private final String configKey;
        
        ModFeature(String displayName, String configKey) {
            this.displayName = displayName;
            this.configKey = configKey;
        }
        
        public String getDisplayName() {
            return displayName;
        }
        
        public String getConfigKey() {
            return configKey;
        }
        
        public static ModFeature fromConfigKey(String key) {
            for (ModFeature feature : values()) {
                if (feature.configKey.equalsIgnoreCase(key)) {
                    return feature;
                }
            }
            return null;
        }
    }
    
    public static void initialize(File configDir) {
        configFile = new File(configDir, "mod_features.json");
        loadConfig();
        
        // Set default states for all features (enabled by default)
        for (ModFeature feature : ModFeature.values()) {
            featureStates.putIfAbsent(feature, true);
        }
    }
    
    /**
     * Checks if a feature is enabled
     */
    public static boolean isFeatureEnabled(ModFeature feature) {
        return featureStates.getOrDefault(feature, true);
    }
    
    /**
     * Enables a feature
     */
    public static void enableFeature(ModFeature feature) {
        featureStates.put(feature, true);
        saveConfig();
    }
    
    /**
     * Disables a feature
     */
    public static void disableFeature(ModFeature feature) {
        featureStates.put(feature, false);
        saveConfig();
    }
    
    /**
     * Toggles a feature's state
     */
    public static boolean toggleFeature(ModFeature feature) {
        boolean newState = !isFeatureEnabled(feature);
        featureStates.put(feature, newState);
        saveConfig();
        return newState;
    }
    
    /**
     * Gets all features and their states
     */
    public static Map<ModFeature, Boolean> getAllFeatures() {
        return new HashMap<>(featureStates);
    }
    
    /**
     * Loads configuration from file
     */
    private static void loadConfig() {
        if (!configFile.exists()) {
            TModFabric.LOGGER.info("No mod features config found, creating default.");
            saveConfig();
            return;
        }
        
        try (FileReader reader = new FileReader(configFile)) {
            Type type = new TypeToken<Map<String, Boolean>>(){}.getType();
            Map<String, Boolean> loaded = GSON.fromJson(reader, type);
            
            if (loaded != null) {
                // Convert string keys back to enum
                for (Map.Entry<String, Boolean> entry : loaded.entrySet()) {
                    ModFeature feature = ModFeature.fromConfigKey(entry.getKey());
                    if (feature != null) {
                        featureStates.put(feature, entry.getValue());
                    }
                }
                TModFabric.LOGGER.info("Loaded mod features configuration");
            }
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to load mod features config", e);
        }
    }
    
    /**
     * Saves configuration to file
     */
    private static void saveConfig() {
        try {
            configFile.getParentFile().mkdirs();
            
            // Convert enum keys to strings for JSON
            Map<String, Boolean> toSave = new HashMap<>();
            for (Map.Entry<ModFeature, Boolean> entry : featureStates.entrySet()) {
                toSave.put(entry.getKey().getConfigKey(), entry.getValue());
            }
            
            try (FileWriter writer = new FileWriter(configFile)) {
                GSON.toJson(toSave, writer);
            }
            TModFabric.LOGGER.info("Saved mod features configuration");
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save mod features config", e);
        }
    }
}

