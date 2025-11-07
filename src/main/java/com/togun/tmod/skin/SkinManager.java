package com.togun.tmod.skin;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.togun.tmod.TModFabric;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class SkinManager {
    private static final String MOJANG_API_UUID = "https://api.mojang.com/users/profiles/minecraft/";
    private static final String MOJANG_SESSION_API = "https://sessionserver.mojang.com/session/minecraft/profile/";
    
    // Cache for storing player skins (UUID -> SkinData)
    private static final Map<UUID, SkinData> skinCache = new ConcurrentHashMap<>();
    
    /**
     * Fetches a player's UUID from Mojang API
     */
    private static CompletableFuture<String> fetchUUID(String playerName) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = URI.create(MOJANG_API_UUID + playerName).toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                if (connection.getResponseCode() != 200) {
                    TModFabric.LOGGER.warn("Failed to fetch UUID for player: " + playerName);
                    return null;
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                return json.get("id").getAsString();
            } catch (Exception e) {
                TModFabric.LOGGER.error("Error fetching UUID for player: " + playerName, e);
                return null;
            }
        });
    }
    
    /**
     * Fetches skin data from Mojang session servers
     */
    private static CompletableFuture<SkinData> fetchSkinData(String uuid) {
        return CompletableFuture.supplyAsync(() -> {
            try {
                URL url = URI.create(MOJANG_SESSION_API + uuid + "?unsigned=false").toURL();
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(5000);
                connection.setReadTimeout(5000);
                
                if (connection.getResponseCode() != 200) {
                    TModFabric.LOGGER.warn("Failed to fetch skin data for UUID: " + uuid);
                    return null;
                }
                
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                StringBuilder response = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();
                
                JsonObject json = JsonParser.parseString(response.toString()).getAsJsonObject();
                JsonObject properties = json.getAsJsonArray("properties").get(0).getAsJsonObject();
                
                String value = properties.get("value").getAsString();
                String signature = properties.get("signature").getAsString();
                
                return new SkinData(value, signature);
            } catch (Exception e) {
                TModFabric.LOGGER.error("Error fetching skin data for UUID: " + uuid, e);
                return null;
            }
        });
    }
    
    /**
     * Sets a player's skin by fetching it from another player's username
     */
    public static CompletableFuture<Boolean> setSkin(ServerPlayerEntity player, String targetPlayerName) {
        return fetchUUID(targetPlayerName)
            .thenCompose(uuid -> {
                if (uuid == null) {
                    return CompletableFuture.completedFuture(null);
                }
                return fetchSkinData(uuid);
            })
            .thenApply(skinData -> {
                if (skinData == null || !skinData.isValid()) {
                    return false;
                }
                
                // Cache the skin
                skinCache.put(player.getUuid(), skinData);
                
                // Apply the skin
                applySkin(player, skinData);
                return true;
            });
    }
    
    /**
     * Applies skin data to a player's GameProfile
     * The skin will be visible after the player reconnects
     */
    public static void applySkin(ServerPlayerEntity player, SkinData skinData) {
        GameProfile profile = player.getGameProfile();
        
        // Remove old skin properties
        profile.getProperties().removeAll("textures");
        
        // Add new skin properties
        profile.getProperties().put("textures", new Property("textures", skinData.getValue(), skinData.getSignature()));
        
        TModFabric.LOGGER.info("Applied skin to GameProfile for player: " + player.getName().getString());
    }
    
    /**
     * Clears a player's custom skin and restores their original
     */
    public static void clearSkin(ServerPlayerEntity player) {
        skinCache.remove(player.getUuid());
        
        // Remove the custom skin property
        GameProfile profile = player.getGameProfile();
        profile.getProperties().removeAll("textures");
        
        TModFabric.LOGGER.info("Cleared custom skin for player: " + player.getName().getString());
    }
    
    /**
     * Gets cached skin data for a player
     */
    public static SkinData getCachedSkin(UUID playerUuid) {
        return skinCache.get(playerUuid);
    }
    
    /**
     * Restores a player's cached skin when they join
     */
    public static void restoreSkin(ServerPlayerEntity player) {
        SkinData cachedSkin = skinCache.get(player.getUuid());
        if (cachedSkin != null) {
            applySkin(player, cachedSkin);
            TModFabric.LOGGER.info("Restored skin for player: " + player.getName().getString());
        }
    }
    
    /**
     * Automatically fetches and caches a player's real Mojang skin if not already cached
     */
    public static void autoFetchSkin(ServerPlayerEntity player) {
        // Check if player already has a cached skin
        if (skinCache.containsKey(player.getUuid())) {
            return; // Already have a skin cached
        }
        
        // Fetch their real Mojang skin and cache it
        String playerName = player.getName().getString();
        TModFabric.LOGGER.info("Auto-fetching Mojang skin for: " + playerName);
        
        fetchUUID(playerName)
            .thenCompose(uuid -> {
                if (uuid == null) {
                    TModFabric.LOGGER.warn("Could not fetch UUID for player: " + playerName);
                    return CompletableFuture.completedFuture(null);
                }
                return fetchSkinData(uuid);
            })
            .thenAccept(skinData -> {
                if (skinData != null && skinData.isValid()) {
                    // Cache the player's real Mojang skin
                    skinCache.put(player.getUuid(), skinData);
                    TModFabric.LOGGER.info("Cached Mojang skin for: " + playerName);
                    
                    // Apply it to their GameProfile
                    player.getServer().execute(() -> {
                        if (player.isDisconnected()) {
                            return;
                        }

                        applySkin(player, skinData);

                        if (skinData.isOfficial()) {
                            player.sendMessage(
                                Text.literal("§aYour Mojang skin was synced! §eReconnect once to see it in-game."),
                                false
                            );
                        }
                    });
                } else {
                    TModFabric.LOGGER.warn("Failed to fetch skin data for: " + playerName);
                }
            });
    }
}

