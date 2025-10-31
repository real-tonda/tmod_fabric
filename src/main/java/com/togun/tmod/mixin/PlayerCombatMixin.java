package com.togun.tmod.mixin;

import com.togun.tmod.combat.CombatTagManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Server-side mixin to detect combat and tag players
 */
@Mixin(ServerPlayerEntity.class)
public class PlayerCombatMixin {
    
    /**
     * Tag player when they attack another player
     */
    @Inject(method = "attack", at = @At("HEAD"))
    private void onPlayerAttack(Entity target, CallbackInfo ci) {
        ServerPlayerEntity attacker = (ServerPlayerEntity) (Object) this;
        
        // Tag attacker
        CombatTagManager.tagPlayer(attacker);
        
        // Tag victim if they're a player
        if (target instanceof ServerPlayerEntity victim) {
            CombatTagManager.tagPlayer(victim);
        }
    }
    
    /**
     * Tag player when they take damage from another player
     */
    @Inject(method = "damage", at = @At("HEAD"))
    private void onPlayerDamaged(ServerWorld world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity victim = (ServerPlayerEntity) (Object) this;
        Entity attacker = source.getAttacker();
        
        // Tag victim
        CombatTagManager.tagPlayer(victim);
        
        // Tag attacker if they're a player
        if (attacker instanceof ServerPlayerEntity attackerPlayer) {
            CombatTagManager.tagPlayer(attackerPlayer);
        }
    }
}

