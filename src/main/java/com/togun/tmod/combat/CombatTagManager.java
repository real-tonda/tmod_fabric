package com.togun.tmod.combat;

import com.togun.tmod.TModFabric;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages combat tagging for anti-combat logging
 */
public class CombatTagManager {
    private static final Map<UUID, Long> combatTags = new HashMap<>();
    private static final long COMBAT_TAG_DURATION = 15000; // 15 seconds in milliseconds
    private static boolean aclEnabled = false;
    
    /**
     * Tags a player as being in combat
     */
    public static void tagPlayer(ServerPlayerEntity player) {
        if (!aclEnabled) return;
        
        UUID playerId = player.getUuid();
        long currentTime = System.currentTimeMillis();
        
        // Check if player is newly tagged
        boolean wasTagged = isInCombat(player);
        
        combatTags.put(playerId, currentTime + COMBAT_TAG_DURATION);
        
        if (!wasTagged) {
            player.sendMessage(Text.literal("§c§l⚔ §cYou are now in combat! Don't log out for 15 seconds!"), true);
            TModFabric.LOGGER.info("Player {} entered combat", player.getName().getString());
        }
    }
    
    /**
     * Checks if a player is currently in combat
     */
    public static boolean isInCombat(ServerPlayerEntity player) {
        if (!aclEnabled) return false;
        
        UUID playerId = player.getUuid();
        Long tagExpiry = combatTags.get(playerId);
        
        if (tagExpiry == null) {
            return false;
        }
        
        long currentTime = System.currentTimeMillis();
        if (currentTime >= tagExpiry) {
            // Tag expired
            combatTags.remove(playerId);
            return false;
        }
        
        return true;
    }
    
    /**
     * Gets remaining combat time in seconds
     */
    public static int getRemainingTime(ServerPlayerEntity player) {
        if (!aclEnabled) return 0;
        
        UUID playerId = player.getUuid();
        Long tagExpiry = combatTags.get(playerId);
        
        if (tagExpiry == null) {
            return 0;
        }
        
        long currentTime = System.currentTimeMillis();
        long remaining = tagExpiry - currentTime;
        
        if (remaining <= 0) {
            combatTags.remove(playerId);
            return 0;
        }
        
        return (int) Math.ceil(remaining / 1000.0);
    }
    
    /**
     * Removes combat tag from a player
     */
    public static void removeTag(ServerPlayerEntity player) {
        UUID playerId = player.getUuid();
        if (combatTags.remove(playerId) != null) {
            player.sendMessage(Text.literal("§a§l✓ §aYou are no longer in combat."), true);
            TModFabric.LOGGER.info("Player {} exited combat", player.getName().getString());
        }
    }
    
    /**
     * Removes tag by UUID (for disconnected players)
     */
    public static void removeTag(UUID playerId) {
        combatTags.remove(playerId);
    }
    
    /**
     * Checks and removes expired tags for a player
     */
    public static void checkExpiry(ServerPlayerEntity player) {
        if (!aclEnabled) return;
        
        UUID playerId = player.getUuid();
        Long tagExpiry = combatTags.get(playerId);
        
        if (tagExpiry != null) {
            long currentTime = System.currentTimeMillis();
            if (currentTime >= tagExpiry) {
                removeTag(player);
            }
        }
    }
    
    /**
     * Enables the ACL system
     */
    public static void enable() {
        aclEnabled = true;
        TModFabric.LOGGER.info("Anti-Combat Log system enabled");
    }
    
    /**
     * Disables the ACL system
     */
    public static void disable() {
        aclEnabled = false;
        combatTags.clear();
        TModFabric.LOGGER.info("Anti-Combat Log system disabled");
    }
    
    /**
     * Checks if ACL is enabled
     */
    public static boolean isEnabled() {
        return aclEnabled;
    }
    
    /**
     * Clears all combat tags
     */
    public static void clearAll() {
        combatTags.clear();
    }
}

