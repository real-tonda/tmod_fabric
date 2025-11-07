package com.togun.tmod.mixin;

import com.togun.tmod.commands.GodCommand;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class GodModeMixin {
    
    /**
     * Prevents players in god mode from dying
     * They can take damage but health never goes below 1.0 (0.5 hearts)
     */
    @Inject(method = "damage", at = @At("RETURN"))
    private void preventDeathInGodMode(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (GodCommand.hasGodMode(player.getUuid())) {
            // Ensure health never drops below 1.0 (0.5 hearts)
            if (player.getHealth() < 1.0f) {
                player.setHealth(1.0f);
            }
        }
    }
    
    /**
     * Intercepts the death event itself to prevent death
     * This is a failsafe in case damage reduces health to 0 or below
     */
    @Inject(method = "onDeath", at = @At("HEAD"), cancellable = true)
    private void preventDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (GodCommand.hasGodMode(player.getUuid())) {
            // Cancel the death event
            ci.cancel();
            
            // Restore health to 0.5 hearts
            player.setHealth(1.0f);
        }
    }
}

