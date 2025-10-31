package com.togun.tmod.mixin;

import com.togun.tmod.blacklist.ItemBlacklistManager;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Server-side mixin to prevent combat with blacklisted items
 */
@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin {
    
    /**
     * Prevents attacking with blacklisted weapons
     */
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onServerAttack(Entity target, CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        ItemStack mainHandStack = player.getMainHandStack();
        
        if (ItemBlacklistManager.isBlacklisted(mainHandStack)) {
            player.sendMessage(Text.literal("§cThis item is blacklisted and cannot be used for combat!"), true);
            ci.cancel();
        }
    }
}

