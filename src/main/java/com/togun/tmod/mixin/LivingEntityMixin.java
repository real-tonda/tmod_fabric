package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.network.chat.Component;
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
    @Inject(method = "hurtServer", at = @At("HEAD"), cancellable = true)
    private void onDamage(ServerLevel world, DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        Entity attacker = source.getEntity();
        
        if (attacker instanceof ServerPlayer player) {
            ItemStack weapon = player.getMainHandItem();
            
            if (ItemBlacklistManager.isBlacklisted(weapon)) {
                player.sendSystemMessage(Component.literal("§cThis item is blacklisted and cannot deal damage!"));
                cir.setReturnValue(false);
            }
        }
    }
}
