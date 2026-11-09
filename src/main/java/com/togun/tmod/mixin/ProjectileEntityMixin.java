package com.togun.tmod.mixin;

import com.togun.tmod.mixin.EntityAccessor;
import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side mixin to prevent throwing blacklisted projectiles
 */
@Mixin(Projectile.class)
public class ProjectileEntityMixin {

    /**
     * Cancels projectile spawning if the item is blacklisted
     */
    @Inject(method = "<init>(Lnet/minecraft/world/entity/EntityType;Lnet/minecraft/world/level/Level;)V", at = @At("RETURN"))
    private void onProjectileCreate(CallbackInfo ci) {
        Projectile projectile = (Projectile) (Object) this;

        // Only check on server side
        if (!((EntityAccessor) projectile).getLevelField().isClientSide()) {
            Entity owner = projectile.getOwner();
            if (owner instanceof ServerPlayer player) {
                // Check what item would create this projectile
                ItemStack item = getProjectileItem(projectile);

                if (item != null && ItemBlacklistManager.isBlacklisted(item)) {
                    player.sendSystemMessage(Component.literal("§cThis item is blacklisted and cannot be thrown!"));
                    // Remove the projectile immediately
                    projectile.discard();
                }
            }
        }
    }

    /**
     * Determines which item stack corresponds to a projectile type
     */
    private ItemStack getProjectileItem(Projectile projectile) {
        if (projectile instanceof ThrownEnderpearl) {
            return new ItemStack(Items.ENDER_PEARL);
        }
        // Can add more projectile types here as needed
        return null;
    }
}
