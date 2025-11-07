package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Server-side mixin to prevent damaging entities with blacklisted items
 */
@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    
    /**
     * Prevents damage from blacklisted weapons
     */
    @Inject(method = "damage", at = @At("HEAD"), cancellable = true)
    private void onDamage(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getAttacker();
        
        if (attacker instanceof ServerPlayerEntity player) {
            ItemStack weapon = player.getMainHandStack();
            
            if (ItemBlacklistManager.isBlacklisted(weapon)) {
                player.sendMessage(Text.literal("§cThis item is blacklisted and cannot deal damage!"), true);
                cir.setReturnValue(false);
                cir.cancel();
            }
        }
    }
}

