package com.togun.tmod.config;

import com.togun.tmod.TModFabric;
import com.togun.tmod.config.ModConfigManager.ModFeature;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.resources.ResourceLocation;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Manages Xaero's status effects for server-side feature control
 * Works even when mod is only installed on server (not client)
 */
public class XaeroEffectManager {
    
    private static final Map<ModFeature, String> FEATURE_TO_EFFECT = new HashMap<>();
    private static final int EFFECT_DURATION = 20 * 60 * 60 * 12; // 5 minutes in ticks
    private static final int EFFECT_AMPLIFIER = 0;
    
    static {
        // Map features to Xaero's built-in status effects
        FEATURE_TO_EFFECT.put(ModFeature.XAERO_ENTITY_RADAR, "xaerominimap:no_radar");
        FEATURE_TO_EFFECT.put(ModFeature.XAERO_CAVE_MODE, "xaerominimap:no_cave_maps");
        FEATURE_TO_EFFECT.put(ModFeature.XAERO_WAYPOINTS, "xaerominimap:no_waypoints");
    }
    
    /**
     * Applies configured Xaero effects to a player based on server settings
     * This works server-side only because Xaero's recognizes these effects
     * NOTE: Effects are applied to ALL players, including operators
     */
    public static void applyConfiguredEffects(ServerPlayer player) {
        for (Map.Entry<ModFeature, String> entry : FEATURE_TO_EFFECT.entrySet()) {
            ModFeature feature = entry.getKey();
            String effectId = entry.getValue();
            
            // If feature is disabled on server, apply the blocking effect to ALL players
            // No exceptions for operators - if it's disabled, it's disabled for everyone
            if (!ModConfigManager.isFeatureEnabled(feature)) {
                applyXaeroEffect(player, effectId);
            } else {
                // If feature is enabled, remove the blocking effect
                removeXaeroEffect(player, effectId);
            }
        }
    }
    
    /**
     * Applies a Xaero status effect to block a feature
     */
    private static void applyXaeroEffect(ServerPlayer player, String effectId) {
        try {
            ResourceLocation identifier = ResourceLocation.tryParse(effectId);
            if (identifier == null) return;
            
            Optional<Holder.Reference<MobEffect>> effectOpt = BuiltInRegistries.MOB_EFFECT.get(identifier);
            if (effectOpt.isEmpty()) {
                // Effect not found - Xaero's might not be installed on client
                // This is fine, we just can't enforce it
                return;
            }
            
            Holder<MobEffect> effectEntry = effectOpt.get();
            
            // Check if player already has this effect
            MobEffectInstance existing = player.getEffect(effectEntry);
            if (existing != null && existing.getDuration() > 20 * 30) {
                // Player already has effect with more than 30 seconds remaining
                return;
            }
            
            // Apply the effect (invisible, long duration)
            MobEffectInstance effectInstance = new MobEffectInstance(
                effectEntry,
                EFFECT_DURATION,
                EFFECT_AMPLIFIER,
                false, // ambient
                false, // show particles
                false  // show icon
            );
            
            player.addEffect(effectInstance);
            TModFabric.LOGGER.info("Applied Xaero effect " + effectId + " to player: " + player.getName().getString());
            
        } catch (Exception e) {
            // Log warning if effect can't be applied
            TModFabric.LOGGER.warn("Could not apply Xaero effect " + effectId + " (Xaero's Minimap may not be installed on client): " + e.getMessage());
        }
    }
    
    /**
     * Removes a Xaero status effect from a player
     */
    private static void removeXaeroEffect(ServerPlayer player, String effectId) {
        try {
            ResourceLocation identifier = ResourceLocation.tryParse(effectId);
            if (identifier == null) return;
            
            Optional<Holder.Reference<MobEffect>> effectOpt = BuiltInRegistries.MOB_EFFECT.get(identifier);
            if (effectOpt.isEmpty()) return;
            
            player.removeEffect(effectOpt.get());
            
        } catch (Exception e) {
            // Silently fail
            TModFabric.LOGGER.debug("Could not remove Xaero effect: " + effectId);
        }
    }
    
    /**
     * Refreshes effects for a specific feature across all online players
     * Called when feature configuration changes
     */
    public static void refreshFeatureForAllPlayers(ModFeature feature, net.minecraft.server.MinecraftServer server) {
        String effectId = FEATURE_TO_EFFECT.get(feature);
        if (effectId == null) return;
        
        boolean enabled = ModConfigManager.isFeatureEnabled(feature);
        
        for (ServerPlayer player : server.getPlayerList().getPlayers()) {
            if (enabled) {
                removeXaeroEffect(player, effectId);
            } else {
                applyXaeroEffect(player, effectId);
            }
        }
    }
}
