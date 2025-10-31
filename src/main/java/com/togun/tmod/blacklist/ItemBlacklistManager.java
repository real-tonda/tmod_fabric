package com.togun.tmod.blacklist;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.togun.tmod.TModFabric;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.util.Identifier;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.Type;
import java.util.HashSet;
import java.util.Set;

public class ItemBlacklistManager {
    private static final Set<String> blacklistedItems = new HashSet<>();
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static File blacklistFile;
    
    public static void initialize(File configDir) {
        blacklistFile = new File(configDir, "item_blacklist.json");
        loadBlacklist();
    }
    
    /**
     * Adds an item to the blacklist
     */
    public static boolean addItem(String itemId) {
        // Validate that the item exists
        Identifier identifier = Identifier.tryParse(itemId);
        if (identifier == null || !Registries.ITEM.containsId(identifier)) {
            return false;
        }
        
        boolean added = blacklistedItems.add(itemId);
        if (added) {
            saveBlacklist();
        }
        return added;
    }
    
    /**
     * Removes an item from the blacklist
     */
    public static boolean removeItem(String itemId) {
        boolean removed = blacklistedItems.remove(itemId);
        if (removed) {
            saveBlacklist();
        }
        return removed;
    }
    
    /**
     * Checks if an item is blacklisted
     */
    public static boolean isBlacklisted(Item item) {
        Identifier id = Registries.ITEM.getId(item);
        return blacklistedItems.contains(id.toString());
    }
    
    /**
     * Checks if an item stack is blacklisted
     */
    public static boolean isBlacklisted(ItemStack stack) {
        return isBlacklisted(stack.getItem());
    }
    
    /**
     * Gets all blacklisted items
     */
    public static Set<String> getBlacklistedItems() {
        return new HashSet<>(blacklistedItems);
    }
    
    /**
     * Clears the entire blacklist
     */
    public static void clearBlacklist() {
        blacklistedItems.clear();
        saveBlacklist();
    }
    
    /**
     * Loads the blacklist from file
     */
    private static void loadBlacklist() {
        if (!blacklistFile.exists()) {
            TModFabric.LOGGER.info("No item blacklist file found, creating new one.");
            saveBlacklist();
            return;
        }
        
        try (FileReader reader = new FileReader(blacklistFile)) {
            Type type = new TypeToken<Set<String>>(){}.getType();
            Set<String> loaded = GSON.fromJson(reader, type);
            if (loaded != null) {
                blacklistedItems.addAll(loaded);
                TModFabric.LOGGER.info("Loaded {} blacklisted items", blacklistedItems.size());
            }
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to load item blacklist", e);
        }
    }
    
    /**
     * Saves the blacklist to file
     */
    private static void saveBlacklist() {
        try {
            blacklistFile.getParentFile().mkdirs();
            try (FileWriter writer = new FileWriter(blacklistFile)) {
                GSON.toJson(blacklistedItems, writer);
            }
            TModFabric.LOGGER.info("Saved item blacklist with {} items", blacklistedItems.size());
        } catch (Exception e) {
            TModFabric.LOGGER.error("Failed to save item blacklist", e);
        }
    }
}

