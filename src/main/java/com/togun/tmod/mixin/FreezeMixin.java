package com.togun.tmod.mixin;

import com.togun.tmod.commands.FreezeCommand;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ServerPlayer.class)
public class FreezeMixin {
    
    /**
     * Prevents frozen players from moving
     */
    @Inject(method = "tick", at = @At("HEAD"))
    private void preventMovementWhenFrozen(CallbackInfo ci) {
        ServerPlayer player = (ServerPlayer) (Object) this;
        
        if (FreezeCommand.isFrozen(player.getUUID())) {
            // Reset player velocity to prevent movement
            player.setDeltaMovement(0, player.getDeltaMovement().y, 0);
            
            // Teleport player back to their current position to cancel any movement
            double x = player.getX();
            double y = player.getY();
            double z = player.getZ();
            float yaw = player.getYRot();
            float pitch = player.getXRot();
            
            player.connection.teleport(x, y, z, yaw, pitch);
        }
    }
}
