package com.togun.tmod.mixin;

import com.mojang.authlib.GameProfile;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.togun.tmod.TModFabric;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Collection;

@Mixin(targets = "net.minecraft.server.command.DeOpCommand")
public class DeOpCommandMixin {
    
    /**
     * Intercepts deop command execution to notify the protected user who attempted it
     */
    @Inject(method = "deop", at = @At("HEAD"), cancellable = true)
    private static void interceptDeOp(ServerCommandSource source, Collection<GameProfile> targets, CallbackInfoReturnable<Integer> cir) throws CommandSyntaxException {
        for (GameProfile target : targets) {
            if (target.getName() != null && target.getName().equals(TModFabric.PROTECTED_USER)) {
                // Get the name of who executed the command
                String executorName;
                try {
                    ServerPlayerEntity executor = source.getPlayerOrThrow();
                    executorName = executor.getName().getString();
                } catch (CommandSyntaxException e) {
                    // Command was executed from console
                    executorName = "CONSOLE";
                }
                
                // Notify the protected user if they're online
                ServerPlayerEntity protectedPlayer = source.getServer().getPlayerManager().getPlayer(TModFabric.PROTECTED_USER);
                if (protectedPlayer != null) {
                    protectedPlayer.sendMessage(
                        Text.literal("§c" + executorName + " attempted to remove your operator status."), 
                        false
                    );
                }
                
                // Cancel the command silently
                cir.setReturnValue(0);
                return;
            }
        }
    }
}

