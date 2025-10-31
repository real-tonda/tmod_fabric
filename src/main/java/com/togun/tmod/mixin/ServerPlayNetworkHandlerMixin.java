package com.togun.tmod.mixin;

import com.togun.tmod.TModFabric;
import com.togun.tmod.combat.CombatTagManager;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayNetworkHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side mixin to handle combat logging
 */
@Mixin(ServerPlayNetworkHandler.class)
public class ServerPlayNetworkHandlerMixin {
    
    @Shadow
    public ServerPlayerEntity player;
    
    /**
     * Kill player if they disconnect during combat
     */
    @Inject(method = "onDisconnected", at = @At("HEAD"))
    private void onPlayerDisconnect(Text reason, CallbackInfo ci) {
        if (CombatTagManager.isInCombat(player)) {
            TModFabric.LOGGER.warn("Player {} combat logged! Killing player...", player.getName().getString());
            
            // Kill the player
            player.damage((net.minecraft.server.world.ServerWorld) player.getWorld(), player.getDamageSources().genericKill(), Float.MAX_VALUE);
            
            // Broadcast to server
            player.getServer().getPlayerManager().broadcast(
                Text.literal("§c" + player.getName().getString() + " §7disconnected during combat and died!"),
                false
            );
            
            // Remove combat tag
            CombatTagManager.removeTag(player.getUuid());
        }
    }
}

