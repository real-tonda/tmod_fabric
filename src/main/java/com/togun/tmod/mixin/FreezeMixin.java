package com.togun.tmod.mixin;

import com.togun.tmod.commands.FreezeCommand;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayerEntity.class)
public class FreezeMixin {
    
    /**
     * Prevents frozen players from moving
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void preventMovementWhenFrozen(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity) (Object) this;
        
        if (FreezeCommand.isFrozen(player.getUuid())) {
            // Reset player velocity to prevent movement
            player.setVelocity(0, player.getVelocity().y, 0);
            
            // Teleport player back to their current position to cancel any movement
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            float yaw = player.getYaw();
            float pitch = player.getPitch();
            
            player.networkHandler.requestTeleport(x, y, z, yaw, pitch);
        }
    }
}

