package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side mixin to prevent combat with blacklisted items
 */
@Mixin(ServerPlayer.class)
public class ServerPlayerEntityMixin {
    
    /**
     * Prevents attacking with blacklisted weapons
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onServerAttack(Entity target, CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        ItemStack mainHandStack = player.getMainHandItem();
        
        if (ItemBlacklistManager.isBlacklisted(mainHandStack)) {
            player.sendSystemMessage(Component.literal("§cThis item is blacklisted and cannot be used for combat!"));
            ci.cancel();
        }
    }
}
