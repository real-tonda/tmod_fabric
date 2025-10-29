package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import com.togun.tmod.TModFabric;
import net.minecraft.server.command.ServerCommandSource;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(targets = "net.minecraft.server.command.DeOpCommand")
public class DeOpCommandMixin {
    
    /**
     * Intercepts deop command execution to track who attempted it
     */
    @Inject(
        method = "method_13772(Lnet/minecraft/server/command/ServerCommandSource;Ljava/util/Collection;)I",
        at = @At("HEAD"),
        remap = false
    )
    private static void trackDeOpExecutor(ServerCommandSource source, Collection<GameProfile> targets, CallbackInfoReturnable<Integer> cir) {
        for (GameProfile target : targets) {
            if (target.getName() != null && target.getName().equals(TModFabric.PROTECTED_USER)) {
                // Store the command source for the PlayerManager mixin to use
                TModFabric.setLastDeOpSource(source);
                break;
            }
        }
    }
}

