package com.togun.tmod.mixin;

import com.togun.tmod.commands.GodCommand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayer.class)
public class GodModeMixin {
    
    /**
     * Prevents players in god mode from dying
     * They can take damage but health never goes below 1.0 (0.5 hearts)
     */
    @Inject(method = "hurtServer", at = @At("RETURN"))
    private void preventDeathInGodMode(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        
        if (GodCommand.hasGodMode(player.getUUID())) {
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
    @Inject(method = "die", at = @At("HEAD"), cancellable = true)
    private void preventDeathEvent(DamageSource damageSource, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        
        if (GodCommand.hasGodMode(player.getUUID())) {
            // Cancel the death event
            ci.cancel();
            
            // Restore health to 0.5 hearts
            player.setHealth(1.0f);
            
            // Clear death timer if it was set
            player.setHealth(1.0f);
        }
    }
}
