package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minecraft.world.World;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side mixin to prevent throwing blacklisted projectiles
 */
@Mixin(ProjectileEntity.class)
public class ProjectileEntityMixin {
    
    /**
     * Cancels projectile spawning if the item is blacklisted
     */
    @Inject(method = "<init>(Lnet/minecraft/entity/EntityType;Lnet/minecraft/world/World;)V", at = @At("RETURN"))
    private void onProjectileCreate(CallbackInfo ci) {
        ProjectileEntity projectile = (ProjectileEntity) (Object) this;
        World world = projectile.getWorld();
        
        // Only check on server side
        if (!world.isClient) {
            Entity owner = projectile.getOwner();
            if (owner instanceof ServerPlayerEntity player) {
                // Check what item would create this projectile
                ItemStack item = getProjectileItem(projectile);
                
                if (item != null && ItemBlacklistManager.isBlacklisted(item)) {
                    player.sendMessage(Text.literal("§cThis item is blacklisted and cannot be thrown!"), true);
                    // Remove the projectile immediately
                    projectile.discard();
                }
            }
        }
    }
    
    /**
     * Determines which item stack corresponds to a projectile type
     */
    private ItemStack getProjectileItem(ProjectileEntity projectile) {
        if (projectile instanceof EnderPearlEntity) {
            return new ItemStack(Items.ENDER_PEARL);
        }
        // Can add more projectile types here as needed
        return null;
    }
}

